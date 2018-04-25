package com.dblm.excelparser.parse.parsePattern;

import com.dblm.excelparser.repository.ConnectionProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Шаблон распознавания файла
 */
public class ParsePattern {

    public final static String PARSE_PATTERN_FILE_NAME = "parse_pattern.xml";

    private ConnectionProperties connectionProperties;

    private Set<Constanta> constantsSet;

    private Set<Column> columnSet;

    private Set<Cell> cellSet;

    private Integer dataRowsStartIndex;

    private Integer dataRowsEndIndex;

    private Integer searchDataRowsStartIndexWithAddition;

    public ParsePattern() {
        this.connectionProperties = new ConnectionProperties();
    }

    public boolean isValid() {

        if (!this.connectionProperties.isValid()) {
            return false;
        }
        if(CollectionUtils.isEmpty(this.constantsSet) &&
                CollectionUtils.isEmpty(this.columnSet) &&
                CollectionUtils.isEmpty(this.cellSet)) {
            return false;
        }

        if(CollectionUtils.isNotEmpty(this.constantsSet)) {
            for(Constanta constanta : this.constantsSet) {
                if(!constanta.isValid()) {
                    return false;
                }
            }
        }

        if(CollectionUtils.isNotEmpty(this.columnSet)) {
            for(Column column : this.columnSet) {
                if(!column.isValid()) {
                    return false;
                }
            }
        }
        if(CollectionUtils.isNotEmpty(this.cellSet)) {
            for(Cell cell : this.cellSet) {
                if(!cell.isValid()) {
                    return false;
                }
            }
        }

        if(this.dataRowsStartIndex != null && this.searchDataRowsStartIndexWithAddition != null) {
            return false;
        }

        return true;
    }

    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    public ConnectionProperties.DialectDB getDialectDB() {
        return this.connectionProperties.dialectDB;
    }

    public void setDialectDB(ConnectionProperties.DialectDB dialectDB) {

        this.connectionProperties.dialectDB = dialectDB;
    }

    public String getDbURL() {
        return this.connectionProperties.url;
    }

    public void setDbURL(String dbURL) {

        if(StringUtils.isBlank(dbURL)) {
            return;
        }
        this.connectionProperties.url = dbURL.trim();
    }

    public String getUsername() {
        return this.connectionProperties.username;
    }

    public void setUsername(String username) {

        if(StringUtils.isBlank(username)) {
            return;
        }
        this.connectionProperties.username = username.trim();
    }

    public String getPassword() {
        return this.connectionProperties.password;
    }

    public void setPassword(String password) {

        if(StringUtils.isBlank(password)) {
            return;
        }
        this.connectionProperties.password = password.trim();
    }

    public String getDbSchema() {
        return this.connectionProperties.schema;
    }

    public void setDbSchema(String dbSchema) {

        if(StringUtils.isBlank(dbSchema)) {
            return;
        }
        this.connectionProperties.schema = dbSchema.trim();
    }

    public String getDbTable() {
        return this.connectionProperties.table;
    }

    public void setDbTable(String dbTable) {

        if(StringUtils.isBlank(dbTable)) {
            return;
        }
        this.connectionProperties.table = dbTable.trim();
    }

    public ConnectionProperties.OperationDB getOperationDB() {
        return this.connectionProperties.operationDB;
    }

    public void setOperationDB(ConnectionProperties.OperationDB operationDB) {
        this.connectionProperties.operationDB = operationDB;
    }

    public Set<Constanta> getConstantsSet() {
        return constantsSet;
    }

    public void setConstantsSet(Set<Constanta> constantsSet) {
        this.constantsSet = constantsSet;
    }

    public Set<Column> getColumnSet() {
        return columnSet;
    }

    public void setColumnSet(Set<Column> columnSet) {
        this.columnSet = columnSet;
    }

    public Set<Cell> getCellSet() {
        return cellSet;
    }

    public void setCellSet(Set<Cell> cellSet) {
        this.cellSet = cellSet;
    }

    public Integer getDataRowsStartIndex() {
        return dataRowsStartIndex;
    }

    public void setDataRowsStartIndex(Integer dataRowsStartIndex) {
        this.dataRowsStartIndex = dataRowsStartIndex;
    }

    public void setDataRowsStartIndex(String dataRowsStartIndex) {

        if(StringUtils.isBlank(dataRowsStartIndex)) {
            return;
        }
        try {
            this.dataRowsStartIndex = Integer.parseInt(dataRowsStartIndex.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getDataRowsEndIndex() {
        return dataRowsEndIndex;
    }

    public void setDataRowsEndIndex(Integer dataRowsEndIndex) {
        this.dataRowsEndIndex = dataRowsEndIndex;
    }

    public void setDataRowsEndIndex(String dataRowsEndIndex) {

        if(StringUtils.isBlank(dataRowsEndIndex)) {
            return;
        }
        try {
            this.dataRowsEndIndex = Integer.parseInt(dataRowsEndIndex.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getSearchDataRowsStartIndexWithAddition() {
        return searchDataRowsStartIndexWithAddition;
    }

    public void setSearchDataRowsStartIndexWithAddition(Integer searchDataRowsStartIndexWithAddition) {
        this.searchDataRowsStartIndexWithAddition = searchDataRowsStartIndexWithAddition;
    }

    public void setSearchDataRowsStartIndexWithAddition(String searchDataRowsStartIndexWithAddition) {

        if(StringUtils.isBlank(searchDataRowsStartIndexWithAddition)) {
            return;
        }
        try {
            this.searchDataRowsStartIndexWithAddition =  Integer.parseInt(searchDataRowsStartIndexWithAddition.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isExistsCells() {

        return CollectionUtils.isNotEmpty(this.cellSet);
    }

    public boolean isExistsColumns() {

        return CollectionUtils.isNotEmpty(this.columnSet);
    }

    public boolean isExsistsConstants() {

        return CollectionUtils.isNotEmpty(this.constantsSet);
    }
}
