/**
 * Created by wangqx on 2016/12/14.
 */
//验证option
var repayMethods = [{key:"INTEREST_MONTHS", value:"先息后本（按月）"},
    {key:"INTEREST_DAYS",value:"先息后本（按天）"},
    {key:"EQUAL_INSTALLMENT", value:"等额本息"},
    {key:"BULLET_REPAYMENT", value:"一次性还本付息"},
    {key:'INTEREST_SEASONS',value:"按季付息，到期还本"}];
var feeTable;

$.fn.form.settings.rules.maxNum = function (value,args) {
    if(parseFloat(value) <= args){
        return true;
    }else{
        return false;
    }
};

var feeFormValidateOption =
{
    inline: true,
    fields: {
        feeType: {
            identifier: 'fee.feeType',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择费用类型'
                }
            ]
        },
        feeCycle: {
            identifier: 'fee.feeCycle',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择收费频率'
                }
            ]
        },
        chargeType: {
            identifier: 'fee.chargeType',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择收取方式'
                }
            ]
        },
        chargeBase: {
            identifier: 'fee.chargeBase',
            rules: [
                {
                    type: 'empty',
                    prompt: '请填写正确的金额或者比例'
                },
                {
                    type: 'canBeDecimal[10]',
                    prompt : '请填写正确的金额或者比例（最多10位小数）'
                },{
                    type:'maxNum[99999999.99]',
                    prompt:'金额或比例不超过99999999.99'
                }
            ]
        },
        feeChargeNode: {
            identifier: 'fee.feeChargeNode',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择正确的收费节点'
                }
            ]
        },
        repayMethod: {
            identifier: 'fee.repayMethod',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择正确的还款方式'
                }
            ]
        },
        minFeeAmount: {
            identifier: 'fee.minFeeAmount',
            rules: [
                {
                    type: 'empty',
                    prompt: '请填写正确的最低费用'
                },
                {
                    type: 'validateNumFloat[0-99999999.99]',
                    prompt: '请填写正确的最低费用（最多两位小数，金额小于99999999.99）'
                }
            ]
        },
        status: {
            identifier: 'fee.status',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择是否生效'
                }
            ]
        },
        channelType:{
            identifier:'fee.channelType',
            rules:[
                {
                    type:'empty',
                    prompt:'请选择正确的业务来源'
                }
            ]
        }
    }
}
$(document).ready(function () {

    if ($("#updateProductId").val() != "") {
        feeTable = initFeeTable($("#updateProductId").val());
    }

    $(document).on("click", "#addFee", function() {
        $("#addFeeForm")[0].reset();
        //还款方式
        initRepayMethod("addRepayMethod",true);
        $("#addFeeModal").modal("show");

        //业务来源
        changeChannelType($('#addFeeType').val(),'addChannelType','0')
    });
    //添加费用
    initAddFeeForm();

    $("#addFeeBtn").on("click", function () {
        $("#feeProductId").val($("input[name='product.id']").val());
        $("#feeCode").val($("input[name='product.code']").val());
        $("#addFeeForm").submit();
    });

    $("#cancelAddFeeBtn").on("click", function () {
        $("#addFeeModal").modal("hide");
    });

    //更新费用
    initUpdateFeeForm();

    $("#updateFeeBtn").on("click", function () {
        $("#updateFeeForm").submit();
    });

    $("#cancelUpdateFeeBtn").on("click", function () {
        $("#updateFeeModal").modal("hide");
    });

    $("#addChargeType").on("change",function() {
        var value = $(this).val();
        if (value==="FIXED_AMOUNT" || value==="LOAN_REQUEST_INPUT") {
            $("#addMinFeeAmount").attr("readOnly",'true');
            $("#addMinFeeAmount").val("0");
            $("#addMaxFeeAmount").attr("readOnly",'true');
            $("#addMaxFeeAmount").val("99999999");
        }else {
            $("#addMinFeeAmount").removeAttr("readOnly");
            $("#addMaxFeeAmount").removeAttr("readOnly");
        }
    });

    $("#chargeType").on("change",function() {
        var value = $(this).val();
        if (value==="FIXED_AMOUNT" || value==="LOAN_REQUEST_INPUT") {
            $("#minFeeAmount").attr("readOnly",'true');
            $("#minFeeAmount").val("0");
            $("#maxFeeAmount").attr("readOnly",'true');
            $("#maxFeeAmount").val("99999999");
        }else {
            $("#minFeeAmount").removeAttr("readOnly");
            $("#maxFeeAmount").removeAttr("readOnly");
        }
    });

    $("#addFeeType").on("change",function() {
       var value = $(this).val();
        changeRepayMethod(value,"addRepayMethod");
        changeChannelType(value,'addChannelType','0');
    });

    $("#feeType").on("change",function() {
        var value = $(this).val();
        changeRepayMethod(value,"updateRepayMethod");
        changeChannelType(value,'channelType','0');
    });

});


