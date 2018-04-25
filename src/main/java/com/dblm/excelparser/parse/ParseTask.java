package com.dblm.excelparser.parse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.dblm.excelparser.parse.parsePattern.ParsePattern;
import com.dblm.excelparser.repository.Repository;
import com.dblm.excelparser.source.SourceManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Задача парсинга файлов
 *
 */
public class ParseTask implements Runnable {

    private final static Logger log = LogManager.getLogger(ParseTask.class);

    private SourceManager sourceManager;

    private Repository repository;

    private Parser parser;

    private List<Path> files;

    private List<MultipartFile> multipartFiles;

    private boolean withoutSaving;

    private Set<String> report;

    private boolean isRun;
    private boolean isComplete;
    private boolean isInterrupted;

    public ParseTask() {

        withoutSaving = true;
        report = new LinkedHashSet<>();
    }

    public void init() {

        // Если файлы были загружены с веба
        if(CollectionUtils.isNotEmpty(this.multipartFiles)) {
            this.files = sourceManager.saveMultipleFiles(this.multipartFiles);
        } else {
            // Здесь реализуем загрузку файлов при запланированном запуске
        }
    }

    public void run() {

        try {
            isInterrupted = false;
            isComplete = false;
            isRun = true;
            if(CollectionUtils.isEmpty(files)) {
                this.logError("Задача распознавания остановлена. Файлы не загружены");
                isRun = false;
                isInterrupted = true;
                return;
            }

            ParsePattern parsePattern = this.parser.buldParsePattern(files);
            if(parsePattern == null || !parsePattern.isValid()) {
                this.logError("Задача распознавания остановлена. Невалидный шаблон 'parse_pattern.xml'");
                isRun = false;
                isInterrupted = true;
                return;
            }
            this.logInfo("Валидный 'parse_pattern.xml'");

            if(!repository.checkConnectionWithTargetDB(parsePattern.getConnectionProperties())) {
                this.logError("Задача распознавания остановлена. Нет соединения с базой данных");
                isRun = false;
                isInterrupted = true;
                return;
            }
            this.logInfo("База данных доступна");

            if(!repository.isExistsSchemaAndTable(parsePattern.getConnectionProperties())) {
                this.logError("Задача распознавания остановлена. Указанные схема и таблица не найдены в базе данных");
                isRun = false;
                isInterrupted = true;
                return;
            }
            this.logInfo("Указанные схема и таблица найдены в базе данных");

            this.parser.setFilesForParse(this.files);
            this.logInfo("Начинаем распознавание файлов...");
            this.parser.run();
            this.report.addAll(this.parser.getReport());
            if(this.parser.getParseDataItemsValid().size() > 0 && !withoutSaving) {
                this.logInfo("Сохраняем данные в базе данных");
                this.repository.saveParseData(parsePattern.getConnectionProperties(), this.parser.getParseDataItemsValid());
                this.report.addAll(this.repository.getReport());
            }
            if(this.parser.getParseDataItemsNotValid().size() > 0) {
                this.sourceManager.saveWorkbook(this.parser.createWorkBookError());
                this.logInfo("Файл с ошибками сохранен");
            }
            this.logInfo("Задача распознавания завершена");
            isComplete = true;
        } catch (Exception e) {
            this.logError("Задача распознавания прервана", e);
        }
        isRun = false;
    }

    public SourceManager getSourceManager() {
        return sourceManager;
    }

    public void setSourceManager(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
    }
    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public List<Path> getFiles() {
        return files;
    }

    public void setFiles(List<Path> files) {
        this.files = files;
    }

    public List<MultipartFile> getMultipartFiles() {
        return multipartFiles;
    }

    public void setMultipartFiles(List<MultipartFile> multipartFiles) {
        this.multipartFiles = multipartFiles;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public boolean isWithoutSaving() {
        return withoutSaving;
    }

    public void setWithoutSaving(boolean withoutSaving) {
        this.withoutSaving = withoutSaving;
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

    public boolean isRun() {
        return isRun;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }
}
