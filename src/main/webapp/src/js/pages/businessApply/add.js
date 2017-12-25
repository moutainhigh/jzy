var minAmount = $("#stepData").data("min_amount");
var maxAmount = $("#stepData").data("max_amount");
var prdId = $("#stepData").data("prd_id");
var comeFrom = utils.getUrlParam('comeFrom');
var certifType;
//港澳台通行证校验
$.fn.form.settings.rules.passId = function(value){
    if(certifType == 'PASS_ID'){
        var str_l = value.length;
        switch (str_l){
            case 11:
                var reg = new RegExp("^(H|M)[0-9]{10}$");
                return reg.test(value)
            case 8 :
                var reg = new RegExp("^[0-9]{8}$");
                return reg.test(value);
            case 10:
                //香港身份证
                var hk_reg = /^[A-Z]{1}[0-9]{6}(\(|\（)[0-9A-Z]{1}(\)|\）)$/;
                //澳门身份证
                var am_reg = /^(1|5|7){1}[0-9]{6}(\(|\（)[0-9]{1}(\)|\）)$/;
                //台湾身份证
                var tw_reg = /^[A-Z]{1}[0-9]{9}$/;
                return am_reg.test(value) || tw_reg.test(value) || hk_reg.test(value);
            default:
                return false;
        }
    }else{
        return true;
    }
};

//身份证
$.fn.form.settings.rules.identityCodeValids = function (value) {
    if(certifType == 'ID') {
        var city = {
            11: "北京", 12: "天津", 13: "河北", 14: "山西", 15: "内蒙古",
            21: "辽宁", 22: "吉林", 23: "黑龙江 ",
            31: "上海", 32: "江苏", 33: "浙江", 34: "安徽", 35: "福建", 36: "江西", 37: "山东",
            41: "河南", 42: "湖北 ", 43: "湖南", 44: "广东", 45: "广西", 46: "海南",
            50: "重庆", 51: "四川", 52: "贵州", 53: "云南", 54: "西藏 ",
            61: "陕西", 62: "甘肃", 63: "青海", 64: "宁夏", 65: "新疆",
            71: "台湾",
            81: "香港",
            82: "澳门",
            91: "国外 "
        };
        var tip = "";
        var pass = true;

        if (!value || !/^\d{6}(18|19|20)?\d{2}(0[1-9]|1[012])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i.test(value)) {
            tip = "身份证号格式错误";
            pass = false;
        } else if (!city[value.substr(0, 2)]) {
            tip = "地址编码错误";
            pass = false;
        } else {
            //18位身份证需要验证最后一位校验位
            if (value.length == 18) {
                value = value.split('');
                //∑(ai×Wi)(mod 11)
                //加权因子
                var factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
                //校验位
                var parity = [1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2];
                var sum = 0;
                var ai = 0;
                var wi = 0;
                for (var i = 0; i < 17; i++) {
                    ai = value[i];
                    wi = factor[i];
                    sum += ai * wi;
                }
                var last = parity[sum % 11];
                if (parity[sum % 11] != value[17]) {
                    tip = "校验位错误";
                    pass = false;
                }
            }
        }
        return pass;
    }else{
        return true;
    }
};

var validateOptions = {
    inline: true,
    on: 'blur',
    fields: {
        name: {
            identifier: 'name',
            rules: [{
                type: 'empty',
                prompt: '姓名不能为空'
            }, {
                type: 'maxLength[20]',
                prompt: '不超过20个字符'
            }]
        },
        certifNumber: {
            identifier: 'certifNumber',
            rules: [{
                type: 'empty',
                prompt: '证件号码不能为空'
            }, {
                type: 'maxLength[20]',
                prompt: '不超过20个字符'
            },{
                type:'passId',
                prompt:'证件号格式不对'
            },{
                type:'identityCodeValids',
                prompt:'身份证格式不对'
            }]
        },
        phone: {
            identifier: 'phone',
            rules: [{
                type: 'mobile',
                prompt: '手机号格式不正确'
            }]
        },
        address: {
            identifier: 'address',
            rules: [{
                type: 'maxLength[100]',
                prompt: '不超过100个字符'
            }]
        },

    },
    onSuccess: function (e, fidlds) {
        e.preventDefault();
    }
};

