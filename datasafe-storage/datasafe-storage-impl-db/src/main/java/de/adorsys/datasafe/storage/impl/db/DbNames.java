package de.adorsys.datasafe.storage.impl.db;

public enum DbNames {

    H2("org.h2.jdbcx.JdbcDataSource"),
    POSTGRES("jdbc:postgresql"),
    MYSQL("com.mysql.jdbc.Driver");

    private String className;

    DbNames(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
