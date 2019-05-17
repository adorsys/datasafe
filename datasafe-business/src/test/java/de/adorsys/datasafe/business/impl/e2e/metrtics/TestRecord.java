package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.AllArgsConstructor;
import lombok.Getter;

//make convertable to json
@Getter
@AllArgsConstructor
public class TestRecord {

    private String description;
    private long duration;
    private String userName;
    private String storage;

}
