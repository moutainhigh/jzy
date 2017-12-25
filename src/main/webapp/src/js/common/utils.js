/**
 * Created by pengs on 2016/11/24.
 */
var utils = {
    /**
     * 封装Mustache渲染模板
     * @param {type} selector
     * @param {type} data
     * @returns {unresolved}
     */
    render: function (selector, data, partial) {
        var template = $(selector).html();
        var partialTemplate = $(partial).html();
        var partials = {
            partial: partialTemplate
        };
        Mustache.parse(template);
        return Mustache.render(template, data, partials);
    },
    /**
     * 是否为字符串
     */
    isString: function isString(str) {
        return (typeof str == 'string') && str.constructor == String;
    },
    /**
     * 获取url参数
     * @param name
     * @returns {*}
     */
    getUrlParam: function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
}

//校验按钮是否有权限
var promise={
    //同步的getJSON方法
    getJsonBySync:function(url,data,callback){
        $.ajax({
            type: "get",
            async: false,
            url: url,
            data: data,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            cache: false,
            success: function (res) {
                callback(res);
            },
            error: function (err) {
                $.uiAlert({
                    type: "danger",
                    textHead: '权限',
                    text: '权限校验失败，请联系管理员！',
                    time: 3
                });
                callback(false);
            }
        });
    },
    //过滤掉没有权限的按钮
    filterPromise:function(object){
        if(object){
            if(!object.eq(0).data("promise"))
                object =  object.find("[data-promise*=':']");
            object.each(function(i,ele){
                if(object[$(ele).data("promise")]==undefined){
                    promise.getJsonBySync('/role/hasPromise',{"promise":$(ele).data("promise")},function(data){
                        object[$(ele).data("promise")] = data;
                        if(data==false){
                            $(ele).remove();
                        }
                    });
                }else{
                    if(!object[$(ele).data("promise")]){
                        $(ele).remove();
                    }
                }
            });
        }
    }
}
function add(a, b) {
    var c, d, e;
    try {
        c = a.toString().split(".")[1].length;
    } catch (f) {
        c = 0;
    }
    try {
        d = b.toString().split(".")[1].length;
    } catch (f) {
        d = 0;
    }
    return e = Math.pow(10, Math.max(c, d)), (mul(a, e) + mul(b, e)) / e;
}

function sub(a, b) {
    var c, d, e;
    try {
        c = a.toString().split(".")[1].length;
    } catch (f) {
        c = 0;
    }
    try {
        d = b.toString().split(".")[1].length;
    } catch (f) {
        d = 0;
    }
    return e = Math.pow(10, Math.max(c, d)), (mul(a, e) - mul(b, e)) / e;
}

function mul(a, b) {
    var c = 0,
        d = a.toString(),
        e = b.toString();
    try {
        c += d.split(".")[1].length;
    } catch (f) {}
    try {
        c += e.split(".")[1].length;
    } catch (f) {}
    return Number(d.replace(".", "")) * Number(e.replace(".", "")) / Math.pow(10, c);
}

function divFloat(a, b) {
    var c, d, e = 0,
        f = 0;
    try {
        e = a.toString().split(".")[1].length;
    } catch (g) {}
    try {
        f = b.toString().split(".")[1].length;
    } catch (g) {}
    return c = Number(a.toString().replace(".", "")), d = Number(b.toString().replace(".", "")), mul(c / d, Math.pow(10, f - e));
}


var SETTINGS = {
    // /**
    //  * 组织架构代码
    //  */
    // ORGANIZITION: {
    //     AGENCY: 1300,
    //     ORGANIZE: 1301,
    //     POSITION: 1302
    // },
    /**
     * 分页器基本模板
     */
    jqPaginator: {
        prev: '<a class="icon item"><i class="left chevron icon"></i></a>',
        next: '<a class="icon item"><i class="right chevron icon"></i></a>',
        page: '<a class="item">{{page}}</a>',
        first: '<a class="item">首页</a>',
        last: '<a class="item">末页</a>',
        wrapper: '<div class="ui right floated pagination menu" ></div>'
    },
    /**
     * datatables默认设置
     */
    datatables: {
        "processing": true,
        language: {
            "sProcessing": "载入中...",
            "sLengthMenu": "每页 _MENU_ 项",
            "sZeroRecords": "没有匹配结果",
            "sInfo": "当前显示第 _START_ 至 _END_ 项，共 _TOTAL_ 项。",
            "sInfoEmpty": "当前显示第 0 至 0 项，共 0 项",
            "sInfoFiltered": "(由 _MAX_ 项结果过滤)",
            "sInfoFiltered": "",
            "sInfoPostFix": "",
            "sSearch": "搜索:",
            "sUrl": "",
            "sEmptyTable": "表中数据为空",
            "sLoadingRecords": "载入中...",
            "sInfoThousands": ",",
            "oPaginate": {
                "sFirst": "首页",
                "sPrevious": "上页",
                "sNext": "下页",
                "sLast": "末页",
                "sJump": "跳转"
            },
            "oAria": {
                "sSortAscending": ": 以升序排列此列",
                "sSortDescending": ": 以降序排列此列"
            }
        },
        "dom": "<'ui grid'<'row'<'eight wide column'><'right aligned eight wide column'>><'row dt-table'<'sixteen wide column'tr>><'row'<'seven wide column'i><'right aligned nine wide column'p>>>"
    }
}

$.extend($.fn.dataTable.defaults, SETTINGS.datatables);
//fix modal bug
$.fn.modal.settings.transition="fade";
$.fn.modal.settings.onHidden=function(){$(this).removeClass("hidden")}
$.fn.modal.settings.observeChanges = true;
//closeable fasle
$.fn.modal.settings.closable=false;


/* 日期格式化 */
//todo 日期格式化有插件moment.js 这个要删掉
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}





//search提示文字中文化
$.fn.search.settings.error = {
    source      : 'Cannot search. No source used, and Semantic API module was not included',
    noResults   : '无匹配内容',
    logging     : 'Error in debug logging, exiting.',
    noTemplate  : 'A valid template name was not specified.',
    serverError : '后台通信错误',
    maxResults  : 'Results must be an array to use maxResults setting',
    method      : 'The method you called is not defined.'
}

//dropdown提示文字中文化
$.fn.dropdown.settings.message = {
    addResult: "Add <b>{term}</b>",
    count: "{count} selected",
    maxSelections: "Max {maxCount} selections",
    noResults: "无匹配内容.",
    serverError: "后台通信错误"
}
$.fn.dropdown.settings.error = {
    action: "You called a dropdown action that was not defined",
        alreadySetup: "Once a select has been initialized behaviors must be called on the created ui dropdown",
        labels: "Allowing user additions currently requires the use of labels.",
        missingMultiple: "<select> requires multiple property to be set to correctly preserve multiple values",
        method: "The method you called is not defined.",
        noAPI: "The API module is required to load resources remotely",
        noStorage: "Saving remote data requires session storage",
        noTransition: "This module requires ui transitions <https://github.com/Semantic-Org/UI-Transition>"
}
//ajax session失效跳转
$(document).on("ajaxError",function(event,jqxhr,settings,thrownError){
    if (jqxhr.getResponseHeader("sessionStatus")==="false"){
        window.location.href="/user/toLogin";
    }
});