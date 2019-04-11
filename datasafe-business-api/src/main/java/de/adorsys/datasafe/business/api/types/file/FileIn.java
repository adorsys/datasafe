package de.adorsys.datasafe.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;

@Data
public class FileIn {

    @NonNull
    private final FileMeta meta;

    @NonNull
    private final InputStream data;
}
