/**
 * Created by yangzb01 on 2017-07-18.
 */
var minAmount = $("#stepData").data("min_amount");
var maxAmount = $("#stepData").data("max_amount");
var prdId = $("#stepData").data("prd_id");
var comeFrom = utils.getUrlParam('comeFrom');
var certifType;

//银行卡号4位一空
$('#main_banks').on("keyup",".js-bankCard",function(){
    var Val = $(this).val().replace(/\D/g, '').replace(/....(?!$)/g, '$& ');
    $(this).val(Val);
});

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

$.fn.form.settings.rules.mainBorrwerExist = function (value) {
    return $("#mainBorrowerList .item").length > 0;
};

/*
* 收款账户
* */
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
                type:'empty',
                prompt:'营业执照编号不为空'
            },{
                type: 'maxLength[30]',
                prompt: '不超过30个字符'
            }]
        },
        legalPerson:{
            identifier: 'legalPerson',
            rules: [{
                type:'empty',
                prompt:'法人姓名不为空'
            },{
                type: 'maxLength[20]',
                prompt: '不超过20个字符'
            }]
        },
        legalPersonCertifNumber:{
            identifier: 'legalPersonCertifNumber',
            rules: [{
                type: 'identityCodeValid',
                prompt: '身份证格式不正确'
            }]
        }
    },
    onSuccess: function (e, fidlds) {
        e.preventDefault();
    }
};

//自营&渠道 转换
function initSlectType(obj){
    if(obj){
        var val = obj;
    }else{
        var val = $('#addForm select[name="channelType"]').val();
    }
    if(val == '0'){
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


//收款账户
var account = {
    /**
    * 新增demo
    * */
    add_demo:function(){
        $("#btn-addBank").click(function () {
            $("#main_banks").append($("#demo-bank").clone().removeClass("ks-hidden"));
        });
    },

    /**
    * 删除demo
    * */
    del_demo:function(){
        $(document).on("click", ".js-removeBank", function () {
            $(this).parent().parent().remove();
        });
    }
};

var search ={
    /**
    * 渠道查询
    * */
    channel:function(){
        $('#managerChannelSearch').search({
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
                var $this = $('#managerChannelSearch');
                $this.find('input').attr('readonly',true);
                $this.find('.js_search').addClass('close');
                $this.find('.js_search').css('pointer-events','auto');

                $("#addForm input[name='channelId']").val(data.id);
                if($('select[name="channelType"]').val() == '1'){
                    $("#addForm input[name='saleId']").val(data.managerId);
                }
            },onResults: function(data) {

            }
        });
    },

    /**
    * 业务提供方查询
    * */
    partner:function(){
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
    },

    /**
    * 承揽业务员查询
    * */
    manager:function(){
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
    },

    /**
     * 承做业务员查询
     * */
    engagedManager:function(){
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
                $this.find('.js_search').addClass('close');
                $this.find('.js_search').css('pointer-events','auto');
                $("#addForm input[name='engagedSaleId']").val(data.id);
            }
        });
    }
};


