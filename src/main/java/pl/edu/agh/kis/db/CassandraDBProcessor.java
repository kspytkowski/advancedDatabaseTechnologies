package pl.edu.agh.kis.db;

import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.edu.agh.kis.model.Measurement;

import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
class CassandraDBProcessor implements DBProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraDBProcessor.class);
    private static final String[] CASSANDRA_NODES = new String[]{"10.156.207.9", "10.156.207.26", "10.156.207.175"};
    private static final String KEYSPACE = "measurements";
    private static final int SENSOR_INDEX = 0;
    private static final int TIME_INDEX = 1;
    private static final int MEASUREMENT_INDEX = 2;
    private static final String INSERT_STATEMENT = "INSERT INTO measurements (sensorID, time, value) VALUES (?,?,?)";
    private Cluster cluster;
    private Session session;
    private PreparedStatement preparedStatement;

    public CassandraDBProcessor() {
        cluster = Cluster.builder().addContactPoints(CASSANDRA_NODES).build();
        session = cluster.connect(KEYSPACE);
        preparedStatement = session.prepare(INSERT_STATEMENT);
        cluster.getConfiguration().getCodecRegistry().register(InstantCodec.instance);
    }

    @Override
    public void addMeasurementsToDB(List<Measurement> measurements) {
        if (!measurements.isEmpty()) {
            batches(measurements, 100).forEach(
                    z -> {
                        BatchStatement batchStatement = new BatchStatement();
                        z.forEach(x -> batchStatement.add(new BoundStatement(preparedStatement).bind(x.getSensorId(), x.getMeasurementTimestamp().toInstant(ZoneOffset.UTC), x.getMeasurement())));
                        session.execute(batchStatement);
                    }
            );
            LOG.info("Saved traffic data to database");
        } else {
            LOG.info("Got empty list of traffic data, nothing saved to database");
        }
    }

    @Override
    public List<Measurement> getMeasurementsFromBody(String body) {
        List<Measurement> ans = new ArrayList<>();
        if (body != null) {
            for (String line : body.split("\n")) {
                String[] fields = line.split(",");
                LocalDateTime measurementTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(fields[TIME_INDEX]));
                ans.add(new Measurement(fields[SENSOR_INDEX], measurementTime, Integer.valueOf(fields[MEASUREMENT_INDEX])));
            }
        }
        return ans;
    }

    @Override
    public void closeConnection() {
        cluster.close();
    }

    @PreDestroy
    void cleanUp() {
        closeConnection();
    }

    private <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("length = " + length);
        int size = source.size();
        if (size <= 0)
            return Stream.empty();
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }

}
