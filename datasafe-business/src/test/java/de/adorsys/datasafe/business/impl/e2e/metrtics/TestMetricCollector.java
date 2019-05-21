package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static j2html.TagCreator.*;

@Data
@Slf4j
public class TestMetricCollector<T extends TestRecord> {


    Map<String, List<TestRecord>> saveRecords = Collections.synchronizedMap(new HashMap<>());
    Map<String, List<TestRecord>> registerRecords = Collections.synchronizedMap(new HashMap<>());
    int dataSize;
    String storageType;
    RecordCollection saveCol;
    RecordCollection regCol;
    int numberOfThreads;

    public void writeToJSON() {
        saveCol = RecordCollection
                .builder()
                .dataSize(dataSize)
                .description("Save")
                .storage(storageType)
                .records(saveRecords)
                .build();


        regCol=RecordCollection
                .builder()
                .description("Register")
                .storage(storageType)
                .records(registerRecords)
                .build();


        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(getFilePathForResultOfSaveTest()), saveCol);
            mapper.writeValue(new File(getFilePathForResultOfRegisterUserTest()), regCol);
        } catch (IOException e) {
            e.printStackTrace();
        }
/*

        AtomicInteger counter = new AtomicInteger();
        String s = html(
                head(
                        title("Title"),
                        link().withRel("stylesheet").withHref("http://cdn.webix.com/site/webix.css?v=6.3.3").withType("text/css").withCharset("utf-8"),
                        script().withSrc("http://cdn.webix.com/site/webix.js?v=6.3.3").withType("text/javascript").withCharset("utf-8")
                ),
                body(div().withId("test"),
                            h3("Save operation metrics"),
                            table(
                                            tr(
                                                    th("Operation"),//.attr("colspan", 4),
                                                    th("Storage"),//.attr("colspan", 4),
                                                    th("Data Size"),//.attr("colspan", 4),
                                                    th("User Name"),
                                                    each(saveRecords, record -> th(
                                                            String.valueOf(counter.incrementAndGet())
                                                            )
                                                    )
                                            ),
                                            each(saveCol, save -> tr(
                                                    td("SAVE DATA"),
                                                    td(save.getKey()),
                                                    each(save.getValue().getRecords(), record -> tr(
                                                            td(String.valueOf(dataSize)),
                                                            td(String.valueOf(record.getKey())),
                                                            each(saveRecords, record2 -> td(String.valueOf(record2.getValue().getDuration())))
                                                    ))
                                            ))

                            ).attr("width", "100%").withId("metrics"),

                        rawHtml("<script type=\"text/javascript\" charset=\"utf-8\">" +
                                "webix.ready(function () {\n" +
                                "            grid = webix.ui({\n" +
                                "                container: \"test\",\n" +
                                "                view: \"datatable\",\n" +
                                "                select: \"cell\",\n" +
                                "                autoheight: true,\n" +
                                "                autowidth: true\n" +
                                "            });\n" +
                                "            grid.parse(\"metrics\", \"htmltable\");\n" +
                                "        });" +
                                "</script>"
                        )
                )
        ).renderFormatted();


        File f = new File("./target/metrics.html");
        try(FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(s.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private String getFilePathForResultOfRegisterUserTest() {
        return "./target/metrics_register_user_" + storageType + "_" + numberOfThreads + "pool_size.json";
    }

    @NotNull
    private String getFilePathForResultOfSaveTest() {
        return "./target/metrics_save_" + storageType + "_" + dataSize + "bytes_" + numberOfThreads + "pool_size.json";
    }

    public void logSave(Logger log) {
        /*listOfSaveRecords.entrySet().stream().sorted(Map.Entry.comparingByKey());

        for (Map.Entry<String, List<T>> record : listOfSaveRecords.entrySet()) {
            log.info("Storage: {} {}", record.getKey(), record.getValue());
        }*/
    }

    public void logRegister(Logger log) {
        /*listOfSaveRecords.entrySet().stream().sorted(Map.Entry.comparingByKey());

        for (Map.Entry<String, List<T>> record : listOfRegisterRecords.entrySet()) {
            log.info("Storage: {} {}", record.getKey(), record.getValue());
        }*/
    }

    public void addRegisterRecords(String userName, T record) {
        if(registerRecords.get(userName) == null) registerRecords.put(userName, Collections.synchronizedList(new ArrayList<>()));
        registerRecords.get(userName).add(record);
    }

    //System.out.println("For storage: " + descriptor.getStorageService().getClass().getName() + " and file size: " + size +
      //      " executed in " + Duration.between(start, end).toMillis() + "ms");*/
    //method get avarage
    //method get min
    //method get max
}
