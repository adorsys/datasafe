package de.adorsys.datasafe.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

import java.io.OutputStream;
import java.net.URI;

@Data
public class FileOut {

    @NonNull
    private final URI path;

    @NonNull
    private final OutputStream data;
}
