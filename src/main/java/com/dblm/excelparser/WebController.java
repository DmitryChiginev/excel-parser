package com.dblm.excelparser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Веб-контроллер
 *
 */
@Controller
public class WebController {

    private final static Logger log = LogManager.getLogger(WebController.class);

    public final static String ATTRIBUTE_NAME_OF_CURRENT_ERROR_PATH = "ATTRIBUTE_NAME_OF_CURRENT_ERROR_PATH";

    @Autowired
    private Scheduler scheduler;

    private Set<String> currentReport;

    /**
     * Главня страница.
     *
     * @param model
     * @return
     */
    @RequestMapping("/")
    public String index(HttpServletRequest request, Model model) {

        return new ViewBuilder(model, "index").setJsFileName("index.js").build();
    }

    /**
     * Загружаем и сразу парсим
     *
     * @param files
     * @return
     */
    @RequestMapping(path = "/loadAndStartParsing", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> startParsing(HttpServletRequest request,
                                               @RequestParam("files") List<MultipartFile> files,
                                               @RequestParam(value = "withoutSaving", required = false) boolean withoutSaving) {

        try {
            boolean fileParsePatternIsExists = false;
            List<MultipartFile> filtredList = new LinkedList<>();
            for (MultipartFile file : files) {
                if(file.getOriginalFilename().contains("xls")
                        || file.getOriginalFilename().contains("xlsx")
                        || file.getOriginalFilename().contains("xml")) {
                    if(file.getOriginalFilename().contains("xml")) {
                        fileParsePatternIsExists = true;
                    }
                    filtredList.add(file);
                }
            }
            if(this.scheduler.getCurrentTask() != null && this.scheduler.getCurrentTask().isRun()) {
                log.error("Текущая задача еще не завершена");
                return ResponseEntity.ok("Текущая задача еще не завершена");
            }
            if(this.scheduler.getCurrentTask() != null && this.scheduler.getCurrentTask().isComplete()) {
                this.scheduler.clearContext();
            }
            if(CollectionUtils.isEmpty(filtredList)) {
                log.error("Файлов для распознавания не найдено");
                return ResponseEntity.ok("Файлов для распознавания не найдено");
            }
            if(filtredList.size() == 1 && fileParsePatternIsExists) {
                log.error("Файлов для распознавания не найдено");
                return ResponseEntity.ok("Файлов для распознавания не найдено");
            }
            if(!fileParsePatternIsExists) {
                log.error("Не найден файл 'parse_pattern.xml'");
                return ResponseEntity.ok("Не найден файл 'parse_pattern.xml'");
            }

            this.scheduler.parseNow(filtredList, withoutSaving);
            return ResponseEntity.ok().body("");
        } catch (Exception e) {
            log.error("", e);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(path = "/loadErrorFile")
    public ResponseEntity<Resource> loadFileErrors(HttpServletRequest request) {

        try {
            if(this.scheduler.getCurrentTask() == null) {
                return ResponseEntity.ok(null);
            }
            Path errors = this.scheduler.getCurrentTask().getSourceManager().getPathToErrors();
            if(errors != null) {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.add("content-disposition", "attachment; filename=" + errors.toFile().getName());
                responseHeaders.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(errors));
                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .contentLength(errors.toFile().length())
                        .body(resource);
            }
        } catch (Exception e) {
            log.error("Error of loading file error.xlsx", e);
        }
        return ResponseEntity.ok(null);
    }

    @RequestMapping(path = "/getReport", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> getReport() {

        try {
            if(this.scheduler.getCurrentTask() != null) {
                String report = StringUtils.join(this.scheduler.getCurrentTask().getReport(), "<br>");
                return ResponseEntity.ok(report.equalsIgnoreCase("") ? "starting..." : report);
            } else {
                ResponseEntity.ok("");
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return  ResponseEntity.ok().body(null);
    }

    @RequestMapping(path = "/getStatusCurrentTask", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> getStatusCurrentTask() {

        try {
            if(this.scheduler.getCurrentTask() != null) {
                if(this.scheduler.getCurrentTask().isRun()) {
                    return ResponseEntity.ok("run");
                }
                if(this.scheduler.getCurrentTask().isComplete()) {
                    return ResponseEntity.ok("complete");
                }
                if(this.scheduler.getCurrentTask().isInterrupted()) {
                    return ResponseEntity.ok("interrupted");
                }
                return ResponseEntity.ok("created");
            } else {
                ResponseEntity.ok("");
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return  ResponseEntity.ok().body(null);
    }


    /**
     * Класс, собирающий отображение по шаблону.
     *
     */
    private class ViewBuilder {

        private Model model;
        private String content;
        private List<String> css;
        private List<String> js;

        public ViewBuilder(Model model, String content) {

            this.model = model;
            this.content = content;
            this.css = new ArrayList<>();
            this.js = new ArrayList<>();
        }

        public ViewBuilder addAttribute(String name, Object obj) {

            this.model.addAttribute(name, obj);
            return this;
        }

        public ViewBuilder setCssFileName(String cssFileName) {

            this.css.add(cssFileName);
            return this;
        }

        public ViewBuilder setJsFileName(String jsFileName) {

            this.js.add(jsFileName);
            return this;
        }

        public String build() {

            model.addAttribute("body", content+".jsp");
            if(CollectionUtils.isNotEmpty(this.css)) {
                model.addAttribute("css", this.css);
            }
            if(CollectionUtils.isNotEmpty(this.js)) {
                model.addAttribute("js", this.js);
            }
            return "/layout/template";
        }
    }
}
