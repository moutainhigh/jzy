/**
 * Created by pengyueyang on 2016/12/12.
 */
$.fn.dataTable.ext.search.push(
    function (settings, data, dataIndex) {
        var searchTime = $("#s_create_time").val();
        if (searchTime == "") {
            return true;
        }
        var times = searchTime.split("~");
        var startTime = times[0] + " 00:00:00";
        var endTime = times[1] + " 23:59:59";
        var createTime = strToDate(data[4]);
        if (startTime != "" && endTime != "" && createTime >= strToDate(startTime) && createTime <= strToDate(endTime)) {
            return true;
        }
        return false;
    }
);

var productType = new Object();

function strToDate(str) {
    return new Date(str.replace(/-/g, "/"));
}
$(document).ready(function () {
    $("#s_type>option").each(function () {
        productType[this.value] = $(this).text();
    });

    //获取数据
    initTable();

    //重置
    $("#resetBnt").on("click", function () {
        reset();
    });

    //添加
    $(document).on("click","#addBnt", function () {
        showAdd();
    });

    $('#searchBnt').on('click', function () {
        filterColumn(1, $("#s_type option:selected").attr("data_name"), 2, $("#s_name").val());
    });

    $("#s_create_time").dateRangePicker({maxDays: 360, separator: '~'});

});


function initTable() {
    dtTable = $("#productList").DataTable({
        serverSide: false, //服务端分页
        searching: true, //显示默认搜索框
        ordering: false, //开启排序
        "ajax": {
            "url": $(
                document).api.settings.api['get productList'],
            "dataSrc": function (json) {
                for (var i = 0; i < json.data.length;
                     i++) {
                    json.data[i] = $.extend({
                            id: 0,
                            code: "",
                            typeId: "",
                            name: "",
                            alias: "",
                            createTime: "",
                            status: "",
                            flowConfigStatus: ""
                        },
                        json.data[i]);
                }
                return json.data
            },
            "type": "POST"
        },
        columns: [{
            data: 'code'
        }, {
            data: 'typeId',
            render: function (data) {
                return productType[data];
            }
        }, {
            data: 'name'
        }, {
            data: 'alias'
        }, {
            data: 'createTime'
        }, {
            data: 'status',
            render: function (data) {
                return enums.public_status[data];
            }
        }], columnDefs: [{
            //指定第最后一列
            className:"single line",
            targets: 6,
            render: function (data, type, row, meta) {
                var startBtn = "<div onclick=\"start('" + row.id + "')\" class=\"ui mini submit button basic\" id='start_" + row.id + "' >启用</div>";
                var changeBtn = "<div onclick=\"edit('" + row.id + "')\" class=\"ui mini basic submit button \" id='change_" + row.id + "'><i class='edit icon'></i>修改</div>";
                if (row.flowConfigStatus) {
                    changeBtn = "<div onclick=\"view('" + row.id + "')\" class=\"ui mini basic submit button \" id='change_" + row.id + "'><i class='info circle icon'></i>查看</div>"
                    startBtn = "<div onclick=\"stop('" + row.id + "')\" class=\"ui mini submit button\" id='start_" + row.id + "' >禁用</div>";
                }
                return changeBtn + startBtn;
            }
        }],
         "createdRow": function ( row, data, index ) {
            if(!data.flowConfigStatus){
                setTdStatus("disabled",$(row.lastChild));
            }
        },
        "iDisplayLength": 20,
        "aLengthMenu": [
            [20],
            [20]
        ],
        initComplete:function(){
            $(".right.aligned.eight.wide.column").append("<div class='ui teal small button ' id='addBnt'><i class='plus icon'></i>添加</div>");
        },
        "dom": "<'ui grid'<'row'<'eight wide column'><'right aligned eight wide column'>><'row dt-table'<'sixteen wide column'tr>><'row'<'seven wide column'i><'right aligned nine wide column'p>>>"
    });
}

function reset() {
    $("#s_type").val("");
    $("#s_name").val("");
    $("#s_create_time").val("");
}

function showAdd() {
    window.location.href = "/product/add_init";
}

function edit(id) {
    window.location.href = "/product/edit_init?id=" + id + "";
}

function view(id) {
    window.location.href = "/product/view?id=" + id + "";
}

//搜索
function filterColumn(i, val, j, val1) {
    $('#productList').DataTable().column(i).search(val).column(j).search(val1).draw();
}
//启用
function start(id) {
    if (id && id != "") {
        $.uiDialog("你确定要启动这个产品？", {
            onApprove: function () {
                $(document).api({
                        on: "now",
                        url: "/product/start",
                        method: "post",
                        data: {
                            "productId": id
                        },
                        onSuccess: function (data) {
                            var obj = $(("#start_" + id));
                            obj.text("禁用");
                            obj.attr("onclick", "stop('" + id + "')");
                            obj.removeClass("basic");
                            var objChange = $(("#change_" + id));
                            objChange.html("<i class='edit icon'></i>查看");
                            objChange.attr("onclick", "view('" + id + "')");
                            setTdStatus("able", obj.parent("td"));
                            $.uiAlert({
                                type: "success",
                                textHead: '启用成功',
                                text: data.msg,
                                time: 1
                            });
                        },
                        onFailure: function (data) {
                            $.uiAlert({
                                type: "danger",
                                textHead: '启用失败',
                                text: data.msg,
                                time: 1
                            });
                        }
                    }
                );
            },
            onDeny: function () {
            }
        })
    }
}


function stop(id) {
    $.uiDialog("确定禁用？", {
        onApprove: function () {
            var $modal = $(this);
            $(document).api({
                on: "now",
                method: 'post',
                url: "/product/stop",
                data: {
                    "productId": id
                },
                onSuccess: function (response) {
                    $modal.modal({
                        onHidden: function () {
                            if (response.ok) {
                                var obj = $(("#start_" + id));
                                obj.text("启用");
                                obj.attr("onclick", "start('" + id + "')");
                                obj.addClass("basic");
                                var objChange = $(("#change_" + id));
                                objChange.html("<i class='edit icon'></i>修改");
                                objChange.attr("onclick", "edit('" + id + "')");
                                setTdStatus("disabled",obj.parent("td"));
                                $.uiAlert({
                                    type: "success",
                                    textHead: '禁用成功',
                                    text: '删除成功',
                                    time: 1
                                });
                            } else {
                                $.uiAlert({
                                    type: "danger",
                                    textHead: '禁用失败',
                                    text: response.msg,
                                    time: 1
                                });
                            }
                        }
                    }).modal("hide");
                }
            });
            return false;
        },
        onDeny: function () {
        }
    })
}


function setTdStatus(status,obj) {
    if (status==='disabled') {
        obj.siblings("td").addClass("disabled");
    }
    if (status==='able') {
        obj.siblings("td").removeClass("disabled");
    }

}