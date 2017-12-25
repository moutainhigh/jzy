/**
 * Created by yangzb01 on 2017/2/27.
 */
var prdId = $("#stepData").data("prd_id");
var loanId = $("#stepData").data("loan_id");
var comeFrom = utils.getUrlParam('comeFrom');

mediaInfo = [];
// 业务员查询
$("#managerSearch").search({
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
        $("#addForm input[name='saleId']").val(data.id);
    }
});

//贴现人查询
$("#search-mainCertifNumber").search({
    apiSettings: {
        method: "get",
        url: $(document).api.settings.api['query by name type'] + '?name={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'certifNumber'
    },
    onSelect: function (data) {
        $("#addForm input[name='masterBorrowerId']").val(data.id);
        // $("#btn-editMainBorrower").data({"id": data.id});
        $("#addForm input[name='certifNumber']").val(data.certifNumber);

        $('#billAll_info_form input[name="accountName"]').val(data.accountName);
        $('#billAll_info_form input[name="accountBank"]').val(data.bankName);
        $('#billAll_info_form input[name="accountNo"]').val(data.account);

        $('#billAll_info_form input[name="legalRepresentative"]').val(data.legalRepresentative);
        $('#billAll_info_form input[name="legalRepresentativePhone"]').val(data.legalRepresentativePhone);
        $('#billAll_info_form input[name="linkman"]').val(data.linkman);
        $('#billAll_info_form input[name="linkmanPhone"]').val(data.linkmanPhone);
        $('#billAll_info_form input[name="residence"]').val(data.residence);

        addMediaInfo();//调企业资料模板
    }
});

// 绑定上传事件
function bindUpload(){
    $(".js-uploadBtn").each(function(index) {;
        var uploader = Object.create(Uploader).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
        });
        uploader.init();
    });
}

// 点击上传按钮
$(document).on("click",".js-uploadBtn",function() {
    var $item = $(this).parents("tr");
    // clearMedia($item);
});

//点击按钮删除对应文件
$(document).on('click','.js-remove',function(){
    var parentsDel = $(this).parents('.js-canBeDel');
    mediaDetail = mediaInfo == "" ? mediaDetail:mediaInfo ;
    var k = $(this).parents('tr').index();
    var n = parentsDel.index();
    $.each(mediaDetail[k],function (i,item) {
        if(n == i){
            mediaDetail[k].splice(n,1);
        }
    });
    var detail = mediaDetail[k] == '' ? '' : JSON.stringify(mediaDetail[k]);
    $(this).parents("tr").find("input[name='mediaDetail']").val(detail);
    parentsDel.remove();
});

$.fn.form.settings.rules.matchedCharacter = function(value){
    var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\(\)\（\）]+$/g;
    return (reg.test(value) && value.length <= 50) || value == ''
}

$.fn.form.settings.rules.link_phone = function(value){
    var mobile = /^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$/;
    var fix_phone = /^[0-9]{3,5}\-[0-9]{3,8}$/;
    return mobile.test(value) || fix_phone.test(value) || value ==''
}

//借款人表单校验
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
        account: {
            identifier: 'account',
            rules: [{
                type: 'bankCard',
                prompt: '账号不正确（6-30位）'
            }]
        },
        legalRepresentative: {
            identifier: 'legalRepresentative',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'matchedCharacter',
                prompt:'50位字符（只包含数字、中文、字母和括号）'
            }]
        },
        legalRepresentativePhone: {
            identifier: 'legalRepresentativePhone',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'link_phone',
                prompt:'请输入正确的联系方式'
            }]
        },
        linkman: {
            identifier: 'linkman',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'matchedCharacter',
                prompt:'50位字符（只包含数字、中文、字母和括号）'
            }]
        },
        linkmanPhone: {
            identifier: 'linkmanPhone',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'link_phone',
                prompt:'请输入正确的联系方式'
            }]
        },
        residence: {
            identifier: 'residence',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'maxLength[200]',
                prompt:'长度为200个字符'
            }]
        },
    },
    onSuccess: function (e, fidlds) {
        e.preventDefault();
    }
};

