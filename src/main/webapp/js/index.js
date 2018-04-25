var files = [];
$(document)
    .on(
        "change",
        "#loadFiles",
        function(event) {
            files=event.target.files;
        });

function submitForm(url) {
    $("#btnUpload").prop('disabled', true);
    $("#btnUpload2").prop('disabled', true);
    $("#btnLoadError").css('display', 'none');
    var data = new FormData();
    for(var i = 0; i < files.length; i++) {
        data.append('files', files[i]);
    }
    $.ajax({
        url: url,
        data: data,
        cache: false,
        enctype: 'multipart/form-data',
        contentType: false,
        processData: false,
        method: 'POST',
        success: function(data){
            $("#btnUpload").prop('disabled', false);
            $("#btnUpload2").prop('disabled', false);
            $("#log").html(data);
            if(data === '') {
                startWatch();
            }
        }
    });

}

function startWatch() {
    var interval = setInterval( function () {
        var report = "empty";
        $.ajax({
            url: '/getReport',
            method: 'GET',
            success: function(data){
                if(data === "") {
                    return;
                }
                if(report !== data) {
                    $('#log').html(data);
                    report = data;
                }
            }
        });

        $.ajax({
            url: '/getStatusCurrentTask',
            method: 'GET',
            success: function(data){
                if(data === "") {
                    return;
                }
                if(data === 'run') {
                    $("#btnUpload").prop('disabled', true);
                    $("#btnUpload2").prop('disabled', true);
                    $("#btnLoadError").css('display', 'none');
                }
                if(data === 'complete' || data === 'interrupted') {
                    $("#btnUpload").prop('disabled', false);
                    $("#btnUpload2").prop('disabled', false);
                    $("#btnLoadError").css('display', 'inline-block');
                    clearInterval(interval);
                }
            }
        });

    }, 500);
}
