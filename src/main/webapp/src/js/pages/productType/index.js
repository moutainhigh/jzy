/**
 * Created by wangqx on 2016/12/8.
 */
$(document).ready(function () {
    initAddForm();
    //新增
    $("#addBnt").on('click', function () {
        $('#add_type_form').submit();
    });
    //新增取消
    $("#cancelBnt").on("click", function () {
        $("#add_type_modal").modal("hide");
    });

    initEditForm();
    //修改
    $("#editBnt").on('click', function () {
        $('#edit_type_form').submit();
    });

    //修改取消
    $("#editCancelBnt").on('click', function () {
        $("#edit_type_modal").modal('hide');
    });

    $(document).on('click', "#add",function () {
        $("#add_type_form")[0].reset();
        getCode();
    });

    $('#search').on('click', function () {
        filterColumn(0, $("#s_code").val(), 1, $("#s_name").val());
    });

    $("#reset").on("click", function () {
        $("#s_code").val("");
        $("#s_name").val("");
    });
});


var validateOptions = {
    inline: true,
    fields: {
        name: {
            identifier: 'name',
            rules: [
                {
                    type: 'empty',
                    prompt: '名称不能为空'
                },
                {
                    type: 'validateNotSpecial',
                    prompt: '不允许输入特殊字符'
                },

                {
                    type: 'maxLength[50]',
                    prompt: '最多输入50个字符'
                }
            ]
        },
        description: {
            identifier: 'description',
            rules: [
                {
                    type: 'validateNotSpecial',
                    prompt: '不允许输入特殊字符'
                },
                {
                    type: 'maxLength[300]',
                    prompt: '最多输入300个字符'
                }

            ]
        },
        guarantyType: {
            identifier: 'guarantyType',
            rules: [
                {
                    type: 'empty',
                    prompt: '担保类型不能为空'
                }
            ]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};

//修改
function edit(id) {
    var form = $("#edit_type_form");
    $(document).api({
            on: "now",
            url: "/product_type/edit_init",
            method: "post",
            data: {typeId: id},
            onSuccess: function (data) {
                var productType = data.productType;
                $("#editId").val(productType.id);
                $("#editCodeSpan").text(productType.code);
                $("#editName").val(productType.name);
                $("#editDescription").val(productType.description);
                form.find(("input[name='status']")).attr("checked", false);
                form.find(("input[name='parentProductType']")).attr("checked", false);
                form.find(("input[name='businessType']")).attr("checked", false);
                form.find(("input[name='status'][value=" + productType.status + "]"))
                    .attr("checked", true);
                form.find(
                    ("input[name='parentProductType'][value="
                    + productType.parentProductType
                    + "]")).attr("checked", true);
                form.find(
                    ("input[name='businessType'][value=" + productType.businessType
                    + "]"))
                    .attr("checked", true);
                form.find(
                    ("select[name='guarantyType']")).val(productType.guarantyType);
                $("#edit_type_modal").modal({blurring:true}).modal('show');
            },
            onFailure: function (data) {
                $.uiAlert(
                    {
                        type: "danger",
                        textHead: '获取数据错误',
                        text: data.msg,
                        time: 1
                    });
            }
        }
    );
}


// 获取产品大类编码
function getCode() {
    $(document).api({
            on: "now",
            url: "/product_type/get_code",
            method: "post",
            onSuccess: function (data) {
                $("#code").val(data.code);
                $("#codeSpan").text(data.code);
                $("#add_type_modal").modal({blurring:true}).modal("show");
            },
            onFailure: function (data) {
                $.uiAlert(
                    {
                        type: "danger",
                        textHead: '获取编码错误',
                        text: data.msg,
                        time: 1
                    });
            }
        }
    );
}
var dtTable = $("#productTypeTable").DataTable({
    serverSide: false, //服务端分页
    searching: true, //显示默认搜索框
    ordering: false, //开启排序
    "ajax": {
        "url": $(
            document).api.settings.api['get productType'],
        "data": function (d) {
            //$.extend(d, personQuery.data);
        },
        "dataSrc": function (json) {
            for (var i = 0; i < json.data.length;
                 i++) {
                json.data[i] = $.extend({
                        id: 0,
                        code: "",
                        name: "",
                        guarantyType: "",
                        businessType: "",
                        status: ""
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
        data: 'name'
    }, {
        data: 'guarantyType',
        render: function (data) {
            return enums.guaranty_type[data];
        }
    }, {
        data: 'businessType',
        render: function (data) {
            return enums.business_type[data];
        }
    }, {
        data: 'status',
        render: function (data) {
            return enums.public_status[data];
        }
    },], columnDefs: [{
        //指定第最后一列
        className:"single line",
        targets: 5,
        render: function (data, type, row, meta) {
            return "<button onclick=\"edit('" + row.id + "')\" class=\"ui mini basic button \" data-id=" + row.id + "><i class='edit icon'></i>修改</button>";
        }
    }],
    "iDisplayLength": 20,
    "aLengthMenu": [
        [20],
        [20]
    ],
    initComplete:function(){
        $(".right.aligned.eight.wide.column").append("<div class='ui teal small button ' id='add'><i class='plus icon'></i>添加</div>");
    },
    "dom": "<'ui grid'<'row'<'eight wide column'><'right aligned eight wide column'>><'row dt-table'<'sixteen wide column'tr>><'row'<'seven wide column'i><'right aligned nine wide column'p>>>"
});

//搜索
function filterColumn(i, val, j, val1) {
    $('#productTypeTable').DataTable().column(i).search(val).column(j).search(val1).draw();
}

function initAddForm() {
    $('#add_type_form').form(validateOptions).api({
        action: 'save productType',
        method: 'POST',
        serializeForm: true,
        beforeSend:function (settings) {
            for(i in settings.data){
                if(i !='user.roles'){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
            }
            return settings;
        },
        onSuccess: function (data) {
            $.uiAlert(
                {
                    type: "success",
                    textHead: '添加成功',
                    text: '成功添加产品大类',
                    time: 1,
                    onClosed: function () {
                        $('#add_type_modal')
                            .modal(
                                'hide');
                        dtTable.ajax.reload();
                    }
                });
        },
        onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '添加失败',
                    text: data.msg,
                    time: 1
                });
        }
    });
}

function initEditForm() {
    $('#edit_type_form').form(validateOptions).api({
        action: 'edit productType',
        method: 'POST',
        serializeForm: true,
        beforeSend:function (settings) {
            for(i in settings.data){
                if(i !='user.roles'){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
            }
            return settings;
        },
        onSuccess: function (response) {
            $.uiAlert(
                {
                    type: "success",
                    textHead: '修改成功',
                    text: '成功修改产品大类',
                    time: 1,
                    onClosed: function () {
                        $('#edit_type_modal')
                            .modal('hide');
                        dtTable.ajax.reload();
                    }
                });
        },
        onFailure: function (response, element) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '修改失败',
                    text: response.msg,
                    time: 1
                });
        }
    });
}
