/**
 * Created by pengs on 2016/11/27.
 */
/**
 * base on semantic,ui-alert.css;
 * origin user:diw112
 * project:https://diw112.github.io/semanticUiAlert/
 * modifie by :cobantou
 */
$.uiAlert = function(options) {
    var setUI = $.extend({
        textHead: 'Your user registration was successful.',
        text: '系统出错，请联系维护人员',
        textcolor: '#fff',
        type: 'success',
        position: 'top-right',
        icon: '',
        time: 5,
        permanent: false,
        onClosed:function(){

        }
    }, options);

    if(setUI.icon == "") {
        switch(setUI.type) {
            case "success":
                setUI.icon = "checkmark box";
                break;
            case "info":
                setUI.icon = "info circle";
                break;
            case "warning":
                setUI.icon = "warning sign";
                break;
            case "danger":
                setUI.icon = "remove circle";
                break;
            default:
                break;
        }
    }

    var ui_alert = 'ui-alert-content';
    ui_alert += '-' + setUI.position;
    if(!$('body > .' + ui_alert).length) {
        $('body').append('<div class="dimmer ui-alert-content ' + ui_alert + '" style="width: inherit;"></div>');
    }
    var message = $('<div id="messages" class="ui icon message ' + setUI.type + '"><i class="' + setUI.icon + ' icon" style="color: ' + setUI.textcolor + ';"></i><i class="close icon" style="color: ' + setUI.textcolor + ';" id="messageclose"></i><div style="color: ' + setUI.textcolor + '; margin-right: 10px;">   <div class="header" style="color: ' + setUI.textcolor + ';">' + setUI.textHead + '</div>  <p> ' + setUI.text + '</p></div>  </div>');
    $('.' + ui_alert).prepend(message);
    message.animate({
        opacity: '1',
    }, 300);
    if(setUI.permanent === false) {
        var timer = 0;
        $(message).mouseenter(function() {
            clearTimeout(timer);
        }).mouseleave(function() {
            uiAlertHide();
        });
        uiAlertHide();
    }

    function uiAlertHide() {
        timer = setTimeout(function() {
            message.animate({
                opacity: '0',
            }, 300, function() {
                message.remove();
                setUI.onClosed();
            });
        }, (setUI.time * 1000));
    }

    $('#messageclose')
        .on('click', function() {
            $(this)
                .closest('#messages')
                .transition('fade');
        });

};