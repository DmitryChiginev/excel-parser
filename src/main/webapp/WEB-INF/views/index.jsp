<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<div class="container">
    <div class="row" style="padding-top: 5px;">
        <div class="col-lg-12" style="padding: 3px">
            <div style="width: 100%; padding: 10px; border-radius: 5px; background: #ececec;">
                <label class="label">Распознать файлы</label>
                <p>
                    Выберите файлы для распазнования
                    (убедитесь, что файл parse_pattern находится среди выбранных)
                </p>

                <div style="padding: 10px; border-radius: 8px; border: 1px solid #b3b0b0;">
                    <div>
                        <input id="loadFiles" type="file" name="files" multiple accept="application/vnd.sealed-xls">
                        <div style="margin: 5px;"></div>
                        <button id="btnUpload" class="btn btn-dark" value="Загрузить" onclick="submitForm('/loadAndStartParsing')">Загрузить</button>
                        <button id="btnUpload2" class="btn btn-dark" value="Загрузить без записи в БД" onclick="submitForm('/loadAndStartParsing?withoutSaving=true')">Загрузить без записи в БД</button>
                        <a id="btnLoadError" class="btn btn-success" href="/loadErrorFile" style="display: none">Скачать файл ошибок</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row" style="padding-top: 5px;">
        <div class="col-lg-12" style="padding: 3px">
            <div style="height: 400px; width: 100%; padding: 10px; border-radius: 5px; background: #ececec;">
                <div id="log" style="border-radius: 5px; background: #1b1e21; height: 100%; color: #f2f2f2; padding: 5px; overflow-y: auto;">

                </div>
            </div>
        </div>
    </div>
</div>