// 承揽业务员查询（自营）
$("#channelSearch").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['get prdUser'] + '?keyWord={query}&productId=' + prdId
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (data) {
        var $this = $('#channelSearch');
        var engagedThis = $("#engagedChannelSearch");
        $this.find('input').attr('readonly',true);
        $this.find('.js_search').addClass('close').css('pointer-events','auto');
        engagedThis.find('input').attr('readonly',true);
        engagedThis.find('.js_search').addClass('close').css('pointer-events','auto');
        $("#addForm input[name='saleId'],#addForm input[name='engagedSaleId']").val(data.id);
        $("#addForm input[name='engagedSaleName']").val(data.name);
    }
});

//承做业务员
$("#engagedChannelSearch").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['get prdUser'] + '?keyWord={query}&productId=' + prdId
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (data) {
        var $this = $('#engagedChannelSearch');
        $this.find('input').attr('readonly',true);
        $this.find('.js_search').addClass('close').css('pointer-events','auto');
        $("#addForm input[name='engagedSaleId']").val(data.id);
    }
});


function initSlectType(obj){
    if(obj){
        var val = obj;
    }else{
        var val = $('#addForm select[name="channelType"]').val();
    }
    if(val == '0'){
        //自营
        $('#channelSource').removeClass('ks-hidden');
        $('#channelSource input').attr('disabled',false);
        $('#managerChannelSearch').addClass('ks-hidden');
        $('#managerChannelSearch input').attr('disabled',true);
        $('#channelSearch,#engagedChannel').removeClass('ks-hidden');
        $('#channelSearch input,#engagedChannelSearch input').attr('disabled',false);
    }else{
        $('#channelSearch,#engagedChannel').addClass('ks-hidden');
        $('#channelSearch input,#engagedChannelSearch input').attr('disabled',true);
        $('#managerChannelSearch').removeClass('ks-hidden');
        $('#managerChannelSearch input').attr('disabled',false);
        $('#channelSource').addClass('ks-hidden');
        $('#channelSource input').attr('disabled',true);
    }
}

window.onload = function(){
    if($('#addForm select[name="channelType"]').val() == '0'){
        $('#managerName').html('承揽业务员')
    }else{
        $('#managerName').html('业务来源')
    }
};

$('#addForm select[name="channelType"]').change(function(){
    var val = $(this).val();
    if(val == '0'){
        $('#managerName').html('承揽业务员')
    }else{
        $('#managerName').html('业务来源')
    }

    initSlectType(val);
});
initSlectType();


// 添加收款账户
$("#btn-addBank").click(function () {
    $("#main_banks").append($("#demo-bank").clone().removeClass("ks-hidden"));
});

// 移除收款账户
$(document).on("click", ".js-removeBank", function () {
    $(this).parent().parent().remove();
});

//银行卡号4位一空
$('#main_banks').on("keyup",".js-bankCard",function(){
    var Val = $(this).val().replace(/\D/g, '').replace(/....(?!$)/g, '$& ');
    $(this).val(Val);
});

function renderBorrower(response) {
    var data = response.data;
    data.certifTypeInCN = function () {
        return enums.certifType[this.certifType];
    }
    return utils.render("#borrowerTemplate", data);
}

// 点击新建借款人按钮
$("#btn-addMainBorrower").click(function () {
    $("#btn-addBorrower").data({"action": "main"});
    $("#form-addBorrower")[0].reset();
    $("#modal-addBorrower").modal({blurring: true}).modal('show');
});
$("#btn-addCommonBorrower").click(function () {
    $("#btn-addBorrower").data({"action": "common"});
    $("#form-addBorrower")[0].reset();
    $("#modal-addBorrower").modal({blurring: true}).modal('show');
});

