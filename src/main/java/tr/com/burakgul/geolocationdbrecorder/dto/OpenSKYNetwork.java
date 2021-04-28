package tr.com.burakgul.geolocationdbrecorder.dto;

import lombok.Data;

import java.util.List;

@Data
public class OpenSKYNetwork {
    private int time;
    List<List<Object>> states;
}