var borrower = {
    /**
    * 借款人modal显示
    * */
    show:function(){
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
    },

    /**
    * 新建借款人
    * */
    add:function(){
        $("#btn-addBorrower").click(function () {
            var action = $(this).data("action");
            certifType = $('#form-addBorrower select[name="certifType"]').val();
            $('#form-addBorrower').form(validateOptions).api({
                url: '/borrower/add',
                method: 'POST',
                serializeForm: true,
                beforeSend: function (settings) {
                    settings.data["certifType"] ='BUSINESS_LICENSE';
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess: function (response) {
                    if (action == "main") {
                        $("#mainBorrowerList").html(borrower.render(response));
                    } else {
                        $("#commonBorrowerList").append(borrower.render(response));
                    }
                    $("#modal-addBorrower").modal('hide');
                    $.uiAlert({type: "success", textHead: '新建借款人', text: '保存成功', time: 1});
                },
                onFailure: function (response) {
                    $.uiAlert({type: "danger", textHead: '新建借款人', text: response.msg, time: 3});
                }
            });
        });
    },

    /**
    * 更新借款人信息
    * */
    update:function(){
        $("#btn-eidtBorrower").click(function () {
            certifType = $('#form-editBorrower select[name="certifType"]').val();
            $('#form-editBorrower').form(validateOptions).api({
                url: '/borrower/update',
                method: 'POST',
                serializeForm: true,
                beforeSend:function(settings){
                    settings.data['certifType'] = 'BUSINESS_LICENSE';
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess: function (response) {
                    var borrowerId = response.data.id;
                    $("#borrower-" + borrowerId).find(".borrower-name").html(response.data.name);
                    $("#borrower-" + borrowerId).find(".borrower-certifNumber").html(response.data.certifNumber);
                    $("#borrower-" + borrowerId).find(".borrower-legalPerson").html(response.data.legalPerson);
                    $("#borrower-" + borrowerId).find(".borrower-legalPersonCertifNumber").html(response.data.legalPersonCertifNumber);

                    $("#modal-editBorrower").modal('hide');
                    $.uiAlert({type: "success", textHead: '修改借款人', text: '保存成功', time: 1});
                },
                onFailure: function (response) {
                    $.uiAlert({type: "danger", textHead: '修改借款人', text: response.msg, time: 3});
                }
            });
        });
    },

    /**
    * 编辑借款人
    * */
    edit:function(){
        $(document).on("click", ".btn-borrowerEdit", function () {
            var borrowerId = $(this).data("borrowerid");
            $("#modal-editBorrower").find("input[name='id']").val(borrowerId);
            $("#modal-editBorrower").find("input[name='name']").val($("#borrower-" + borrowerId).find('.borrower-name').text());
            $("#modal-editBorrower").find("input[name='certifNumber']").val($("#borrower-" + borrowerId).find('.borrower-certifNumber').text());
            $("#modal-editBorrower").find("input[name='legalPerson']").val($("#borrower-" + borrowerId).find('.borrower-legalPerson').text());
            $("#modal-editBorrower").find("input[name='legalPersonCertifNumber']").val($("#borrower-" + borrowerId).find('.borrower-legalPersonCertifNumber').text());
            $("#modal-editBorrower").modal({blurring: true}).modal("show");
        });
    },

    del:function(){
        $(document).on("click", ".btn-borrowerRemove", function () {
            var borrowerId = $(this).data("borrowerid");
            $("#borrower-" + borrowerId).remove();
        });
    },

    /**
    * 添加主借款人
    * */
    main:function(){
        $("#js-queryMainBorrower").api({
            action: "fetch borrowerByCertifNumber",
            beforeSend: function (settings) {
                settings.data["certifNumber"] = $("#input-mainCertifNumber").val();
                settings.data["certifType"] ='BUSINESS_LICENSE';
                return settings;
            },
            onSuccess: function (response) {
                if (!response.data) {
                    $.uiAlert({type: "danger", textHead: '添加主借款人错误', text: "借款人不存在", time: 3});
                    return
                }
                $("#mainBorrowerList").html(borrower.render(response));
            }
        });
    },

    /**
    * 共同借款人
    * */
    common:function(){
        $("#js-queryCommonBorrower").api({
            action: "fetch borrowerByCertifNumber",
            beforeSend: function (settings) {
                settings.data["certifNumber"] = $("#input-commonCertifNumber").val();
                settings.data["certifType"] ='BUSINESS_LICENSE';
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
                    $("#commonBorrowerList").append(borrower.render(response));
                }
            }
        });
    },

    /**
    * 数据填充模板
    * */
    render:function(response){
        var data = response.data;
        return utils.render("#borrowerTemplate", data);
    }
};



function add_bl(){
    $(document).on("click", "#btn-submit", function () {
        if($('#legalPerson').html() ==''){
            $.uiAlert({type: "danger", textHead: '提交失败', text: "主借款人信息不全，请从新编辑或新增", time: 3});
            return false;
        }
        var channelType = $('#addForm select[name="channelType"]').val();
        var partnerName = $('#addForm input[name="partnerName"]').val();
        if(channelType == 0 &&  partnerName==''){
            $("#addForm input[name='channelId']").val("");
        }
        var flag = true;
        $("#main_banks .ui.form").each(function () {
            var s_flag = $(this).form(formSettings).form("validate form");
            flag = flag && s_flag;
        });
        $("#addForm").form({
            fields: {
                'org.managerName': {
                    identifier: 'org.managerName',
                    rules: [{
                        type: 'empty',
                        prompt: '业务员不能为空'
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
                    window.location.href = "/business_apply/to_update?flag=2&id=" + response.data.id+'&comeFrom='+comeFrom;
                },
                onFailure: function (response) {
                    $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
                }
            });
            $("#addForm").submit();
        }
    })
}


function init(){
    $('#addForm select[name="channelType"]').change(function(){
        var val = $(this).val();
        if(val == '0'){
            $('#managerName').html('业务员')
        }else{
            $('#managerName').html('业务来源')
        }

        initSlectType(val);
    });

    initSlectType();
    search.channel();
    search.partner();
    search.manager();
    search.engagedManager();
    borrower.show();
    borrower.add();
    borrower.update();
    borrower.edit();
    borrower.del();
    borrower.main();
    borrower.common();
    account.add_demo();
    account.del_demo();
    add_bl();

    $(document).on('click','.js_search.close',function(){
        $(this).css('pointer-events','none');
        $(this).siblings('input').attr('readonly',false);
        $(this).siblings('input').val("");
        $(this).removeClass('close');
    });
}
init();
























