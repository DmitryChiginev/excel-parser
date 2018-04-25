package com.dblm.excelparser.parse.parsePattern;


import org.apache.commons.lang3.StringUtils;

/**
 * Стратегия разбора значения колонки
 *
 */
public class ParseStrategy {

    /**
     * Начальный символ, с которого начинаем забирать строку
     */
    private String startIndex;

    /**
     * Конечный символ по который забираем строку
     */
    private String endIndex;

    /**
     * Регулярное выражение, с помощью которого забираем значение
     */
    private String regularExpression;

    public boolean isValid() {

        if(this.startIndex != null && this.endIndex != null) {
            return true;
        }

        if(StringUtils.isBlank(this.regularExpression)) {
            return true;
        }

        return false;
    }

    public String getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(String startIndex) {

        if(StringUtils.isBlank(startIndex)) {
            return;
        }
        try {
            this.startIndex = startIndex.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(String endIndex) {

        if(StringUtils.isBlank(endIndex)) {
            return;
        }
        try {
            this.endIndex = endIndex.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression.trim();
    }
}
