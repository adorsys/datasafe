package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class TestMetricCollector {

    private Map<String, List<Long>> saveRecords = Collections.synchronizedMap(new HashMap<>());
    private Map<String, List<Long>> registerRecords = Collections.synchronizedMap(new HashMap<>());
    private int dataSize;
    private String storageType;
    private RecordCollection saveMetrics;
    private RecordCollection registerMetrics;
    private int numberOfThreads;

    public void writeToJSON() {
        saveMetrics = RecordCollection
                .builder()
                .dataSize(dataSize)
                .description("Save data")
                .storage(storageType)
                .records(saveRecords)
                .build();


        registerMetrics = RecordCollection
                .builder()
                .description("Register user")
                .storage(storageType)
                .records(registerRecords)
                .build();


        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(getFilePathForResultOfSaveTest()), saveMetrics);
            mapper.writeValue(new File(getFilePathForResultOfRegisterUserTest()), registerMetrics);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getFilePathForResultOfRegisterUserTest() {
        return "./target/metrics_register_user_" + storageType + "_" + numberOfThreads + "pool_size.json";
    }

    private String getFilePathForResultOfSaveTest() {
        return "./target/metrics_save_" + storageType + "_" + dataSize + "bytes_" + numberOfThreads + "pool_size.json";
    }

    public void addRegisterRecords(String userName, Long operationDuration) {
        if(registerRecords.get(userName) == null) {
            registerRecords.put(userName, Collections.synchronizedList(new ArrayList<>()));
        }

        registerRecords.get(userName).add(operationDuration);
    }

    public void addSaveRecord(String user, Long operationDuration) {
        if(saveRecords.get(user) == null) {
            saveRecords.put(user, Collections.synchronizedList(new ArrayList<>()));
        }

        saveRecords.get(user).add(operationDuration);
    }

}
