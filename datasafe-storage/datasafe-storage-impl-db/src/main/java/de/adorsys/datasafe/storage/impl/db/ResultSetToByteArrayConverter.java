package de.adorsys.datasafe.storage.impl.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetToByteArrayConverter {
    byte[] rowToByteArray(ResultSet resultSet) throws SQLException;
}
