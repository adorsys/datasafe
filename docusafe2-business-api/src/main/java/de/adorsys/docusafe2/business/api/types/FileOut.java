package de.adorsys.docusafe2.business.api.types;

import lombok.Data;

import java.io.InputStream;

@Data
public class FileOut {

    private final FileMeta meta;
    private final InputStream data;
}
