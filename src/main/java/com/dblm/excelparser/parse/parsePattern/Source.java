package com.dblm.excelparser.parse.parsePattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Исходная колонка в файле
 * (исходная информация)
 *
 */
public class Source {

    /**
     * Определяется либо через позицию,
     * либо через имя колонки
     */
    public enum By {

        POSITION,
        NAME;

        public static By getInstance(String name) {

            if(StringUtils.isBlank(name)) {
                return null;
            }
            if(name.trim().equalsIgnoreCase("POSITION")) {
                return By.POSITION;
            }
            if(name.trim().equalsIgnoreCase("NAME")) {
                return By.NAME;
            }
            return null;
        }
    }

    private By by;

    private DataType type;

    private String dateFormat;

    private boolean notNull;

    /** Имя колонки */
    private String name;

    private Integer colIndex;

    private Integer rowIndex;

    private String value;


    public boolean isValid() {

        if(this.by == null ||
                this.type == null) {
            return false;
        }

        if(this.by == By.NAME && this.name == null) {
            return false;
        }

        if(this.by == By.POSITION && (this.colIndex == null && this.rowIndex == null)) {
            return false;
        }

        return true;
    }

    public By getBy() {
        return by;
    }

    public void setBy(By by) {
        this.by = by;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {

        if(StringUtils.isNotBlank(dateFormat)) {
            this.dateFormat = dateFormat.trim();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if(StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        }
    }

    public Integer getColIndex() {
        return colIndex;
    }

    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }
    public void setColIndex(String colIndex) {

        if(StringUtils.isBlank(colIndex)) {
            return;
        }
        try {
            this.colIndex = Integer.parseInt(colIndex.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setRowIndex(String rowIndex) {

        if(StringUtils.isBlank(rowIndex)) {
            return;
        }
        try {
            this.rowIndex = Integer.parseInt(rowIndex.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {

        if(!StringUtils.isBlank(value)) {
            this.value = value.trim();
        }
    }
}
