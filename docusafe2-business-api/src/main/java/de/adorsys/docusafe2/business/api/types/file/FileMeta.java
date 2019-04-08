package de.adorsys.docusafe2.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

@Data
public class FileMeta {

    @NonNull
    private final String name;
}