// 点击修改借款人保存按钮
$("#btn-eidtBorrower").click(function () {
    certifType = $('#form-editBorrower select[name="certifType"]').val();
    $('#form-editBorrower').form(validateOptions).api({
        url: '/borrower/update',
        method: 'POST',
        serializeForm: true,
        beforeSend:function(settings){
            for(i in settings.data){
                var val = settings.data[i];
                settings.data[i] = $.trim(val);
            }
            return settings;
        },
        onSuccess: function (response) {
            var borrowerId = response.data.id;
            $("#borrower-" + borrowerId).find(".borrower-name").html(response.data.name);
            $("#borrower-" + borrowerId).find(".borrower-certifType").html(enums.certifType[response.data.certifType]);
            $("#borrower-" + borrowerId).find(".borrower-certifNumber").html(response.data.certifNumber);
            $("#borrower-" + borrowerId).find(".borrower-phone").html(response.data.phone);
            $("#borrower-" + borrowerId).find(".borrower-address").html(response.data.address);

            $("#modal-editBorrower").modal('hide');
            $.uiAlert({type: "success", textHead: '修改借款人', text: '保存成功', time: 1});
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '修改借款人', text: response.msg, time: 3});
        }
    });
});

// 点击新建借款人表单保存按钮
$("#btn-addBorrower").click(function () {
    var action = $(this).data("action");
    certifType = $('#form-addBorrower select[name="certifType"]').val();
    $('#form-addBorrower').form(validateOptions).api({
        url: '/borrower/add',
        method: 'POST',
        serializeForm: true,
        onSuccess: function (response) {
            if (action == "main") {
                $("#mainBorrowerList").html(renderBorrower(response));
            } else {
                $("#commonBorrowerList").append(renderBorrower(response));
            }
            $("#modal-addBorrower").modal('hide');
            $.uiAlert({type: "success", textHead: '新建借款人', text: '保存成功', time: 1});
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '新建借款人', text: response.msg, time: 3});
        }
    });
});

// 点击编辑借款人
$(document).on("click", ".btn-borrowerEdit", function () {
    var borrowerId = $(this).data("borrowerid");
    var certifType = $(this).data("certif_type");
    $("#modal-editBorrower").find("input[name='id']").val(borrowerId);
    $("#modal-editBorrower").find("select[name='certifType']").find("option[value=" + certifType + "]").attr("selected", true);
    $("#modal-editBorrower").find("input[name='name']").val($("#borrower-" + borrowerId).find('.borrower-name').text());
    $("#modal-editBorrower").find("input[name='phone']").val($("#borrower-" + borrowerId).find('.borrower-phone').text());
    $("#modal-editBorrower").find("input[name='certifNumber']").val($("#borrower-" + borrowerId).find('.borrower-certifNumber').text());
    $("#modal-editBorrower").find("input[name='address']").val($("#borrower-" + borrowerId).find('.borrower-address').text());
    $("#modal-editBorrower").modal({blurring: true}).modal("show");
});

// 移除借款人
$(document).on("click", ".btn-borrowerRemove", function () {
    var borrowerId = $(this).data("borrowerid");
    $("#borrower-" + borrowerId).remove();
});


// 添加主借款人
$("#js-queryMainBorrower").api({
    action: "fetch borrowerByCertifNumber",
    beforeSend: function (settings) {
        settings.data["certifNumber"] = $("#input-mainCertifNumber").val();
        settings.data["certifType"] = '';
        return settings;
    },
    onSuccess: function (response) {
        if (!response.data) {
            $.uiAlert({type: "danger", textHead: '添加主借款人错误', text: "借款人不存在", time: 3});
            return
        }
        if ($("#commonBorrowerList #borrower-" + response.data.id).length > 0) {
            $.uiAlert({type: "danger", textHead: '添加主借款人错误', text: "共同借款人已存在此借款人", time: 3});
        } else {
            $("#mainBorrowerList").html(renderBorrower(response));
        }
    }
});

