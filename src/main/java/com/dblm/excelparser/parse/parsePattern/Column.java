package com.dblm.excelparser.parse.parsePattern;

import java.util.Set;

/**
 * Описание колонки excel-таблицы
 */
public class Column extends Constanta {

    private Set<Value> values;

    private ParseStrategy parseStrategy;

    private String defaultValue;

    /**
     * Метод валидации описания колонки
     *
     * @return
     */
    public boolean isValid() {

        // Здесь переопределяем метод валидации,
        // т.к. он отличен от валидации константы.
        if(this.getSource() == null || this.getTarget() == null) {
            return false;
        }

        if(!this.getSource().isValid() || !this.getTarget().isValid()) {
            return false;
        }

        return true;
    }

    public Set<Value> getValues() {
        return values;
    }

    public void setValues(Set<Value> values) {
        this.values = values;
    }

    public ParseStrategy getParseStrategy() {
        return parseStrategy;
    }

    public void setParseStrategy(ParseStrategy parseStrategy) {
        this.parseStrategy = parseStrategy;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {

        if(defaultValue == null || defaultValue.equalsIgnoreCase("null")) {
            return;
        }
        this.defaultValue = defaultValue.trim();
    }

    public boolean isExsistsParseStrategy() {

        return this.parseStrategy != null;
    }
}
