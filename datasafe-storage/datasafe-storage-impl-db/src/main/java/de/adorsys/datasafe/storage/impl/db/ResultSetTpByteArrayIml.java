package de.adorsys.datasafe.storage.impl.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetTpByteArrayIml implements ResultSetToByteArrayConverter {
    @Override
    public byte[] rowToByteArray(ResultSet resultSet) throws SQLException {
        return new byte[0];
    }
}