// 添加共同借款人
$("#js-queryCommonBorrower").api({
    action: "fetch borrowerByCertifNumber",
    beforeSend: function (settings) {
        settings.data["certifNumber"] = $("#input-commonCertifNumber").val();
        settings.data["certifType"] = '';
        return settings;
    },
    onSuccess: function (response) {
        if (!response.data) {
            $.uiAlert({type: "danger", textHead: '添加共同借款人错误', text: "借款人不存在", time: 3});
            return
        }
        if ($("#mainBorrowerList #borrower-" + response.data.id).length > 0) {
            $.uiAlert({type: "danger", textHead: '添加共同借款人错误', text: "主借款人已存在此借款人", time: 3});
            return
        }

        if ($("#commonBorrowerList #borrower-" + response.data.id).length > 0) {
            $.uiAlert({type: "danger", textHead: '添加共同借款人错误', text: "共同借款人已存在此借款人", time: 3});
        } else {
            $("#commonBorrowerList").append(renderBorrower(response));
        }
    }
});


$.fn.form.settings.rules.mainBorrwerExist = function (value) {
    return $("#mainBorrowerList .item").length > 0;
};

$.fn.form.settings.rules.validateAmount = function (value) {
    var amount = 0;
    $("#main_banks .relative").each(function () {
        var loanAmount =$(this).find("input[name='account.amount']").val()
        if(isNaN(loanAmount)||loanAmount===""){
            loanAmount=0;
        }
        amount += parseFloat(loanAmount);
    });
    if ((isNaN(amount) || amount < parseFloat(minAmount)) || (amount > parseFloat(maxAmount))) {
        return false;
    } else {
        return true;
    }
};

$.fn.form.settings.rules.selected = function (value,obj) {
    if(value == ''){
        return true;
    }else{
        if($('input[name="'+obj+'"]').prop('readonly')){
            return true;
        }else{
            return false
        }
    }
}


