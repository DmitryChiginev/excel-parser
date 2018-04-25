package com.dblm.excelparser.repository;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

/**
 * Настройки соединения с базой данных
 *
 */
public class ConnectionProperties {

    /**
     * Диалект
     */
    public enum DialectDB {

        MYSQL,
        POSTGRESQL;

        public static DialectDB getInstance(String name) {

            if(StringUtils.isBlank(name)) {
                return null;
            }
            if(name.trim().equalsIgnoreCase("MYSQL")) {
                return DialectDB.MYSQL;
            }
            if(name.trim().equalsIgnoreCase("POSTGRESQL")) {
                return DialectDB.POSTGRESQL;
            }
            return null;
        }
    }

    /**
     * Тип операции в БД.
     */
    public enum OperationDB {

        INSERT,
        UPDATE;

        public static OperationDB getInstnce(String name) {

            if(StringUtils.isBlank(name)) {
                return null;
            }
            if(name.trim().equalsIgnoreCase("INSERT")) {
                return OperationDB.INSERT;
            }
            if(name.trim().equalsIgnoreCase("UPDATE")) {
                return OperationDB.UPDATE;
            }
            return null;
        }
    }

    public DialectDB dialectDB;
    public String url;
    public String username;
    public String password;
    public String schema;
    public String table;
    public OperationDB operationDB;

    public boolean isValid() {

        if(StringUtils.isBlank(this.schema)
                || StringUtils.isBlank(this.table)
                || this.dialectDB == null
                || StringUtils.isBlank(this.url)
                || StringUtils.isBlank(this.username)
                || StringUtils.isBlank(this.password)
                || this.operationDB == null) {
            return false;
        }
        return true;
    }

    public DataSource getDataSource() {

        if(this.dialectDB == DialectDB.MYSQL) {
            MysqlDataSource mysqlDataSource = new MysqlDataSource();
            mysqlDataSource.setURL(this.url);
            mysqlDataSource.setUser(this.username);
            mysqlDataSource.setPassword(this.password);
            mysqlDataSource.setDatabaseName(this.schema);
            return mysqlDataSource;
        }
        return null;
    }
}
