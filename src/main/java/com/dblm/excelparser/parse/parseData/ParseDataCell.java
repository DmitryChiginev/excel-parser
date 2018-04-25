package com.dblm.excelparser.parse.parseData;

import com.dblm.excelparser.parse.parsePattern.Column;
import com.dblm.excelparser.parse.parsePattern.Constanta;
import com.dblm.excelparser.parse.parsePattern.DataType;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Ячейка данных
 *
 * (соответстует конкретному полю в бд)
 */
public class ParseDataCell {

    /** Название поля */
    private String fieldName;
    /** Тип поля */
    private DataType type;
    /** Значение */
    private String value;
    /**
     * Флаг того, что поле не может
     * быть пустым (используется при
     * валидации ячеек)
     */
    private boolean notNull;
    /**
     * Формат даты
     * (в случае, если тип данных DATE)
     */
    private String dateFormat;
    /**
     * Метка "битой" ячейки.
     * (Выделяется в файле errors.xlsx)
     */
    private boolean wrong;
    /** Исходная ячейка excel */
    private Cell cell;
    /**
     * Комментарий к ячейке
     * (Используется только при отладке)
     */
    private String comment;

    private Column column;



    public ParseDataCell(Cell cell, Column column) {
        this.column = column;
        this.cell = cell;
    }

    public ParseDataCell(Constanta constanta) {
        this.fieldName = constanta.getTarget().getName();
        this.value = constanta.getSource().getValue();
        this.type = constanta.getSource().getType();
        this.notNull = true;
        this.comment = "Constant for field with name '" + this.fieldName + "'.";
    }

    /**
     * Метод валидации
     *
     * @return
     */
    public boolean isValid() {

        if (StringUtils.isBlank(fieldName)) {
            this.comment += "Not specified column name.";
            return false;
        }

        boolean valid = true;

        if (!isValidValueForType()) {
            this.comment += "Does not match type";
            valid = false;
        }

        if (notNull) {
            if(value == null) {
                this.comment += "This column must be not null. But value is null.";
                valid = false;
            }
        } else {
            if(!valid) {
                value = null;
                return true;
            }
        }

        return valid;
    }

    /**
     * Метод валидации по типу данных
     *
     * @return
     */
    private boolean isValidValueForType() {

        try {
            if (this.type == DataType.INTEGER
                    || this.type == DataType.DECIMAL
                    || this.type == DataType.MONEY
                    || this.type == DataType.TON) {
                if(StringUtils.isBlank(value)) {
                    return true;
                }
                if (isInteger() || isFloat()) {
                    return true;
                }
            }
            if (this.type == DataType.DATE) {
                return this.isDate();
            }
            if (this.type == DataType.STRING) {
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    private boolean isInteger() {

        try {
            Integer val = Integer.parseInt(value);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    private boolean isFloat() {

        try {
            Float val = Float.parseFloat(value);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * Проверка значения ячекий с типом DATE
     * (при проверке может происходить автоматическое преобразование к одному формату
     * в случае, если формат даты не указан в шаблоне)
     *
     * @return
     */
    private boolean isDate() {

        try {
            String dbPattern = "yyyy-MM-dd";
            DateTimeFormatter dbDateTimeFormatter = DateTimeFormatter.ofPattern(dbPattern);
            if(StringUtils.isBlank(value)) {
                return true;
            }

            if(StringUtils.isBlank(this.dateFormat)) {
                this.dateFormat = dbPattern;
            }

            this.value = this.value.replaceAll("/", ".");
            this.value = this.value.replaceAll("-", ".");
            this.value = this.value.replaceAll(",", ".");
            this.value = this.value.replaceAll("_", ".");
            this.value = this.value.replaceAll(" ", ".");


            this.dateFormat = this.dateFormat.replaceAll("/", ".");
            this.dateFormat = this.dateFormat.replaceAll("-", ".");
            this.dateFormat = this.dateFormat.replaceAll(",", ".");
            this.dateFormat = this.dateFormat.replaceAll("_", ".");
            this.dateFormat = this.dateFormat.replaceAll(" ", ".");


            String[] dateComp = this.value.split("\\.");
            String[] dateFormatComponent = this.dateFormat.split("\\.");
            if(dateComp.length < 3) {
                return false;
            }
            String tmp = "";
            for(int i = 0; i < dateComp.length; i++) {
                String cur = dateComp[i];
                if(cur.length() == 1) {
                    cur = "0" + cur;
                }
                if(cur.length() == 2 && dateFormatComponent[i].length() == 4) {
                    cur = "20" + cur;
                }

                if(tmp.length() == 0) {
                    tmp = cur;
                } else {
                    tmp = tmp + "." + cur;
                }
            }
            this.value = tmp;


            if(StringUtils.isNotBlank(this.dateFormat)) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.dateFormat);
                try {
                    LocalDate localDate = LocalDate.parse(this.value, dateTimeFormatter);
                    if (localDate != null) {
                        this.value = dbDateTimeFormatter.format(localDate);
                        return true;
                    }
                } catch (Exception e) {

                }
            }


            String pattern = "yyyy.MM.dd";
            switch (this.value.lastIndexOf('.')) {
                case 5:
                    pattern = "dd.MM.yyyy";
                    break;
                case 6:
                    pattern = "dd.MMM.yyyy";
                    break;
                case 7:
                    pattern = "yyyy.MM.dd";
                    break;
                case 8:
                    pattern = "yyyy.MMM.dd";
                    break;
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate localDate = LocalDate.parse(value, dateTimeFormatter);
            if (localDate != null) {
                this.value = dbDateTimeFormatter.format(localDate);
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    private Integer getInt(String val) {

        try {
            return Integer.parseInt(val);
        } catch (Exception e) {

        }
        return null;
    }


    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {

        if (!StringUtils.isBlank(fieldName)) {
            this.fieldName = fieldName.trim();
        }
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {

        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {

        if(StringUtils.isBlank(value)) {
            return;
        }
        this.value = value.trim();
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public boolean isWrong() {
        return wrong;
    }

    public void setWrong(boolean wrong) {
        this.wrong = wrong;
    }

    public boolean isFromColumn() {
        return this.column != null && !(this.column instanceof com.dblm.excelparser.parse.parsePattern.Cell);
    }

    public Column getColumn() {
        return column;
    }
}