// 点击新建借款人按钮
$("#btn-addMainBorrower").click(function () {
    $("#btn-addBorrower").data({"action": "main"});
    $("#form-addBorrower")[0].reset();
    $("#modal-addBorrower").modal({blurring: true}).modal('show');
});

// 点击修改借款人按钮
$("#btn-editMainBorrower").click(function () {
    var id = $(this).data("id");
    var name = $("#search-mainCertifNumber input[name='masterBorrowerName']").val();
    if(null == name || ''== name){
        $.uiAlert({type: "danger", textHead: '提示', text: '请先选择需要维护的贴现人', time: 3});
        return;
    }else {
        $(document).api({
                on: "now",
                url: "/borrower/query_by_name",
                method: "get",
                data: {name: name},
                onSuccess: function (data) {
                    if(0==data.data.length){
                        $.uiAlert(
                            {
                                type: "danger",
                                textHead: '提示',
                                text: '此贴现人不存在',
                                time: 3
                            });
                    }else {
                        $(document).api({
                                on: "now",
                                url: "/borrower/fetch_by_id",
                                method: "get",
                                data: {id: id},
                                onSuccess: function (data) {
                                    var borrower = data.data;
                                    $("#form-editBorrower input[name='id']").val(borrower.id);
                                    $("#form-editBorrower input[name='name']").val(borrower.name);
                                    $("#form-editBorrower input[name='certifNumber']").val(borrower.certifNumber);
                                    $("#form-editBorrower input[name='accountName']").val(borrower.accountName);
                                    $("#form-editBorrower input[name='account']").val(borrower.account);
                                    $("#form-editBorrower input[name='bankName']").val(borrower.bankName);
                                    $("#btn-editBorrower").data({"action": "main"});
                                    $("#modal-editBorrower").modal({
                                        observeChanges: true,
                                        blurring: true
                                    }).modal("show");
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
                }
            }
        );
    }
});

//新建贴现人
$("#btn-addBorrower").click(function () {
    $('#form-addBorrower').form(validateOptions).api({
        url: '/borrower/add_discount',
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
            $("#modal-addBorrower").modal('hide');
            $.uiAlert({type: "success", textHead: '新建贴现人', text: '保存成功', time: 1});
            $("#search-mainCertifNumber").find('.prompt').val(response.data.name);
            $("#addForm input[name='masterBorrowerId']").val(response.data.id);
            $("#addForm input[name='certifNumber']").val(response.data.certifNumber);

            $('#billAll_info_form input[name="accountName"]').val(response.data.accountName);
            $('#billAll_info_form input[name="accountBank"]').val(response.data.bankName);
            $('#billAll_info_form input[name="accountNo"]').val(response.data.account);

            $('#billAll_info_form input[name="legalRepresentative"]').val(response.data.legalRepresentative);
            $('#billAll_info_form input[name="legalRepresentativePhone"]').val(response.data.legalRepresentativePhone);
            $('#billAll_info_form input[name="linkman"]').val(response.data.linkman);
            $('#billAll_info_form input[name="linkmanPhone"]').val(response.data.linkmanPhone);
            $('#billAll_info_form input[name="residence"]').val(response.data.residence);
            addMediaInfo();//调企业资料模板
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '新建贴现人', text: response.msg, time: 3});
        }
    });
});

//编辑贴现人
$("#btn-editBorrower").click(function () {
    $('#form-editBorrower').form(validateOptions).api({
        url: '/borrower/update_discount',
        method: 'POST',
        serializeForm: true,
        onSuccess: function (response) {
            $("#modal-editBorrower").modal('hide');
            $.uiAlert({type: "success", textHead: '编辑贴现人', text: '保存成功', time: 1});
            $("#search-mainCertifNumber").find('.prompt').val(response.data.name);
            $("#addForm input[name='masterBorrowerId']").val(response.data.id);
            $("#addForm input[name='certifNumber']").val(response.data.certifNumber);
            addMediaInfo();//调企业资料模板
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '修改贴现人', text: response.msg, time: 3});
        }
    });
});



