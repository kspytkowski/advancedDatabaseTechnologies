package pl.edu.agh.kis.scraper.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class Measurement {

    @NonNull
    private String sensorId;
    @NonNull
    private LocalDateTime measurmentTimestamp;
    @NonNull
    private int measurment;
}
