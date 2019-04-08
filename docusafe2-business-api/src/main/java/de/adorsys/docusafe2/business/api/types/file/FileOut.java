package de.adorsys.docusafe2.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

import java.io.OutputStream;

@Data
public class FileOut {

    @NonNull
    private final FileMeta meta;

    @NonNull
    private final OutputStream data;
}
