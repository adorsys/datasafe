package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Builder;
import lombok.ToString;

@ToString(callSuper=true)
public class SaveTestRecord extends TestRecord {

    private int dataSize;
    private String threadName;

    @Builder
    public SaveTestRecord(long duration, String userName, String storage, int size, String threadName) {
        super("Save data", duration, userName, storage);
        this.dataSize = size;
        this.threadName = threadName;
    }
}
