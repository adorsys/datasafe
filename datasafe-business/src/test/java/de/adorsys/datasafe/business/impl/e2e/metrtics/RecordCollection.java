package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RecordCollection {

    private String description;
    private String storage;
    private int dataSize;
    private Map<String, List<Long>> records;

}
