$(function(){
    $("#indiegogo a").on("click", function(ev) {
        //$("#indiegogo").putToBack();
    });
    $("#start a").on("click", function(ev) {
        //$("#start").putToBack();
    });
    $(".again").on("click", function(ev) {
        $(this).parent().putToBack();
    });
});

$.fn.putToBack = function() {
    var $this = $(this);
    //var index = $this.css("z-index");
    $this.animate({"left":"10000px"}, 500);
};
