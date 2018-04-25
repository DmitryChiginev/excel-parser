package com.dblm.excelparser.source;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;


/**
 * Менеджер исходников
 *
 */
public class SourceManager {

    private final  static Logger log = LogManager.getLogger(SourceManager.class);

    private String pathToUploadDir;

    private Path currentWorkDir;

    private Path pathToErrors;

    public SourceManager(String pathToUploadDir) {

        this.pathToUploadDir = pathToUploadDir;
    }

    /**
     * Загружает файлы на сервер в указанную директорию
     *
     * @param files
     * @return
     */
    public List<Path> saveMultipleFiles(List<MultipartFile> files) {

        List<Path> paths = new LinkedList<>();
        for(MultipartFile file : files) {
            try {
                Path p = this.convert(file);
                paths.add(p);
            } catch (Exception e) {
                System.out.println(e);
                log.error("Error converting file " + file.getOriginalFilename(), e);
            }
        }
        return paths;
    }

    /**
     * Конвертирукт MultipartFile в Path
     *
     * @param file
     * @return
     * @throws IOException
     */
    private Path convert(MultipartFile file) throws IOException {

        Path p = Paths.get(getCurrentWorkDir().toString() + "/" + file.getOriginalFilename());
        Files.write(p, file.getBytes());
        return p;
    }


    /**
     * Возвращает текущюю рабочую директорию
     *
     * @return
     */
    public Path getCurrentWorkDir() {
        if(this.currentWorkDir == null) {
            this.createNewWorkDir();
        }
        return currentWorkDir;
    }

    /**
     * Создает новую рабочую директорию
     *
     */
    private void createNewWorkDir() {

        LocalDateTime datetime = LocalDateTime.now();
        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
        String nameDir = "parse_by_" + datetime.format(formatter);
        try {
            currentWorkDir = Paths.get(pathToUploadDir + nameDir);
            currentWorkDir = Files.createDirectories(currentWorkDir);
        } catch (IOException e) {
            log.error("Error of creating work directory.", e);
        }
    }


    public void saveWorkbook(Workbook wb) {

        if(wb == null) {
            return;
        }
        String fileSeparator = "/";
        if(this.getCurrentWorkDir().toString().contains("\\")) {
            fileSeparator = "\\";
        }
        String path = this.getCurrentWorkDir() + fileSeparator + "errors.xlsx";
        pathToErrors  = Paths.get(path);
        try (FileOutputStream fos = new FileOutputStream(path)) {

            wb.write(fos);
            wb.close();
        } catch (Exception e)  {
            log.error("Error saving error excel", e);
        }
    }

    public Path getPathToErrors() {
        return pathToErrors;
    }

    public void setPathToErrors(Path pathToErrors) {
        this.pathToErrors = pathToErrors;
    }
}
