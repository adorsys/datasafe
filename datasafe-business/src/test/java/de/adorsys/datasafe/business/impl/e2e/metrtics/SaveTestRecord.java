package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper=true)
public class SaveTestRecord extends TestRecord {

    private String threadName;

    @Builder
    public SaveTestRecord(long duration , String threadName) {
        super(duration);
        this.threadName = threadName;
    }
}
