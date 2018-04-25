package com.dblm.excelparser.parse.parsePattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Допустимые значения колонки
 */
public class Value {

    private String main;

    private Set<String> setAlt;

    private String mean;

    private boolean caseSensitive;

    private boolean strinctEquality;


    public boolean isValid() {

        if(StringUtils.isBlank(this.main) || StringUtils.isBlank(this.mean)) {
            return false;
        }

        return true;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {

        if(StringUtils.isBlank(main)) {
            return;
        }
        this.main = main.trim();
    }

    public Set<String> getSetAlt() {
        return setAlt;
    }

    public void setSetAlt(Set<String> setAlt) {
        this.setAlt = setAlt;
    }

    public void addAlt(String alt) {

        if(StringUtils.isBlank(alt)) {
            return;
        }
        if(this.setAlt == null) {
            this.setAlt = new LinkedHashSet<>();
        }
        this.setAlt.add(alt.trim());
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public boolean isExsistsAltMeanings() {

        return CollectionUtils.isNotEmpty(this.setAlt);
    }

    public boolean isMean(String value) {

        if(this.compareValues(value, this.main, this.caseSensitive, this.strinctEquality)) {
            return true;
        } else {
            if(CollectionUtils.isNotEmpty(this.setAlt)) {
                for(String alt : this.setAlt) {
                    if(this.compareValues(value, alt, this.caseSensitive, this.strinctEquality)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean compareValues(String v1, String v2, boolean caseSensitive, boolean strinctEquality) {

        if(strinctEquality) {
            if(caseSensitive && v1.equals(v2)) {
                return true;
            } else if(!caseSensitive && v1.equalsIgnoreCase(v2)) {
                return true;
            }
        } else {
            if(caseSensitive && v1.contains(v2)) {
                return true;
            } else if(!caseSensitive && StringUtils.containsIgnoreCase(v1, v2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseInsensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isStrinctEquality() {
        return strinctEquality;
    }

    public void setStrinctEquality(boolean strinctEquality) {
        this.strinctEquality = strinctEquality;
    }
}
