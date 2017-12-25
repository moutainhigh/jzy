/**
 * created by cobantou
 * based on semantic.js ,common/utils.js
 */
;
(function(window, $, undefined) {
    $.uiDialog = function(text, options) {
        if(!utils.isString(text)) {
            console.error("uiDialog first argument must be a string!");
            return;
        }
        var t = text ? text : "";
        var defaults = {
            blurring: true,
        };

        function render(t) {
            var template = "<div class='ui basic  modal js-dialog'>" +
                "  <div class='content'>" +
                " <div class='center '>" +
                " <h3>" + t + "</h3>" +
                " </div>" +
                "  </div>" +
                "  <div class='actions'>" +
                "    <div class='ui black cancel    button'>" +
                "取消" +
                "</div>" +
                "<div class='ui ok blue   button'>" +
                "确定" +
                " </div>" +
                "</div>" +
                " </div>";
            $(".js-dialog").remove();
            return $(template).appendTo("body");
        }

        var $dialog = render(t);
        return $dialog
            .modal($.extend(true, defaults, options))
            .modal("show");
    }
})(window, jQuery)