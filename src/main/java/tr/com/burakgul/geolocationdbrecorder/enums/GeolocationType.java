package tr.com.burakgul.geolocationdbrecorder.enums;

public enum GeolocationType {
    POLICE("POLICE"),
    AMBULANCE("AMBULANCE"),
    ;

    private final String text;

    GeolocationType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
