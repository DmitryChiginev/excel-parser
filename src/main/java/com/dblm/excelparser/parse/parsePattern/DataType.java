package com.dblm.excelparser.parse.parsePattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Типы данных.
 */
public enum DataType {

    STRING,
    INTEGER,
    DECIMAL,
    MONEY, // Костыльный тип - для данного типа осуществляется перевод в копейки (умножение на 100)
    DATE,
    TON; // Костыльный тип - для данного типа выполняется перевод в кг (домножение на 1000)

    public static DataType getInstence(String name) {

        if(StringUtils.isBlank(name)) {
            return null;
        }
        if(name.trim().equalsIgnoreCase("STRING")) {
            return DataType.STRING;
        }
        if(name.trim().equalsIgnoreCase("INTEGER")) {
            return DataType.INTEGER;
        }
        if(name.trim().equalsIgnoreCase("DECIMAL")) {
            return DataType.DECIMAL;
        }
        if(name.trim().equalsIgnoreCase("MONEY")) {
            return DataType.MONEY;
        }
        if(name.trim().equalsIgnoreCase("DATE")) {
            return DataType.DATE;
        }
        if(name.trim().equalsIgnoreCase("TON")) {
            // Тонны
            return DataType.TON;
        }
        return null;
    }
}
