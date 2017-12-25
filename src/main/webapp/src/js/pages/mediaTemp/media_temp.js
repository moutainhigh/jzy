
var mediaTmpl={}
var dtTable;

// 事件管理
function bindEvents() {
    $("body").on("click", ".addButton", function () {
        mediaTmpl.add(0);
    })
    $("body").on("click", ".resetButton", function () {
        mediaTmpl.reset(0);
    })
    $("body").on("click", ".searchButton", function () {
        mediaTmpl.search(0);
    })
    $("body").on("click", ".updateButton", function () {
        var id = $(this).data("id");
       mediaTmpl.edit(id);
    })
    $("body").on("click", ".viewButton", function () {
        var id = $(this).data("id");
        mediaTmpl.view(id);
    })
}
//加载列表
function init() {

    dtTable = $("#mediaTempTable").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax": {
            "url":$(document).api.settings.api['query mediaTemp'],
            "type": "POST",
            "data": function (d) {
                var data = {};
                data.code=$("#code").val();
                data.name=$("#name").val();
                data.status=$("#status").val();
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            }
        },
        "columns": [
            {data: 'code'},
            {data: 'name'},
            {data: 'description'},
            {
                data: 'status',
                render: function (data) {
                    return enums.status[data];
                }
            }
        ]
        ,columnDefs: [{
            //   指定第最后一列
            targets: 4,
            render: function (data,type, row, meta) {
                return '<div class="ui mini basic button viewButton" data-id="' + row.id + '"><i class="Info Circle icon"></i>查看</div>' + '<div  class="ui mini basic button updateButton" data-id="' + row.id + '"><i class="edit icon"></i>编辑</div>';
            }
        }],
        "iDisplayLength": 10,
        initComplete: function () {
            $(".right.aligned.eight.wide.column").append($("#addOrgTemplate").html());
        }
    })
}
/**
 * 搜索
 */
$(function (){
    $.extend(mediaTmpl, {
        search:function () {
            dtTable.ajax.reload();
        }
    })
})

/**
 * 重置
 */
$(function (){
    $.extend(mediaTmpl, {
        reset:function () {
            $("#name").val("");
            $("#code").val("");
            $("#status").val("")
            dtTable.ajax.reload();
        }
    })
})

/**
 * 跳转到新增影像资料页面
 */
$(function (){
    $.extend(mediaTmpl, {
        add: function () {
            location.href = "/media_temp/add";//location.href实现客户端页面的跳转
        }
    })
})

/**
 * 跳转到修改影像资料页面
 */
$(function (){
    $.extend(mediaTmpl, {
        edit: function (id) {
            location.href = "/media_temp/edit?id="+id;//location.href实现客户端页面的跳转

        }
    })
})
/**
 * 跳转到查看影像资料页面
 */
$(function (){
    $.extend(mediaTmpl, {
        view: function (id) {
            location.href = "/media_temp/view?id="+id;//location.href实现客户端页面的跳转
        }
    })
})