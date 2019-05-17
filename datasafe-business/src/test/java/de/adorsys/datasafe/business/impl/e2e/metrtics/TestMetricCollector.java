package de.adorsys.datasafe.business.impl.e2e.metrtics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
public class TestMetricCollector<T extends TestRecord> {

    private List<T> listOfSaveRecords;
    private List<T> listOfRegisterRecords;
    public TestMetricCollector() {
        listOfSaveRecords = Collections.synchronizedList(new ArrayList<>());
        listOfRegisterRecords = Collections.synchronizedList(new ArrayList<>());
    }

    public void addSaveRecord(T record) {
        listOfSaveRecords.add(record);
    }

    public void addRegisterRecord(T record) {
        listOfRegisterRecords.add(record);
    }

    public void printSave() {
        listOfSaveRecords.sort(Comparator.comparing(TestRecord::getStorage));

        for (TestRecord record : listOfSaveRecords) {
            System.out.println(record);
        }
    }

    public void writeToJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String jsonInString = null;
        try {
            mapper.writeValue(new File("./target/metrics.json"), this);

            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(jsonInString);

    }

    public void logSave(Logger log) {
        listOfSaveRecords.sort(Comparator.comparing(TestRecord::getStorage));

        for (TestRecord record : listOfSaveRecords) {
            log.info(record.toString());
        }
    }

    public void logRegister(Logger log) {
        listOfRegisterRecords.sort(Comparator.comparing(TestRecord::getStorage));

        for (TestRecord record : listOfRegisterRecords) {
            log.info(record.toString());
        }
    }

    //System.out.println("For storage: " + descriptor.getStorageService().getClass().getName() + " and file size: " + size +
      //      " executed in " + Duration.between(start, end).toMillis() + "ms");*/
    //method get avarage
    //method get min
    //method get max
}
