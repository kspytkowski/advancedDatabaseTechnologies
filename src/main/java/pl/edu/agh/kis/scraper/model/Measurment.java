package pl.edu.agh.kis.scraper.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * Created by Jakub Fortunka on 10.12.2016.
 */

@Data
public class Measurment {

    @NonNull private String sensorId;
    @NonNull private LocalDateTime measurmentTimestamp;
    @NonNull private int measurment;

}
