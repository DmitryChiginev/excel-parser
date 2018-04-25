package com.dblm.excelparser.parse.parsePattern;

/**
 * Описание константы.
 */
public class Constanta{

    private Source source;

    private Target target;


    /**
     * Метод валидации описания константы
     *
     * @return
     */
    public boolean isValid() {

        if(this.source == null || this.target == null) {
            return false;
        }

        if(this.source.getType() == null || !this.target.isValid()) {
            return false;
        }

        return true;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
}