function changeChannelType(value,id,typeVal){
    var obj = $(("#"+id));
    var _all = obj.find("option[value='ALL']");
    var _else = obj.find("option[value='0'],option[value='1']");
    if(value =='OVERDUE_FEE' || value =='PREPAYMENT_FEE'){
        _all.attr('disabled',true);
        _else.attr('disabled',false);
        obj.val(typeVal);
    }else{
        _all.attr('disabled',false);
        _else.attr('disabled',true);
        obj.val('ALL');
    }
}

function changeRepayMethod(value,id) {
    var obj = $(("#"+id));
    if(value==="OVERDUE_FEE"||value==="PREPAYMENT_FEE") {
        obj.children("option[value='ALL']").remove();
        obj.children("option").attr("disabled",false);
    } else {
        obj.children("option").attr("disabled",true);
        if (obj.children("option").first().val()!="ALL") {
            obj.prepend('<option value="ALL">全部</optin>');
        }
        obj.children("option").first().attr("disabled",false);
        obj.val("ALL");
    }
}

//初始化费用添加from
function initAddFeeForm() {
    var maxFeeAmountRule= {
        identifier: 'fee.maxFeeAmount',
            rules: [
            {
                type: 'empty',
                prompt: '请填写正确的最高费用'
            }, {
                type: 'validateNumFloat[0-99999999.99]',
                prompt: '请填写正确的最高费用（最多两位小数，金额小于99999999.99）'
            }, {
                    type: 'greater[addMinFeeAmount]',
                    prompt: '最高费用不能低于最低费用'
                }
        ]
    }
    feeFormValidateOption.fields.maxFeeAmount = maxFeeAmountRule;
    $("#addFeeForm").form(feeFormValidateOption).api({
        url: "/product_fee/add",
        serializeForm: true,
        method: "post",
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
            $("#addFeeModal").modal("hide");
            $.uiAlert(
                {
                    type: "success",
                    textHead: '添加成功',
                    text: data.msg,
                    time: 1
                });
            feeTable.ajax.reload();
        }, onFailure: function (data) {
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


//初始化费用修改from
function initUpdateFeeForm() {
    var maxFeeAmountRule= {
        identifier: 'fee.maxFeeAmount',
        rules: [
            {
                type: 'empty',
                prompt: '请填写正确的最高费用'
            },
            {
                type: 'validateNumFloat[0-99999999.99]',
                prompt: '请填写正确的最高费用（最多两位小数，金额小于99999999.99）'
            }, {
                type: 'greater[minFeeAmount]',
                prompt: '最高费用不能低于最低费用'
            }
        ]
    }
    feeFormValidateOption.fields.maxFeeAmount = maxFeeAmountRule;
    $("#updateFeeForm").form(feeFormValidateOption).api({
        url: "/product_fee/update",
        serializeForm: true,
        method: "post",
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
            $("#updateFeeModal").modal("hide");
            $.uiAlert(
                {
                    type: "success",
                    textHead: '修改成功',
                    text: data.msg,
                    time: 1
                });
            feeTable.ajax.reload();
        }, onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '修改失败',
                    text: data.msg,
                    time: 1
                });
        }
    });
}

//更新费用
function editFee(feeId) {
    initRepayMethod("updateRepayMethod",false);
    $(document).api({
        on: "now",
        url: "/product_fee/get_by_id",
        data: {id: feeId},
        method: "post",
        onSuccess: function (data) {
            var fee = data.fee;
            //还款方式
            $("#updateFeeForm")[0].reset();
            $("#feeId").val(fee.id);
            $("#feeType").val(fee.feeType);
            $("#feeCycle").val(fee.feeCycle);
            $("#chargeType").val(fee.chargeType);
            $("#chargeBase").val(fee.chargeBase);
            $("#chargeNode").val(fee.chargeNode);
            $("#minFeeAmount").val(fee.minFeeAmount);
            $("#maxFeeAmount").val(fee.maxFeeAmount);
            $("#updateRepayMethod").val(fee.repayMethod);
            $("#updateFeeProductId").val(fee.productId);
            $("#channelType").val(fee.channelType);
            changeRepayMethod(fee.feeType,"updateRepayMethod");
            if(fee.channelType =='ALL' && (fee.feeType == 'OVERDUE_FEE' || fee.feeType == 'PREPAYMENT_FEE')){
                changeChannelType(fee.feeType,"channelType",'0');
            }else{
                changeChannelType(fee.feeType,"channelType",fee.channelType);
            }

            if (fee.chargeType==="FIXED_AMOUNT" || fee.chargeType==="LOAN_REQUEST_INPUT") {
                $("#minFeeAmount").attr("readOnly", 'true');
                $("#maxFeeAmount").attr("readOnly", 'true');
            }
            $("#updateFeeModal").modal("show");
        }, onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '后台处理错误',
                    text: data.msg,
                    time: 1
                });
        }
    });
}

