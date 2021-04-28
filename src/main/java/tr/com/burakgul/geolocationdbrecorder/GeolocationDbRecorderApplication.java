package tr.com.burakgul.geolocationdbrecorder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tr.com.burakgul.geolocationdbrecorder.service.GeolocationService;

@SpringBootApplication
@RequiredArgsConstructor
public class GeolocationDbRecorderApplication implements CommandLineRunner {

    private final GeolocationService geolocationService;

    Logger logger = LoggerFactory.getLogger(GeolocationDbRecorderApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GeolocationDbRecorderApplication.class, args);
    }

    @Override
    public void run(String... args) {
        long scriptStartTime = System.currentTimeMillis();
        this.geolocationService.geolocationDataCreator();
        logger.info("Script executed {} ms.", System.currentTimeMillis() - scriptStartTime);
    }
}
