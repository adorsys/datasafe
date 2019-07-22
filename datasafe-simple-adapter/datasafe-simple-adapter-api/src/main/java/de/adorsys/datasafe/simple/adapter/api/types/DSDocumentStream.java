package de.adorsys.datasafe.simple.adapter.api.types;

import java.io.InputStream;

public class DSDocumentStream {

    private DocumentFQN documentFQN;
    private InputStream documentStream;

    public DSDocumentStream(DocumentFQN documentFQN, InputStream documentStream) {
        this.documentFQN = documentFQN;
        this.documentStream = documentStream;
    }

    public DocumentFQN getDocumentFQN() {
        return documentFQN;
    }

    public InputStream getDocumentStream() {
        return documentStream;
    }
}
