package com.dblm.excelparser;

import com.dblm.excelparser.parse.ParseTask;
import com.dblm.excelparser.parse.Parser;
import com.dblm.excelparser.repository.Repository;
import com.dblm.excelparser.source.SourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Сервис выполнения задач парсинга
 *
 */
@Service
@PropertySource("classpath:application.properties")
public class Scheduler {

    private final static Logger log = LogManager.getLogger(Scheduler.class);

    @Value("${files.upload.dir}")
    private String pathToUploadDir;

    @Autowired
    private Repository repository;

    private ParseTask currentTask;

    /**
     * Метод немедленного выполнения задачи парсига
     *
     * @param files
     */
    public void parseNow(List<MultipartFile> files, boolean withoutSaving) {

        log.info("Был вызван метод 'parseNow'");
        if(currentTask != null && currentTask.isRun()) {
            log.error("Текущая задача выполняется. Запуск новой невозможен");
            return;
        }
        currentTask = new ParseTask();
        currentTask.setSourceManager(new SourceManager(this.pathToUploadDir));
        currentTask.setRepository(repository);
        currentTask.setParser(new Parser());
        currentTask.setMultipartFiles(files);
        currentTask.setWithoutSaving(withoutSaving);
        currentTask.init();

        Thread thread = new Thread(currentTask);
        thread.start();
    }

    public ParseTask getCurrentTask() {
        return currentTask;
    }

    public void clearContext() {

        this.currentTask = null;
    }
}
