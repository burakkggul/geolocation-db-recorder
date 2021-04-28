package tr.com.burakgul.geolocationdbrecorder.model;

import lombok.Data;
import tr.com.burakgul.geolocationdbrecorder.enums.GeolocationType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@Data
public class Geolocation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geolocation_sequence")
    @SequenceGenerator(name = "geolocation_sequence", sequenceName = "geolocation_sequence", allocationSize = 1000)
    private Long id;

    @Column(name = "timestamp")
    private int timestamp;

    @Column(name = "data_order")
    private Long dataOrder;

    @Column(name = "geolocation_type")
    @Enumerated(EnumType.STRING)
    private GeolocationType geolocationType;

    private Double latitude;

    private Double longitude;

    @Column(name = "call_sign")
    private String callSign;
}
