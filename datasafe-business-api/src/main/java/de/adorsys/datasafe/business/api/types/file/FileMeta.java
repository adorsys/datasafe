package de.adorsys.datasafe.business.api.types.file;

import lombok.Data;
import lombok.NonNull;

@Data
public class FileMeta {

    @NonNull
    private final String name;
}
