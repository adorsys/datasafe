package de.adorsys.datasafe.business.impl.e2e.metrtics;

import lombok.Builder;
import lombok.ToString;

@ToString(callSuper=true)
public class UserRegisterTestRecord extends TestRecord {

    @Builder
    UserRegisterTestRecord(long duration, String userName, String storage) {
        super("Register User", duration, userName, storage);
    }
}
