$(function () {
    // 跳转返回影像资料列表
    $("body").on("click", ".backButton", function () {
        location.href = "/media_temp/index";//location.href实现客户端页面的跳转
    })
    // 设置影像资料Tab
    $('.viewDateMenu .item').tab();

    // 初始化影像资料的datatable
    initDatatables();
    //form初始化
    submitForm();
    // })
})

function initDatatables() {
    // 初始化业务影像datatable
    init_Datatables("#y_dataTabel","#y_addButton","BUSINESS","Y");
    // 初始化风控影像datatable
    init_Datatables("#f_dataTabel","#f_addButton","RISK","F");
    // 初始化财务影像datatable
    init_Datatables("#c_dataTabel","#c_addButton","FINANCE","C");
    // 初始化贷后影像datatable
    init_Datatables("#d_dataTabel","#d_addButton","POST_LOAN","D");
}
//datatables公用方法
function init_Datatables(datatableName,addButton,mediaItemType,codeType) {
    $(document).ready(function () {
        var counter = 1;
        var table = $(datatableName).DataTable({
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            iDisplayLength: 50,

        });
        //添加行
        $(addButton).on('click', function () {
            table.row.add([
                '<div class="ui fluid input"><input type="text" name="yx_code" placeholder="业务员代码" readonly="readonly" id="code" value="' + getCode(counter,codeType) + '"></div>',
                '<div class="ui fluid input"><input type="text"  placeholder="请输入正确的资料名称"  name="yx_name" maxlength="50"/></div> ',
                /*'<div class="ui fluid input"><input type="text" id="yx_mediaItemType"  name="yx_mediaItemType" value="'+mediaItemType+'" readonly="readonly"/></div>',*/
                '<div class="ui fluid input field"><select class="ui fluid dropdown" id="required" name="yx_required"><option value="true" selected="selected" >是</option> <option value="false">否</option></select></div> ',
                '<button  class="ui mini teal button js-del"><i class="Info Circle icon" id="del_' + counter + '"></i>删除</button>',
            ]).draw();
            counter++;
        });
        // 删除行
        $(datatableName+' tbody').on('click', 'button.js-del', function (e) {
            e.preventDefault();
            if(table.data().length>1){
                table.row($(this).parents('tr')).remove().draw();
            }
        })
        $(addButton).click();
    });
}

//表单
function submitForm() {
    var $form =$(".ui.js-saveForm");
    $form.form({
        inline: true,
        fields: {
            name: {
                identifier: 'mediaTmpl.name',
                rules: [
                    {
                        type: 'maxLength[50]',
                        prompt: '{name}不能超过50个字符'
                    }
                ]
            },
            description: {
                identifier: 'mediaTmpl.description',
                rules: [
                    {
                        type: 'maxLength[300]',
                        prompt: '{name}不能超过300个字符'
                    }
                ]
            },
            status: {
                identifier: 'mediaTmpl.status',
                rules: [
                    {
                        type: 'empty',
                        prompt: '请选择状态'
                    }
                ]
            },yx_name: {
                identifier: 'yx_name',
                rules: [
                    {
                        type: 'empty',
                        prompt: '不能为空'
                    }
                ]
            },
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "add mediaTemp",
        method: 'POST',
        serializeForm: true,
        data: {
            "mediaTmpl.status": $("#mediaTmpl.status").val(),
        },
        beforeSend: function (settings) {
            var data = getValues();
            console.log(data);
            settings.data.jsonObject = data;
            return settings;
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead: '新增成功',
                text: '成功新增影像资料配置模板',
                time: 1,
                onClosed: function () {
                    location.href = "/media_temp/index";//location.href实现客户端页面的跳转
                }
            });
        },
        onFailure: function (response, element) {
            $.uiAlert({
                type: "danger",
                textHead: '新增失败',
                text: response.msg,
                time: 1,
                onClosed: function () {

                }
            });
        }
    })

}

//获取影像资料code
function getCode(counter,type) {
    var code = "";
    if (counter < 10) {
        code = type+"0" + counter;
    } else {
        code = type + counter;
    }
    return code;
}

// 组装影像资料数据
function getValues() {

    var items=[];

    if($("#y_dataTabel").DataTable().data().length>0){
        var item =[];
        // 循环获取业务影像资料
        $y_tr = $("#y_dataTabel tbody tr");
        $y_tr.each(function(index,ele){
            console.log("index="+index);
            if($(ele).find("input[name='yx_name']").val()!=""){
                item = {
                    code:$.trim($(ele).find("input[name='yx_code']").val()),
                    name:$.trim($(ele).find("input[name='yx_name']").val()),
                    mediaItemType:"BUSINESS",//$(ele).find("input[name='yx_mediaItemType']").val(),
                    required:$(ele).find("select[name='yx_required']").val()
                }
                items.push(item);
            }
        });
    }
    // 循环获取风控影像资料
    if($("#f_dataTabel").DataTable().data().length>0) {
        // 循环获取业务影像资料
        $f_tr = $("#f_dataTabel tbody tr");
        $f_tr.each(function (index, ele) {
            if ($(ele).find("input[name='yx_name']").val() != "") {
                item = {
                    code:$.trim($(ele).find("input[name='yx_code']").val()),
                    name:$.trim($(ele).find("input[name='yx_name']").val()),
                    mediaItemType: "RISK",//$(ele).find("input[name='yx_mediaItemType']").val(),
                    required: $(ele).find("select[name='yx_required']").val()
                }
                items.push(item);
            }
        });
    }
        // 循环获取财务影像资料
        if ($("#c_dataTabel").DataTable().data().length > 0) {
            // 循环获取业务影像资料
            $c_tr = $("#c_dataTabel tbody tr");
            $c_tr.each(function (index, ele) {
                if ($(ele).find("input[name='yx_name']").val() != "") {
                    item = {
                        code:$.trim($(ele).find("input[name='yx_code']").val()),
                        name:$.trim($(ele).find("input[name='yx_name']").val()),
                        mediaItemType: "FINANCE",//$(ele).find("input[name='yx_mediaItemType']").val(),
                        required: $(ele).find("select[name='yx_required']").val()
                    }
                    items.push(item);
                }
            });
        }
            // 循环获取贷后影像资料
            if ($("#d_dataTabel").DataTable().data().length > 0) {
                // 循环获取业务影像资料
                $d_tr = $("#d_dataTabel tbody tr");
                $d_tr.each(function (index, ele) {
                    if ($(ele).find("input[name='yx_name']").val() != "") {
                        item = {
                            code:$.trim($(ele).find("input[name='yx_code']").val()),
                            name:$.trim($(ele).find("input[name='yx_name']").val()),
                            mediaItemType: "POST_LOAN",//$(ele).find("input[name='yx_mediaItemType']").val(),
                            required: $(ele).find("select[name='yx_required']").val()
                        }
                        items.push(item);
                    }
                });
            }
            return JSON.stringify(items);
}
