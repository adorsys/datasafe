package de.adorsys.datasafe.storage.impl.db;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class JdbcOutputStream extends OutputStream {


    private byte[] buffer;
    private int pos;

    private JdbcTemplate jdbcTemplate;
    private String sql;
    private Object[] params;
    private String key;

    public JdbcOutputStream(JdbcTemplate jdbcTemplate, String sql, Object... params) {
        buffer = new byte[4096];
        this.jdbcTemplate = jdbcTemplate;
        this.sql = sql;
        this.params = params;
    }

    public JdbcOutputStream(JdbcTemplate jdbcTemplate, String sql, String key) {
        buffer = new byte[4096];
        this.jdbcTemplate = jdbcTemplate;
        this.sql = sql;
        this.key = key;
    }

    @Override
    public void write(int b) throws IOException {
        buffer[pos++] = (byte)b;
    }

    @Override
    public void close() throws IOException {
        super.close();
        jdbcTemplate.update(sql, key, getString());
        buffer = new byte[4096];
    }

    private String getString() {
        return new String(Arrays.copyOf(buffer, pos), Charset.forName("UTF-8"));
    }
}
