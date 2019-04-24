package de.adorsys.datasafe.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

import java.net.URI;

@Data
public class FileOut {

    @NonNull
    private final URI path;
}
