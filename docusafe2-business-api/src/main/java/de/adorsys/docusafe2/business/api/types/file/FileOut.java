package de.adorsys.docusafe2.business.api.types.file;

import lombok.Data;

import java.io.InputStream;

@Data
public class FileOut {

    private final FileMeta meta;
    private final InputStream data;
}
