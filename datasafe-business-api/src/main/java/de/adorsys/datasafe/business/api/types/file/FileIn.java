package de.adorsys.datasafe.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;
import java.net.URI;

@Data
public class FileIn {

    @NonNull
    private final URI path;

    @NonNull
    private final InputStream data;
}
