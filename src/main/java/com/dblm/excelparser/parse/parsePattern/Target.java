package com.dblm.excelparser.parse.parsePattern;


import org.apache.commons.lang3.StringUtils;

/**
 * Целевая колонка баз данных.
 */
public class Target {

    private String name;


    public boolean isValid() {

        if(StringUtils.isBlank(this.name)) {
            return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if(StringUtils.isBlank(name)) {
            return;
        }
        this.name = name.trim();
    }
}
