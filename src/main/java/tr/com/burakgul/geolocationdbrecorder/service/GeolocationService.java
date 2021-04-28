package tr.com.burakgul.geolocationdbrecorder.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import tr.com.burakgul.geolocationdbrecorder.dto.OpenSKYNetwork;
import tr.com.burakgul.geolocationdbrecorder.enums.GeolocationType;
import tr.com.burakgul.geolocationdbrecorder.model.Geolocation;
import tr.com.burakgul.geolocationdbrecorder.repository.GeolocationRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GeolocationService {

    @Value("${geolocation.app.cycle.quantity:100}")
    private Integer cycleQuantity;

    @Value("${geolocation.app.api.username}")
    private String username;

    @Value("${geolocation.app.api.password}")
    private String password;

    private final GeolocationRepository geolocationRepository;

    private final Logger logger = LoggerFactory.getLogger(GeolocationService.class);

    public void geolocationDataCreator() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        int i = 0;
        while (i < this.cycleQuantity) {
            logger.info("Cycle {} executing.", i + 1);
            OpenSKYNetwork openSKYNetwork = null;
            long requestExecutionStart = 0;
            try {
                requestExecutionStart = System.currentTimeMillis();
                openSKYNetwork = this.getOpenSkyNetwork(requestExecutionStart);
            } catch (Exception e) {
                logger.error(e.getMessage());
                continue;
            }
            List<Geolocation> geolocationList = new ArrayList<>();
            if (openSKYNetwork != null && openSKYNetwork.getStates() != null && !openSKYNetwork.getStates().isEmpty()) {
                geolocationList = this.prepareFromOpenSKYNetworkToGeolocationList(openSKYNetwork, i);
            } else {
                logger.warn("OpenSKYNetwork state list empty or null.");
                continue;
            }
            int threadCount = i + 1;
            List<Geolocation> finalGeolocationList = geolocationList;
            executorService.submit(new Runnable() {
                @Override
                @Transactional
                public void run() {
                    logger.info("Database recorder thread {} executing.", threadCount);
                    long saveStart = System.currentTimeMillis();
                    geolocationRepository.saveAll(finalGeolocationList);
                    logger.info("Database recorder thread {} end. {} geolocation items saved for {} ms", threadCount, finalGeolocationList.size(), System.currentTimeMillis() - saveStart);
                }
            });
            this.sleepThread(requestExecutionStart);
            logger.info("Cycle {} completed.", i + 1);
            i++;
        }
        executorService.shutdown();
    }

    private OpenSKYNetwork getOpenSkyNetwork(long requestExecutionStart) {
        OpenSKYNetwork openSKYNetwork = WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build())
                .defaultHeaders(header -> {
                    if (this.username != null && this.password != null) {
                        header.setBasicAuth(this.username, this.password);
                    }
                })
                .build().get().uri("https://opensky-network.org/api/states/all").retrieve().bodyToMono(OpenSKYNetwork.class).block();
        logger.info("https://opensky-network.org/api/states/all request executed for {} ms.", System.currentTimeMillis() - requestExecutionStart);
        return openSKYNetwork;
    }

    private List<Geolocation> prepareFromOpenSKYNetworkToGeolocationList(OpenSKYNetwork openSKYNetwork, int cycle) {
        this.heapLogger();
        List<Geolocation> geolocationList = new ArrayList<>();
        int i = 0;
        long startPrepareData = System.currentTimeMillis();
        for (List<Object> state : openSKYNetwork.getStates()) {

            // Unix timestamp (seconds) for the last position update. Can be null if no position report was received by OpenSky within the past 15s.

            if (state.get(3) != null) {
                Geolocation geolocation = new Geolocation();
                if (i % 2 == 0) {
                    geolocation.setGeolocationType(GeolocationType.POLICE);
                } else {
                    geolocation.setGeolocationType(GeolocationType.AMBULANCE);
                }
                geolocation.setDataOrder(cycle + 1L);
                if (state.get(6) instanceof Double) {
                    geolocation.setLatitude((Double) state.get(6));
                } else if (state.get(6) instanceof Integer) {
                    geolocation.setLatitude(Double.valueOf((Integer) state.get(6)));
                }
                if (state.get(5) instanceof Double) {
                    geolocation.setLongitude((Double) state.get(5));
                } else if (state.get(5) instanceof Integer) {
                    geolocation.setLongitude(Double.valueOf((Integer) state.get(5)));
                }
                geolocation.setCallSign(((String) state.get(1)).replace(" ", ""));
                geolocation.setTimestamp((Integer) state.get(3));
                geolocationList.add(geolocation);
                i++;
            }
        }
        logger.info("{} geolocation items prepared for {} ms.", geolocationList.size(), System.currentTimeMillis() - startPrepareData);
        return geolocationList;
    }

    private void sleepThread(long requestExecutionStart) {
        try {
            long sleepTime = 5000 - (System.currentTimeMillis() - requestExecutionStart);
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void heapLogger() {
        logger.info("Heap Size: {}MB , Max Heap Size: {}MB, Free Heap Size: {}MB.",
                Runtime.getRuntime().totalMemory() / 1048576,
                Runtime.getRuntime().maxMemory() / 1048576,
                Runtime.getRuntime().freeMemory() / 1048576);
    }
}