function initFeeTable(productId) {

    return $("#productFeeTable").DataTable({
        serverSide: false, //服务端分页
        searching: true, //显示默认搜索框
        ordering: false, //开启排序
        "ajax": {
            "url": '/product_fee/list',
            "data": {"productId": productId},
            "dataSrc": function (json) {
                for (var i = 0; i < json.data.length; i++) {
                    json.data[i] = $.extend({
                            id: 0,
                            code: "",
                            feeType: "",
                            feeCycle: "",
                            chargeType: "",
                            chargeNode: "",
                            chargeBase: "",
                            repayMethod:"",
                            channelType:""
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
            data: 'feeType',
            render: function (data) {
                return enums.feeType[data];
            }
        }, {
            data: 'feeCycle',
            render: function (data) {
                return enums.feeCycleType[data];
            }
        }, {
            data: 'chargeType',
            render: function (data,type,row) {
                var daysText = "";
                if (row.feeType=="OVERDUE_FEE" || row.feeType=="PREPAYMENT_FEE") {
                    daysText = "*天数";
                }
                return enums.feeChargeType[data]+daysText;
            }
        }, {
            data: 'chargeNode',
            render: function (data) {
                return enums.feeChargeNode[data];
            }
        }, {
            data: 'chargeBase',
            render: function (data, type, row) {
                if (row.chargeType == 'LOAN_REQUEST_INPUT') {
                    return "前端输入";
                }
                if (row.chargeType == 'FIXED_AMOUNT') {
                    return data + "元";
                }
                return data + "%";
            }
        },{
            data: 'repayMethod',
            render: function (data) {
                return enums.loanRepayMethod[data];
            }
        },{
            data: 'channelType',
            render: function (data) {
                if(data !='ALL' && data && data !=''){
                    return enums.channelType[data];
                }else{
                    return '全部'
                }
            }
        }], columnDefs: [{
            //指定第最后一列
            className:"single line",
            targets: 8,
            render: function (data, type, row, meta) {
                return "<button onclick=\"editFee('" + row.id + "')\" class=\"ui mini button basic\" data-id=" + row.id + "><i class='edit icon'></i>修改</button>"
                    + "<button onclick=\"deleteFee('" + row.id + "')\" class=\"ui mini basic button red\" data-id=" + row.id + "><i class='remove icon'></i>删除</button>";
            }
        }],
        "iDisplayLength": 20,
        "aLengthMenu": [
            [20],
            [20]
        ],
        initComplete: function () {
            $(".right.aligned.eight.wide.column").append("<a id='addFee' class='ui teal button mini'><i class='plus icon'></i>添加</a>");
        }


    });
}

function deleteFee(id) {
    $.uiDialog("确定删除？", {
        onApprove: function () {
            var $modal = $(this);
            $(document).api({
                on: "now",
                method: 'post',
                url: "/product_fee/delete",
                data: {
                    "feeId": id
                },
                onSuccess: function (response) {
                    $modal.modal({
                        onHidden: function () {
                            if (response.ok) {
                                $.uiAlert({
                                    type: "success",
                                    textHead: '删除成功',
                                    text: '删除成功',
                                    time: 1,
                                    onClosed: function () {
                                        feeTable.ajax.reload();
                                    }
                                });
                            } else {
                                $.uiAlert({
                                    type: "danger",
                                    textHead: '删除失败',
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
//初始化还款方式列表
function initRepayMethod(selectId,isAdd) {
    var repayMehotd = $("#repayMethod").val();
    var repayMehotdArray = repayMehotd.split("");
    console.log(repayMehotdArray)
    var obj = $(("#"+selectId));
    obj.html("");
    repayMehotdArray.forEach(function(value, index, array){
        if (value==1) {
            obj.append("<option value="+repayMethods[index].key+">"+repayMethods[index].value+"</option>");
        }
    });
    if (isAdd) {
        obj.children("option").attr("disabled",true);
    }
    obj.prepend('<option value="ALL" selected="selected">全部</option>');
    obj.children("option").first().attr("disabled",false);
}
