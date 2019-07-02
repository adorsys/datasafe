package de.adorsys.datasafe.storage.impl.db;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;

import java.net.URI;

/**
 * Parses DB uri by converting AbsoluteLocation to some reasonable URI.
 * Converts i.e.
 * jdbc-mysql://sa:password@localhost:3306/database/table
 * to
 * jdbc:mysql://localhost:3306/database
 * that can be used by jdbc driver
 */
public class DefaultDbUriExtractor implements DbUriExtractor {

    @Override
    public String extract(AbsoluteLocation location) {
        URI uri = location.location().asURI();

        if (uri.getPath() == null) {
            throw new IllegalArgumentException("Wrong url format");
        }

        String[] uriParts = uri.getPath().split("/");
        return uri.getScheme().replaceAll("-", ":") + "://" + uri.getHost() + ":" + uri.getPort() + "/" + uriParts[1];
    }
}
