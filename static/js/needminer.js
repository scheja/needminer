$(document).ready(function() {
    var needs = new Array();
    var currentSelection;

    $('#tweet-text').bind('updateInfo keyup mousedown mousemove mouseup', function(event) {
        if (document.activeElement !== $(this)[0]) {
            return;
        }
        var range = $(this).textrange();
        $("#preview").text(range.text);
        currentSelection = {
            start: range.start,
            end: range.end
        };
        console.log(currentSelection);

    });

    $("#mark-need").click(function () {
        console.log(currentSelection);
        needs.push(currentSelection);
        $("#needs").val(JSON.stringify(needs));

        var $alert = $('<div class="alert alert-success alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button> ' +
            '<span class="text"></span> was marked as need.</div>');

        $alert.find("span.text").text($("#preview").text());

        console.log($alert);

        $("#alerts").append($alert);
    });

});