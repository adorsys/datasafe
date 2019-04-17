package de.adorsys.datasafe.business.impl.serde;

import com.google.gson.Gson;
import lombok.experimental.Delegate;

import javax.inject.Inject;

public class GsonSerde {

    @Delegate
    private final Gson gson = new Gson();

    @Inject
    public GsonSerde() {
    }
}
