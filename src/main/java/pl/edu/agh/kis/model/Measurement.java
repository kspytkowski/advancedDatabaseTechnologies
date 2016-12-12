package pl.edu.agh.kis.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class Measurement {

    @NonNull
    private String sensorId;
    @NonNull
    private LocalDateTime measurementTimestamp;
    @NonNull
    private int measurement;

}
