package com.dblm.excelparser.parse;

import com.dblm.excelparser.parse.parseData.ParseDataCell;
import com.dblm.excelparser.parse.parseData.ParseDataItem;
import com.dblm.excelparser.parse.parsePattern.*;
import com.dblm.excelparser.parse.parsePattern.Cell;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер excel (xls, xlsx)
 */
public class Parser extends AbstractParser {

    private final static Logger log = LogManager.getLogger(Parser.class);

    /**
     *  Файлы для парсинга
     */
    private List<Path> filesForParse;

    /**
     * Книга с ошибками
     */
    private XSSFWorkbook errors;

    private Set<String> report;

    /**
     * Распарсенные данные
     */
    private Set<ParseDataItem> parseDataItems;
    private Set<ParseDataItem> parseDataItemsValid;
    private Set<ParseDataItem> parseDataItemsNotValid;


    public Parser() {
        parseDataItems = new LinkedHashSet<>();
        parseDataItemsValid = new LinkedHashSet<>();
        parseDataItemsNotValid = new LinkedHashSet<>();
        report = new LinkedHashSet<>();
    }

    /**
     * Записываем фалы для парсинга
     *
     * @param filesForParse
     */
    public void setFilesForParse(List<Path> filesForParse) {

        if(CollectionUtils.isEmpty(filesForParse)) {
            return;
        }
        this.filesForParse = new LinkedList<>();
        for (Path path : filesForParse) {
            String filname = path.getFileName().toString().toLowerCase();
            if(filname.contains(".xls") || filname.contains(".xlsx")) {
                this.filesForParse.add(path);
            }
        }
    }


    /**
     * Запуск процесса парсинга
     */
    public void run() throws Exception {

        if(CollectionUtils.isEmpty(this.filesForParse)) {
            logError("Файлы не найдены");
            return;
        }

        for (Path path : filesForParse) {
            logInfo("Распознавание фала " + path.toString());
            this.parse(path);
        }
        logInfo("Процесс распознавания файлов завершен. Выполняем валидацию");
        this.validate();
        logInfo("Количество валидных строк: " + this.getParseDataItemsValid().size());
        logInfo("Количество НЕвалидных строк: " + this.getParseDataItemsNotValid().size());
    }

    /**
     * Парсим
     *
     * @param path
     */
    public void parse(Path path) throws Exception {

        Workbook workbook = WorkbookFactory.create(path.toFile());
        Sheet sheet = workbook.getSheetAt(0);

        // Забираем константы из шаблона
        Set<ParseDataCell> constants = new HashSet<>();
        if(this.parsePattern.isExsistsConstants()) {
            for(Constanta c : this.parsePattern.getConstantsSet()) {
                constants.add(new ParseDataCell(c));
            }
        }
        // Парсим отдельные ячейки
        Set<ParseDataCell> cells = new HashSet<>();
        if(this.parsePattern.isExistsCells()) {
            for(Cell c : this.parsePattern.getCellSet()) {
                try {
                    ParseDataCell parseDataCell = this.parseCell(sheet, c, path.toString());
                    if(parseDataCell != null) {
                        cells.add(parseDataCell);
                    }
                } catch (Exception e) {
                    log.error("" , e);
                }
            }
        }

        // Парсим таблицу
        if(this.parsePattern.isExistsColumns()) {
            // Координаты колонок могут быть не известны, и поиск колонки осуществляется
            // по имени колонки. Поэтому, необходимо найти колонки и проиндексировать.
            this.findCoordinatesOfColumn(sheet, this.parsePattern.getColumnSet());
            // Индекс строки, с которой начинаются данные в таблице, тоже может быть
            // не указан, поэтому его нужно найти относительно колонки
            Integer dataRowStartIndex = this.findDataRowStartIndex();
            // Если данные брать не откуда
            if(dataRowStartIndex == null) {
                throw new NullPointerException("dataRowStartIndex is null");
            }
            int endIndex = this.parsePattern.getDataRowsEndIndex() != null
                    && this.parsePattern.getDataRowsEndIndex() > 0 ? this.parsePattern.getDataRowsEndIndex() : sheet.getLastRowNum()+1;

            for(int i = dataRowStartIndex; i < endIndex; i++) {
                Row row = sheet.getRow(i);
                if(row == null) {
                    continue;
                }
                ParseDataItem item = new ParseDataItem();
                item.setFromFile(path.toFile().getName());
                for(Column column : this.parsePattern.getColumnSet()) {
                    org.apache.poi.ss.usermodel.Cell c = row.getCell(column.getSource().getColIndex());
                    ParseDataCell parseDataCell = new ParseDataCell(c, column);
                    parseDataCell.setFieldName(column.getTarget().getName());
                    String value = null;
                    if(c != null) {
                        value = this.parseCellValue(c, column);
                        parseDataCell.setComment(this.buildComment(row.getRowNum(), c.getColumnIndex(), value, path.toString()));
                        if(column.getSource().getType() == DataType.INTEGER
                                || column.getSource().getType() == DataType.DECIMAL
                                || column.getSource().getType() == DataType.MONEY
                                || column.getSource().getType() == DataType.DATE) {
                            value = StringUtils.replaceAll(value, " ", "");
                        }
                        if(column.getSource().getType() == DataType.DECIMAL) {
                            value = this.formatFloatValue(value);
                        }
                        if(column.getSource().getType() == DataType.MONEY) {
                            value = this.toMinimumMonetaryUnit(value);
                        }
                        if(column.getSource().getType() == DataType.TON) {
                            value = this.toKilogram(value);
                        }
                    }
                    if(column.getSource().getType() == DataType.DATE) {
                        parseDataCell.setDateFormat(column.getSource().getDateFormat());
                    }
                    parseDataCell.setValue(value);
                    parseDataCell.setType(column.getSource().getType());
                    parseDataCell.setNotNull(column.getSource().isNotNull());
                    item.addParseDataCell(parseDataCell);
                    if(!CollectionUtils.isEmpty(constants)) {
                        item.addAllParseDataCell(constants);
                    }
                    if(!CollectionUtils.isEmpty(cells)) {
                        item.addAllParseDataCell(cells);
                    }
                }
                this.parseDataItems.add(item);
            }
        }
    }

