package de.adorsys.docusafe2.model;

import org.adorsys.cryptoutils.basetypes.BaseTypeByteArray;

/**
 * Created by peter on 23.12.2017 at 17:39:26.
 */
public class DocumentContent extends BaseTypeByteArray {
    public DocumentContent() {}

    public DocumentContent(byte[] value) {
        super(value);
    }
}
