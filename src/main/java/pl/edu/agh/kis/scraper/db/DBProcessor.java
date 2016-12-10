package pl.edu.agh.kis.scraper.db;

import pl.edu.agh.kis.scraper.model.Measurment;

import java.util.List;

/**
 * Created by Jakub Fortunka on 10.12.2016.
 */
public interface DBProcessor {

    public void addMeasurmentsToDB(List<Measurment> measurmentList);
    public List<Measurment> getMeasurmentsFromBody(String body);
}