var formSettings = {
    inline: true,
    fields: {
        'account.name': {
            identifier: 'account.name',
            rules: [{
                type: 'empty',
                prompt: '收款人不能为空'
            },{
                type: 'z_chRange[50,25]',
                prompt: '50位字符，25位中文'
            }]
        },
        'account.bank': {
            identifier: 'account.bank',
            rules: [{
                type: 'empty',
                prompt: '开户行不能为空'
            }, {
                type: 'z_chRange[50]',
                prompt: '输入不超过50位的中文'
            }]
        },
        'account.account': {
            identifier: 'account.account',
            rules: [{
                type: 'empty',
                prompt: '收款账号不能为空'
            }, {
                type: 'bankCard',
                prompt: '收款账号不正确（请输入6-30位数字）'
            }]
        },
        'account.amount': {
            identifier: 'account.amount',
            rules: [
                {
                    type: 'empty',
                    prompt: '借款本金不能为空'
                }, {
                    type: 'canBeDecimal[2]',
                    prompt: '借款本金只能为数字，小数点后最多两位'
                }, {
                    type: 'validateAmount',
                    prompt: '总金额有误(' + minAmount + '~' + maxAmount + '之间)'
                }
            ]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
}

//todo
$(document).on("click", "#btn-submit", function () {
    var channelType = $('#addForm select[name="channelType"]').val();
    var partnerName = $('#addForm input[name="partnerName"]').val();
    if(channelType == 0 &&  partnerName==''){
        $("#addForm input[name='channelId']").val("");
    }
    var flag = true;
    $("#main_banks .ui.form").each(function () {
        var s_flag = $(this).form(formSettings).form("validate form");
        flag = flag && s_flag;
    })
    $("#addForm").form({
        fields: {
            'org.managerName': {
                identifier: 'org.managerName',
                rules: [{
                    type: 'empty',
                    prompt: '承揽业务员不能为空'
                },{
                    type:'selected[org.managerName]',
                    prompt:'请在查询中选择已有业务员'
                }]
            },
            'engagedSaleName': {
                identifier: 'engagedSaleName',
                rules: [{
                    type: 'empty',
                    prompt: '承做业务员不能为空'
                },{
                    type:'selected[engagedSaleName]',
                    prompt:'请在查询中选择已有业务员'
                }]
            },
            'channelSearchName':{
                identifier: 'channelSearchName',
                rules: [{
                    type: 'empty',
                    prompt: '业务来源不能为空'
                },{
                    type:'selected[channelSearchName]',
                    prompt:'请在查询中选择已有业务来源'
                }]
            },
            'mainCertifNumber': {
                identifier: 'mainCertifNumber',
                rules: [{
                    type: 'mainBorrwerExist',
                    prompt: '主借款人不能为空'
                }]
            },
            'loanSubjectId':{
                identifier:'loanSubjectId',
                rules:[{
                    type:'empty',
                    prompt:'请选择放款主体'
                }]
            },
            'partnerName':{
                identifier:'partnerName',
                rules:[{
                    type:'selected[partnerName]',
                    prompt:'请在查询中选择已有业务提供方'
                }]
            },
            onSuccess: function (e, fidlds) {
                e.preventDefault();
            }
        }
    }).form("validate form");
    if (flag) {
        $("#addForm").api({

            action: "add borrowers",
            method: 'POST',
            serializeForm: true,
            beforeSend: function (settings) {

                settings.data["step"] = "1000";
                settings.data["masterBorrower"] = $("#mainBorrowerList .item").eq(0).data("id");

                var accounts = [];
                var total_amount = 0;
                $("#main_banks .relative").each(function () {
                    var $item = $(this);
                    if($item.find("input[name='account.account']").val()){
                        var account = $item.find("input[name='account.account']").val().replace(/\s/g,'')
                    }
                    accounts.push({
                        'name': $item.find("input[name='account.name']").val(),
                        'bank': $item.find("input[name='account.bank']").val(),
                        'account': account,
                        'amount': $.trim($item.find("input[name='account.amount']").val()),
                        'platformAccount':$item.find("input[name='account.platformAccount']").val()
                    })
                    if (!isNaN($item.find("input[name='account.amount']").val())) {
                        total_amount += parseFloat($.trim($item.find("input[name='account.amount']").val()));
                    }

                });
                settings.data["accounts"] = JSON.stringify(accounts);
                settings.data["amount"] = total_amount;
                if($('#addForm select[name="channelType"]').val() == '0'){
                    settings.data["channelSearchName"] = $('#addForm').find("input[name='partnerName']").val();
                }

                var borrowers = [];
                $("#commonBorrowerList .item").each(function () {
                    borrowers.push($(this).data("id"));
                });
                settings.data["borrowers"] = JSON.stringify(borrowers);
                return settings;
            },
            onSuccess: function (response) {
                window.location.href = "/business_apply/to_update?flag=2&id=" + response.data.id +'&comeFrom='+comeFrom;
            },
            onFailure: function (response) {
                $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
            }
        });
        $("#addForm").submit();
    } else {

    }
})


// 渠道查询
function getManager(obj){
    $('#'+obj).search({
        apiSettings: {
            method: "post",
            url: '/channel/list_channel_names' + '?channelName={query}&channelType=1&cooperationProductType='+$('#productChild').val()+''
        },
        fields: {
            results: 'data',
            title: 'name',
            description: 'fullName'
        },
        onSelect: function (data) {
            var $this = $('#'+obj);
            $this.find('input').attr('readonly',true);
            $this.find('.js_search').addClass('close');
            $this.find('.js_search').css('pointer-events','auto');

            $("#addForm input[name='channelId']").val(data.id);
            if($('select[name="channelType"]').val() == '1'){
                $("#addForm input[name='saleId'],#addForm input[name='engagedSaleId']").val(data.managerId);
            }
        },onResults: function(data) {

        }
    });
}
getManager('managerChannelSearch');

//业务提供方
$('#partnerSearch').search({
    apiSettings: {
        method: "post",
        url: '/channel/list_channel_name' + '?channelName={query}&channelType=0'
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (data) {
        var $this = $('#partnerSearch');
        $this.find('input').attr('readonly',true);
        $this.find('.js_search').addClass('close');
        $this.find('.js_search').css('pointer-events','auto');
        $("#addForm input[name='channelId']").val(data.id);
    }
});

$(document).on('click','.js_search.close',function(){
    $(this).css('pointer-events','none');
    $(this).siblings('input').attr('readonly',false);
    $(this).siblings('input').val("");
    $(this).removeClass('close');
});
