package com.dblm.excelparser.repository;

import com.dblm.excelparser.parse.parseData.ParseDataCell;
import com.dblm.excelparser.parse.parseData.ParseDataItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Репозиторий
 *
 */
@org.springframework.stereotype.Repository
public class Repository {

    private final static Logger log = LogManager.getLogger(Repository.class);


    private Set<String> report;

    public Repository() {

        this.report = new LinkedHashSet<>();
    }
    /**
     * Проверка соединения с бд.
     *
     * @param connectionProperties
     * @return
     */
    public boolean checkConnectionWithTargetDB(ConnectionProperties connectionProperties) {

        DataSource dataSource = connectionProperties.getDataSource();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT 1")
        ) {

            return resultSet.next();
        } catch (Exception e) {
            this.logError("Ошибка тестового соединения с базой данных", e);
        }
        return false;
    }

    /**
     * Проверка существования схемы и таблицы.
     *
     * @param connectionProperties
     * @return
     */
    public boolean isExistsSchemaAndTable(ConnectionProperties connectionProperties) {

        DataSource dataSource = connectionProperties.getDataSource();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SHOW TABLES FROM " + connectionProperties.schema + " LIKE '" + connectionProperties.table + "'")
        ) {

            return resultSet.next();
        } catch (Exception e) {
            this.logError("Ошибка проверки существования схемы данных и таблицы.", e);
        }
        return false;
    }

    /**
     * Сохранение данных
     *
     * @param connectionProperties
     * @param parseDataItemsValid
     * @return
     */
    public boolean saveParseData(ConnectionProperties connectionProperties, Set<ParseDataItem> parseDataItemsValid) {

        this.report = new LinkedHashSet<>();
        DataSource dataSource = connectionProperties.getDataSource();
        try (
                Connection connection = dataSource.getConnection()

        ) {
            try {
                connection.setAutoCommit(false);
                for(ParseDataItem item : parseDataItemsValid) {
                    // execute statement
                    this.executeInsertStatement(connectionProperties, connection, item);
                }
                connection.commit();
                this.logInfo("Данные сохранены");
                return true;
            } catch (Exception e) {
                connection.rollback();
                this.logError("Ошибка сохранения данных", e);
            }
        } catch (Exception e) {
            this.logError("Ошибка сохранения данных", e);
        }
        return false;
    }

    /**
     * Выполняем INSERT
     *
     * @param connectionProperties
     * @param connection
     * @param item
     * @throws Exception
     */
    private void executeInsertStatement(ConnectionProperties connectionProperties, Connection connection, ParseDataItem item) throws Exception {

        List<String> columnNames = new LinkedList<>();
        List<String> values = new LinkedList<>();
        for(ParseDataCell cell : item.getParseDataCellSet()) {
            if(StringUtils.isBlank(cell.getValue())) {
                continue;
            }
            columnNames.add("`" + cell.getFieldName() + "`");
            values.add("'" + cell.getValue() + "'");
        }

        String q = "INSERT INTO "
                + connectionProperties.schema
                + "."
                + connectionProperties.table
                + " ("
                + StringUtils.join(columnNames, ",")
                + ") "
                + " VALUES ("
                + StringUtils.join(values, ",")
                + ")";

        try (PreparedStatement statement = connection.prepareStatement(q)) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private void logInfo(String message) {

        log.info(message);
        report.add(message);
    }

    private void logError(String message) {

        log.error(message);
        report.add(message);
    }

    private void logError(String message, Exception e) {

        log.error(message, e);
        report.add(message + " " + e.getMessage());
    }

    public Set<String> getReport() {
        return report;
    }

}
