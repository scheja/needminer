$(document).ready(function() {
    $("#mark-need").click(function () {
        $("#tag").val("need");
        $(".btn-success").attr("disabled", "disabled");
        $(this).text("Please be patient...");
        $("#mark-need-form").submit();
    });

    $("#mark-nothing").click(function () {
        $("#tag").val("nothing");
        $(".btn-success").attr("disabled", "disabled");
        $(this).text("Please be patient...");
        $("#mark-need-form").submit();
    });

    $("#start-button").click(function () {
        console.log("clicked start");
        $(this).text("Please be patient...").attr("disabled", "disabled");
        $("#start-form").submit();
    });

    $('#emobilityKnowledge').slider({
        formatter: function(value) {
            return 'Current value: ' + value;
        }
    });

    $('#twitterKnowledge').slider({
        formatter: function(value) {
            return 'Current value: ' + value;
        }
    });

});