//校验贴现人信息
var validateSettings = {
    inline: true,
    on: 'blur',
    fields: {
        'org.managerName': {
            identifier: 'org.managerName',
            rules: [{
                type: 'empty',
                prompt: '请选择资产来源'
            }]
        },
        'loanSubjectId': {
            identifier: 'loanSubjectId',
            rules: [{
                type: 'empty',
                prompt: '请选择放款主体'
            }]
        },
        'masterBorrowerName': {
            identifier: 'masterBorrowerName',
            rules: [{
                type: 'empty',
                prompt: '贴现人不能为空'
            }]
        }
    },
    onSuccess: function (e, fidlds) {
        e.preventDefault();
    }
};

//保存贴现人信息
$("#bill_setp1Form").click(function(){
    $("#addForm").form(validateSettings).api({
        action: 'bill add borrower',
        method: 'POST',
        beforeSend:function(settings){
            var $this = $("#addForm");
            var medias = [];
            var errors = [];
            settings.data["masterBorrowerId"] = $this.find('input[name="masterBorrowerId"]').val();
            settings.data["saleId"] = $this.find('input[name="saleId"]').val();
            settings.data["productId"] = $("#stepData").data("prd_id");
            settings.data["loanId"] = loanId;
            settings.data["loanSubjectId"] = $this.find('select[name="loanSubjectId"]').val();
            $("tr.mediaItem").each(function(i,n) {
                var $item = $(this);
                var required = $item.find("input[name='required']").val();
                var productMediaAttachId = $item.find("input[name='productMediaAttachId']").val();
                var tmplId = $item.find("input[name='tmplId']").val();
                var mediaInfo = $item.find("input[name='mediaDetail']").val();
                if(required == "true" && (mediaInfo == "" || mediaInfo == "[]")) {
                    $item.addClass("error");
                    errors.push('<li>必填项必须上传</li>');
                } else {
                    $item.removeClass("error");
                }
                medias.push({
                    'loanId': loanId,
                    'itemName': $item.find("input[name='name']").val(),
                    'required': required,
                    'mediaItemType': $item.find("input[name='mediaItemType']").val(),
                    'productMediaAttachDetails':mediaInfo,
                    'id':productMediaAttachId,
                    'tmplId':tmplId
                });
            });
            settings.data["itemStr"] = JSON.stringify(medias);
            if(errors.length == 0) {
                return settings;
            } else {
                $(".ui.error.message ul").html(errors.join(""));
                $("#addForm").addClass("error");
                return false;
            }
        },
        onSuccess:function(response){
            var localUrl = window.location.pathname;
            if(localUrl.indexOf('to_add') > -1){
                $.uiAlert(
                    {type: "success", textHead: '保存成功', text: '', time: 1,
                    onClosed:function(){
                    window.location.href = '/business_apply/to_update?id='+response.loan.id+'&comeFrom='+comeFrom;
                    }
                });
            }else{
                $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 2});
                relationOrderInit.getData(function(data){
                    relationOrderInit.unRelationList(data);
                });
            }

        },
        onFailure:function(response){
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 4});
        }
    });
    $("#addForm").submit();
});

function addMediaInfo(){
    $(document).api({
        on:'now',
        action: 'borrwer bill media',
        method: 'POST',
        serializeForm: true,
        beforeSend:function(settings){
            var $this = $("#addForm");
            settings.data["masterBorrowerId"] = $this.find('input[name="masterBorrowerId"]').val();
            settings.data["productId"] = $("#stepData").data("prd_id");
            settings.data["loanId"] = loanId;
            return settings;
        },
        onSuccess:function(data){
            var _data = data.data;
            _data.media = data.data;
            _data.url = function(){
                if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                    return true;
                }else{
                    return false;
                }
            };
            _data.itemName = function(){
                return this.name;
            };
            var $riskFormTemplate = utils.render("#billMediaTemplate", _data);
            $("#billMedia").html($riskFormTemplate);
            bindUpload()//渲染后绑定事件
            upArryList();//创建数组
            var n = $('.js-uploadBtn').length;
            for(var i=0;i<n;i++){
                $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
                    mediaDetail[i].push({
                        url:$(ele).attr("href"),
                        attachName:$(ele).text()
                    })
                });
                var detail = JSON.stringify(mediaDetail[i]);
                $('.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail)
            }
        },
        onFailure:function(data){
            $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 4});;
        }
    });
}
