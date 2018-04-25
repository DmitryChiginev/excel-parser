package com.dblm.excelparser.parse.parseData;

import com.dblm.excelparser.repository.ConnectionProperties;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Строка данных
 * (соответствует строке данных в указанной таблице)
 */
public class ParseDataItem {

    private Set<ParseDataCell> parseDataCellSet;

    /** Операция в базе - INSERT или UPDATE */
    private ConnectionProperties.OperationDB operation;
    /** Условие выполнения операции UPDATE */
    private String condition;

    private String fromFile;

    /**
     * Метод валидации
     *
     * @return
     */
    public boolean isValid() {

        if(CollectionUtils.isEmpty(this.parseDataCellSet)) {
            return false;
        }
        for(ParseDataCell parseDataCell : this.parseDataCellSet) {
            if(!parseDataCell.isValid()) {
                parseDataCell.setWrong(true);
                return false;
            }
        }
        return true;
    }

    public Set<ParseDataCell> getParseDataCellSet() {
        return parseDataCellSet;
    }

    public void setParseDataCellSet(Set<ParseDataCell> parseDataCellSet) {
        this.parseDataCellSet = parseDataCellSet;
    }

    public ConnectionProperties.OperationDB getOperation() {
        return operation;
    }

    public void setOperation(ConnectionProperties.OperationDB operation) {
        this.operation = operation;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void addParseDataCell(ParseDataCell parseDataCell) {

        if(CollectionUtils.isEmpty(this.parseDataCellSet)) {
            this.parseDataCellSet = new LinkedHashSet<>();
        }
        this.parseDataCellSet.add(parseDataCell);
    }

    public void addAllParseDataCell(Collection<ParseDataCell> collection) {

        if(CollectionUtils.isEmpty(this.parseDataCellSet)) {
            this.parseDataCellSet = new LinkedHashSet<>();
        }
        this.parseDataCellSet.addAll(collection);
    }

    public String getFromFile() {
        return fromFile;
    }

    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }
}
