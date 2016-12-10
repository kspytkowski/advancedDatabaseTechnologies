package pl.edu.agh.kis.scraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.agh.kis.scraper.db.CassandraDBProcessor;
import pl.edu.agh.kis.scraper.db.DBProcessor;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
class PeriodicalDataScraper {

    private RestTemplate restTemplate = new RestTemplate();
    private static final String TRAFFIC_DATA_URL = "http://borg.kis.agh.edu.pl/~wojnicki/traffic.php";
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicalDataScraper.class);

    private static DBProcessor dbProcessor = new CassandraDBProcessor();

    @Scheduled(fixedRate = 90000)
    public void reportCurrentTime() {
        URI targetUrl = UriComponentsBuilder
                .fromUriString(TRAFFIC_DATA_URL)
                .build()
                .toUri();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.TEXT_PLAIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(acceptableMediaTypes);
        HttpEntity<String> entity = new HttpEntity<>("{ }", headers);
        ResponseEntity<String> trafficData;
        try {
            trafficData = restTemplate.exchange(targetUrl.toString(), HttpMethod.GET, entity, String.class);
            dbProcessor.addMeasurementsToDB(dbProcessor.getMeasurementsFromBody(trafficData.getBody()));
            // LOG.info("Got traffic data: " + trafficData.getBody());
            LOG.info("Got traffic data");
        } catch (ResourceAccessException rae) {
            LOG.error("Could not access resource: " + rae);
        } catch (Exception e) {
            LOG.error("Got unexpected exception: " + e);
        }
        // TODO - In my opinion we have to be prepared for data violating our constraints on database
        // e.g. service may return the same data twice...
    }

    @PreDestroy
    void cleanUp() {
        dbProcessor.closeConnection();
    }

}