    /**
     * Поиск индекса строки, с которой начинаются данные
     *
     * @return
     */
    private Integer findDataRowStartIndex() {

        // Если указан в шаблоне, то возвращаем его
        if(this.parsePattern.getDataRowsStartIndex() != null) {
            return this.parsePattern.getDataRowsStartIndex();
        } else {
            // Получаем добавочное число для поиска индекса колонки - отступ вниз на данное число
            Integer addition = this.parsePattern.getSearchDataRowsStartIndexWithAddition();
            //
            int count = 0;
            int sum = 0;
            for(Column column : this.parsePattern.getColumnSet()) {
                if(column.getSource().getRowIndex() == null) {
                    continue;
                }
                count++;
                sum += column.getSource().getRowIndex();
            }
            if(sum == 0) {
                return 1;
            }
            BigDecimal sumBig = new BigDecimal(sum);
            BigDecimal countBig = new BigDecimal(count);
            BigDecimal res = sumBig.divide(countBig, BigDecimal.ROUND_HALF_UP);
            if(res != null && addition != null) {
                return res.intValue() + addition;
            }
            return res.intValue() + 1;
        }
    }

    /**
     * Поиск координат колонок, если указано только имя колонки
     *
     * @param sheet
     * @param columnSet
     */
    private void findCoordinatesOfColumn(Sheet sheet, Set<Column> columnSet) throws Exception {

        for(Column column : columnSet) {
            if(column.getSource().getBy() == Source.By.NAME) {
                for(int i = 0; i < sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    for(int j = 0; j < row.getLastCellNum(); j++) {
                        org.apache.poi.ss.usermodel.Cell c = row.getCell(j);
                        String value = this.parseCellValue(c, column);
                        if(StringUtils.isNotBlank(value) && value.trim().equalsIgnoreCase(column.getSource().getName().trim())) {
                            column.getSource().setRowIndex(i);
                            column.getSource().setColIndex(j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Парсинг отдельной ячейки
     *
     * @param sheet
     * @param cell
     * @return
     */
    private ParseDataCell parseCell(Sheet sheet, Cell cell, String filename) throws Exception {

        if(cell.getSource().getBy() == Source.By.POSITION) {
            Row row = sheet.getRow(cell.getSource().getRowIndex());
            org.apache.poi.ss.usermodel.Cell c = row.getCell(cell.getSource().getColIndex());
            ParseDataCell parseDataCell = new ParseDataCell(c, cell);
            String value = null;
            if(c != null) {
                value = this.parseCellValue(c, cell);
                parseDataCell.setComment(this.buildComment(row.getRowNum(), c.getColumnIndex(), value, filename));
                if(cell.getSource().getType() == DataType.DECIMAL) {
                    value = this.formatFloatValue(value);
                }
                if(cell.getSource().getType() == DataType.MONEY) {
                    value = this.toMinimumMonetaryUnit(value);
                }
                if(cell.getSource().getType() == DataType.TON) {
                    value = this.toKilogram(value);
                }
                if(cell.getSource().getType() == DataType.DATE) {
                    parseDataCell.setDateFormat(cell.getSource().getDateFormat());
                }
            }

            parseDataCell.setFieldName(cell.getTarget().getName());
            if(value == null || "".equals(value.trim())) {
                if(cell.getDefaultValue() != null) {
                    value = cell.getDefaultValue();
                }
            }
            parseDataCell.setValue(value);
            parseDataCell.setType(cell.getSource().getType());
            parseDataCell.setNotNull(cell.getSource().isNotNull());
            return parseDataCell;
        }
        return null;
    }

    /**
     * Парсинг значения ячейки
     *
     * @param cell
     * @param parseElement
     * @return
     */
    private String parseCellValue(org.apache.poi.ss.usermodel.Cell cell, Constanta parseElement) throws Exception {

        DataFormatter formatter = new DataFormatter();
        String value = null;
        if(cell.getCellTypeEnum() == CellType.FORMULA) {
            if(cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC) {
                value = String.valueOf(cell.getNumericCellValue());
            } else if(cell.getCachedFormulaResultTypeEnum() == CellType.STRING) {
                value = cell.getRichStringCellValue().getString();
            }
        } else {
            value = formatter.formatCellValue(cell);
        }
        if(StringUtils.isBlank(value)) {
            return null;
        }
        if(parseElement instanceof Column) {
            Column column = (Column) parseElement;
            if(column.isExsistsParseStrategy()) {
                if(StringUtils.isNotBlank(column.getParseStrategy().getStartIndex()) && StringUtils.isNotBlank(column.getParseStrategy().getEndIndex())) {
                    int startIndex = 0;
                    int endIndex = value.length() - 1;
                    try {
                        startIndex = value.indexOf(column.getParseStrategy().getStartIndex());
                        endIndex = value.indexOf(column.getParseStrategy().getEndIndex());
                    } catch (Exception e) {

                    }
                    value = value.substring(startIndex+1, endIndex);
                }
                if(StringUtils.isNotBlank(column.getParseStrategy().getRegularExpression())) {
                    Pattern p = Pattern.compile(column.getParseStrategy().getRegularExpression());
                    Matcher matcher = p.matcher(value);
                    if(matcher.find()) {
                        value = matcher.group(0);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(column.getValues())) {
                String foundValue = null;
                for(Value v : column.getValues()) {
                    if(v.isMean(value)) {
                        foundValue = v.getMean();
                    }
                }
                if(StringUtils.isBlank(foundValue) && StringUtils.isNotBlank(column.getDefaultValue())) {
                   value = column.getDefaultValue();
                } else if(StringUtils.isNotBlank(foundValue)){
                    value = foundValue;
                } else {
                    value = null;
                }
            }
        }
        return value == null || value.trim().equals("") ? null : value;
    }

    /**
     * Валидация распарсенных данных
     *
     */
    private void validate() {

        log.info("Start ParseData validation");
        if(CollectionUtils.isEmpty(this.parseDataItems)) {
            log.error("ParseData collection is empty");
            return;
        }
        if(this.parseDataItemsValid == null) {
            this.parseDataItemsValid = new LinkedHashSet<>();
        }
        if(this.parseDataItemsNotValid == null) {
            this.parseDataItemsNotValid = new LinkedHashSet<>();
        }
        for(ParseDataItem parseDataItem : this.parseDataItems) {
            if(parseDataItem.isValid()) {
                this.parseDataItemsValid.add(parseDataItem);
            } else {
                this.parseDataItemsNotValid.add(parseDataItem);
            }
        }
    }

    /**
     * Создание excel с ошибками
     *
     * @return
     */
    public Workbook createWorkBookError() {

        if(CollectionUtils.isEmpty(this.parseDataItemsNotValid)) {
            return null;
        }
        try {
            errors = new XSSFWorkbook();
            XSSFSheet sheet = errors.createSheet("Ошибки");
            DataFormatter formatter = new DataFormatter();
            int i = 1;
            Iterator<ParseDataItem> itemIterator = this.parseDataItemsNotValid.iterator();
            while (itemIterator.hasNext()) {
                XSSFRow row = sheet.createRow(i);
                ParseDataItem parseDataItem = itemIterator.next();
                Iterator<ParseDataCell> itr = parseDataItem.getParseDataCellSet().iterator();
                int j = 0;
                while (itr.hasNext()) {
                    ParseDataCell parseDataCell = itr.next();
                    if(!parseDataCell.isFromColumn()) {
                        continue;
                    }
                    org.apache.poi.ss.usermodel.Cell cellOld = parseDataCell.getCell();
                    org.apache.poi.ss.usermodel.Cell cellNew = row.createCell(parseDataCell.getColumn().getSource().getColIndex());
                    cellNew.setCellValue(formatter.formatCellValue(cellOld));
                    if(parseDataCell.isWrong()) {
                        XSSFCellStyle cellStyle = errors.createCellStyle();
                        cellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
                        cellStyle.setFillPattern(FillPatternType.BIG_SPOTS);

                        cellNew.setCellStyle(cellStyle);
                    }
                    j++;
                }
                org.apache.poi.ss.usermodel.Cell cellAfter = row.createCell(j+3);
                cellAfter.setCellValue("файл: " + parseDataItem.getFromFile());
                i++;
            }
            return errors;
        } catch (Exception e) {
            log.error("Error creating workbook with errors", e);
        }
        return null;
    }


    /**
     * Приведение значения с плавающей точкой к одному виду
     *
     * @param value
     * @return
     */
    private String formatFloatValue(String value) {

        try {
            if(StringUtils.isBlank(value)) {
                return value;
            }
            if(!value.contains(",") && StringUtils.countMatches(value, ".") < 2) {
                return value;
            }
            if(StringUtils.countMatches(value, ",") == 1 && !value.contains(".")) {
                return value.replace(',', '.');
            }
            if(StringUtils.countMatches(value, ",") > 0 && value.contains(".")) {
                value = value.replaceAll(",", "");
                int lastForDot = value.lastIndexOf('.');
                int floatCountRanks = (value.length() - 1 - lastForDot);
                if(floatCountRanks == 0) {
                    return value.replaceAll("\\.", "");
                } else {
                    value = value.replaceAll("\\.", "");
                    int last = value.length() - 1;
                    String left = value.substring(0, value.length() - floatCountRanks);
                    String right = value.substring(last - floatCountRanks + 1, value.length());
                    value = left + "." + right;
                }
                return value;
            }

        } catch (Exception e) {
            log.error("" , e);
        }
        return null;
    }

    private String toMinimumMonetaryUnit(String value) {

        try {
            if(StringUtils.isBlank(value)) {
                return value;
            }
            if(StringUtils.countMatches(value, ",") == 1 && !value.contains(".")) {
                return value.replace(',', '.');
            }
            if(!value.contains(".")) {
                value += "00";
            }
            value = value.replaceAll("\\.", "");
            return value.replaceAll(",", "");
        } catch (Exception e) {
            log.error("", e);
        }
        return value;
    }

    private String toKilogram(String value) {

        try {
            if(StringUtils.isBlank(value)) {
                return value;
            }
            if(value.contains(".")) {
                String[] tmp = value.split("\\.");
                if(tmp[1].length() == 2) {
                    tmp[1] += "0";
                }
                if(tmp[1].length() == 1) {
                    tmp[1] += "00";
                }
                if(tmp[1].length() == 0) {
                    tmp[1] += "000";
                }
                if(tmp[0].equals("0")) {
                    tmp[0] = "";
                }
                return tmp[0] + tmp[1];
            } else {
                return value + "000";
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return value;
    }

    /**
     * Собираем комментарий к распознанному элементу
     *
     * @param rowIndex
     * @param cellIndex
     * @param value
     * @param file
     * @return
     */
    private String buildComment(Integer rowIndex, Integer cellIndex, String value, String file) {

        return "Исходные данные: строка-" + rowIndex + ", колонка-" + cellIndex + ", значение:\"" + value + "\", файл:\"" + file + "\".";
    }

    public Set<ParseDataItem> getParseDataItemsValid() {
        return parseDataItemsValid;
    }

    public void setParseDataItemsValid(Set<ParseDataItem> parseDataItemsValid) {
        this.parseDataItemsValid = parseDataItemsValid;
    }

    public Set<ParseDataItem> getParseDataItemsNotValid() {
        return parseDataItemsNotValid;
    }

    public void setParseDataItemsNotValid(Set<ParseDataItem> parseDataItemsNotValid) {
        this.parseDataItemsNotValid = parseDataItemsNotValid;
    }

    public Workbook getErrors() {
        return errors;
    }

    public void setErrors(XSSFWorkbook errors) {
        this.errors = errors;
    }


    private void logInfo(String message) {

        log.info(message);
        report.add(message);
    }

    private void logError(String message) {

        log.error(message);
        report.add(message);
    }

    private void logError(String message, Exception e) {

        log.error(message, e);
        report.add(message + " " + e.getMessage());
    }

    public Set<String> getReport() {
        return report;
    }
    public void setReport(Set<String> report) {
        this.report = report;
    }
}
