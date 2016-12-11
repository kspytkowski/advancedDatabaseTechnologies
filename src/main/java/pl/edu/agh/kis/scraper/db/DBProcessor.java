package pl.edu.agh.kis.scraper.db;

import pl.edu.agh.kis.scraper.model.Measurement;

import java.util.List;

public interface DBProcessor {

    void addMeasurementsToDB(List<Measurement> measurementsList);
    List<Measurement> getMeasurementsFromBody(String body);
    void closeConnection();
}
