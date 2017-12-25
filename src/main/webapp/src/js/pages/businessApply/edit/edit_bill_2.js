/**
 * Created by yangzb01 on 2017/2/27.
 */
var loanId = $("#stepData").data("loan_id");
var prdId = $("#stepData").data("prd_id");
var userType = $("#stepData").data("userType");
var comeFrom = utils.getUrlParam('comeFrom');

//银行卡号4位一空
$('#billAll_info_form,#bill_info_form').on("keyup",".js-bankCard",function(){
    var Val = $(this).val().replace(/\D/g, '').replace(/....(?!$)/g, '$& ');
    $(this).val(Val);
});

var search = {
    intermediary:function(){
        $('#intermediarySearch').search({
            apiSettings: {
                method: "get",
                url: $(document).api.settings.api['query intermediary'] + '?name={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'idNumber'
            },
            onSelect: function (data) {
                var $this = $('#billAll_info_form');
                $this.find('input[name="intermeId"]').val(data.id);
                $this.find('input[name="idNumber"]').val(data.idNumber);
                $this.find('input[name="phone"]').val(data.phone);
                $this.find('input[name="bank"]').val(data.bank);
                $this.find('input[name="account"]').val(data.account);
                $this.find('input[name="address"]').val(data.address);
            }
        })
    }
};
search.intermediary();

//计算金额、利息、付款金额
function reckonAmount(){
    var allAmount = 0;
    var interest = 0;
    $('#bill_info_form .bill_info').each(function(i,ele){
        var a = $(ele).find('input[name="amount"]').val() =='' ? 0 : $(ele).find('input[name="amount"]').val();
        var b = $(ele).find('input[name="interest"]').val() =='' ? 0 : $(ele).find('input[name="interest"]').val();
        allAmount += parseFloat(a);
        interest += parseFloat(b);
    });
    var payAmount = (allAmount - interest).toFixed(2);
    $('#billAll_info_form').find('input[name="totalAmount"]').val(allAmount.toFixed(2));
    $('#billAll_info_form').find('input[name="interest"]').val(interest.toFixed(2));
    $('#billAll_info_form').find('input[name="amount"]').val(payAmount);
}

//比较总金额和利息
$.fn.form.settings.rules.compareAmount = function (value){
    var totalAmount = $('#billAll_info_form').find('input[name="totalAmount"]').val();
    var totalAmount = parseFloat(totalAmount)
    if(parseFloat(value) >= totalAmount){
        return false;
    }else{
        return true;
    }
};

$.fn.form.settings.rules.matchedCharacter = function(value){
    var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\(\)\（\）]+$/g;
    return (reg.test(value) && value.length <= 50) || value == ''
}

$.fn.form.settings.rules.link_phone = function(value){
    var mobile = /^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$/;
    var fix_phone = /^[0-9]{3,5}\-[0-9]{3,8}$/;
    return mobile.test(value) || fix_phone.test(value) || value ==''
}



//计算实际贴现天数&&实际到期日

var reckon = {
    sl_discountDays : function(){
        var sum1=0;
        $('.bill_info').each(function(i,ele){
            var $this = $(this);
            var overdueDays = $this.find('input[name="overdueDays"]'),//调整天数
                dueDate = $this.find('input[name="dueDate"]'),//到期日
                discountTime = $this.find('input[name="disDate"]'),//贴现日期
                disDays = $this.find('input[name="disDays"]'),//实际贴现天数
                actualDueDate = $this.find('input[name="actualDueDate"]'), //实际到期日
                bill_amount = $this.find('input[name="amount"]'), //票面金额
                interest = $this.find('input[name="interest"]'), //打款利息
                rate = $this.find('input[name="depositRate"]'), //打款利率
                costRate = $this.find('input[name="costRate"]'), // 成本报价
                intermediaryFee = $this.find('input[name="intermediaryFee"]'), // 区间费
                intermediaryTotalFee = $('#billAll_info_form input[name="intermediaryTotalFee"]'),//居间总费用
                depositFlag = $this.find('input[name="depositFlag"]');
            if(overdueDays.val()!='' && dueDate.val() != ''){
                var array = dueDate.val().split('-');
                var t_val = parseInt(overdueDays.val());
                var act_time =moment(moment([array[0],array[1]-1,array[2]]).add(t_val, 'd')).format("YYYY-MM-DD");
                actualDueDate.val(act_time);

                if(discountTime.val() != ''){
                    //计算实际贴现天数
                    var dis_val = (Date.parse(dueDate.val()) - Date.parse(discountTime.val()))/(24*60*60*1000) + t_val;
                    disDays.val(parseInt(dis_val));
                }


                //保存计算利息利率
                if(rate.val() !='' && bill_amount.val() !='' && dis_val && depositFlag.val() =='0'){
                    var amount = parseFloat(bill_amount.val());
                    var rate_s = parseFloat(rate.val());
                    interest.val(mul(divFloat(mul(amount,rate_s),360),dis_val).toFixed(2));//打款利息
                }else if(interest.val() != '' && bill_amount.val() !='' && dis_val){
                    var interest_s = interest.val();
                    var amount = parseFloat(bill_amount.val());
                    rate.val(divFloat(divFloat(mul(interest_s,360),amount),dis_val).toFixed(4));//打款利率
                }
            }

            if(disDays.val()!='' && bill_amount.val() !='' && costRate.val() !='' && interest.val() !=''){
                var a_t =divFloat(mul(bill_amount.val(),(divFloat(costRate.val(),100))),360);
                var a_all =sub(interest.val(),mul(a_t,disDays.val())).toFixed(6);
                intermediaryFee.val(a_all);
                sum1 =add(sum1,a_all);
                intermediaryTotalFee.val(sum1.toFixed(6));
            }
        })
    },

    sl_rate:function(){
        $(document).on('input propertychange','input[name="depositRate"]',function(){
            var this_val = $(this).val() == ''? 0:$(this).val();
            var $this = $(this).parents('.bill_info'),
                dueDate = $this.find('input[name="dueDate"]'),
                discountTime = $this.find('input[name="disDate"]'),
                overdueDays = $this.find('input[name="overdueDays"]'),
                interest = $this.find('input[name="interest"]'),
                billAmount = parseFloat($this.find('input[name="amount"]').val()),
                rate = parseFloat(this_val);
            if(overdueDays.val()!='' && dueDate.val() != '' && discountTime.val() != '' && billAmount) {
                var t_val = parseInt(overdueDays.val());
                var disDays = (Date.parse(dueDate.val()) - Date.parse(discountTime.val()))/(24*60*60*1000) + t_val;
                interest.val(mul(divFloat(mul(billAmount,rate),360),disDays).toFixed(2));//打款利息
            }
        });
    },

    sl_interest:function(){
        $(document).on('input propertychange','input[name="interest"]',function(){
            var this_val = $(this).val() == ''? 0:$(this).val();
            var $this = $(this).parents('.bill_info'),
                dueDate = $this.find('input[name="dueDate"]'),
                discountTime = $this.find('input[name="disDate"]'),
                overdueDays = $this.find('input[name="overdueDays"]'),
                rate = $this.find('input[name="depositRate"]'),
                billAmount = parseFloat($this.find('input[name="amount"]').val()),
                interest = parseFloat(this_val);
            if(overdueDays.val()!='' && dueDate.val() != '' && discountTime.val() != '' && billAmount) {
                var t_val = parseInt(overdueDays.val());
                var disDays = (Date.parse(dueDate.val()) - Date.parse(discountTime.val()))/(24*60*60*1000) + t_val;
                rate.val(divFloat(divFloat(mul(interest,360),billAmount),disDays).toFixed(4));//打款利率
            }
        });
    }
};
reckon.sl_interest();
reckon.sl_rate();


$(document).on('blur','#billAll_info_form input[name="interest"]',function(){
    var $this = $('#billAll_info_form');
    var totalAmount = $this.find('input[name="totalAmount"]').val() == '' ? 0:$this.find('input[name="totalAmount"]').val();
    var interest = $this.find('input[name="interest"]').val() == ''? 0:$this.find('input[name="interest"]').val();
    var allAmount = parseFloat(totalAmount);
    var allInterest = parseFloat(interest);
    $('#billAll_info_form').find('input[name="amount"]').val((allAmount-allInterest).toFixed(2));
});

var settingsForm = {
    inline: true,
    on: 'blur',
    fields: {
        totalAmount: {
            identifier: 'totalAmount',
            rules: [{
                type: 'empty',
                prompt: '票面总金额不能为空'
            }, {
                type: 'newCanBeDecimal[16,2]',
                prompt: '票面总金额整数位不超过16位，小数位不超过2位，且不为负数'
            }]
        },
        interest: {
            identifier: 'interest',
            rules: [{
                type: 'empty',
                prompt: '贴现利息不能为空'
            }, {
                type: 'newCanBeDecimal[16,2]',
                prompt: '贴现利息整数位不超过16位，小数位不超过2位，且不为负数'
            }, {
                type: 'compareAmount',
                prompt: '贴现利息应小于票面总金额'
            }]
        },
        amount: {
            identifier: 'amount',
            rules: [{
                type: 'empty',
                prompt: '付款金额不能为空'
            }, {
                type: 'newCanBeDecimal[16,2]',
                prompt: '付款金额整数位不超过16位，小数位不超过2位，且不为负数'
            }]
        },
        accountName: {
            identifier: 'accountName',
            rules: [{
                type: 'empty',
                prompt: '户名不能为空'
            }]
        },
        accountBank: {
            identifier: 'accountBank',
            rules: [{
                type: 'empty',
                prompt: '开户行不能为空'
            }]
        },
        accountNo: {
            identifier: 'accountNo',
            rules: [{
                type: 'empty',
                prompt: '账号不能为空'
            }, {
                type: 'bankCard',
                prompt: '账号不正确（6-30位）'
            }]
        },
        name: {
            identifier: 'name',
            depends:'js_isIntermediary',
            rules: [{
                type:'empty',
                prompt:'居间人姓名不为空'
            },{
                type:'maxLength[50]',
                prompt:'50位字符'
            }]
        },
        intermediaryTotalFee: {
            identifier: 'intermediaryTotalFee',
            rules: [{
                type: 'empty',
                prompt: '居间总费用不为空'
            }]
        },
        idNumber:{
            identifier:'idNumber',
            depends:'js_isIntermediary',
            rules:[{
                type:'empty',
                prompt:'身份证号不为空'
            },{
                type:'identityCodeValid',
                prompt:'身份证格式不正确'
            }]
        },
        phone:{
            identifier:'phone',
            depends:'js_isIntermediary',
            rules:[{
                type:'mobile',
                prompt:'手机号格式不正确'
            }]
        },
        address:{
            identifier:'address',
            depends:'js_isIntermediary',
            rules:[{
                type:'maxLength[100]',
                prompt:'100位字符'
            }]
        },
        bank:{
            identifier:'bank',
            depends:'js_isIntermediary',
            rules:[{
                type:'maxLength[50]',
                prompt:'50位字符'
            }]
        },
        account:{
            identifier:'account',
            depends:'js_isIntermediary',
            rules:[{
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

    }
};


//默认户名
$(function () {
    $("input[name='accountName']").val($("input[class='prompt']")[1].value);
    $(document).on('click','#bill_setp1Form',function () {
        $("input[name='accountName']").val($("input[class='prompt']")[1].value);
    });

    $('#js_isIntermediary').click(function(){
        console.log($(this).prop('checked'))
        if($(this).prop('checked') == true){
            $('#js_intermediaryInfo').removeClass('ks-hidden')
        }else{
            $('#js_intermediaryInfo').addClass('ks-hidden')
        }
    })
})

/*
* 新增居间人
* */

var formValidate = {
    inline: true,
    on: 'blur',
    fields:{
        name: {
            identifier: 'name',
            rules: [{
                type:'empty',
                prompt:'居间人姓名不为空'
            },{
                type:'maxLength[50]',
                prompt:'50位字符'
            }]
        },
        idNumber:{
            identifier:'idNumber',
            rules:[{
                type:'empty',
                prompt:'身份证号不为空'
            },{
                type:'identityCodeValid',
                prompt:'身份证格式不正确'
            }]
        },
        phone:{
            identifier:'phone',
            rules:[{
                type:'mobile',
                prompt:'手机号格式不正确'
            }]
        },
        address:{
            identifier:'address',
            rules:[{
                type:'maxLength[100]',
                prompt:'100位字符'
            }]
        },
        bank:{
            identifier:'bank',
            rules:[{
                type:'maxLength[50]',
                prompt:'50位字符'
            }]
        },
        account:{
            identifier:'account',
            rules:[{
                type: 'bankCard',
                prompt: '账号不正确（6-30位）'
            }]
        }
    }
}

var addIntermediary = {
    show:function () {
        $('#btn-addShow').click(function(){
            document.getElementById("form-addIntermediary").reset();
            $('#modal-intermediary').modal({blurring: true}).modal('show');
        })
    },
    hide:function(){
        $("#modal-intermediary").modal('hide');
    },
    addSubmit:function(){
        $('#btn-addIntermediary').click(function(){
            $('#form-addIntermediary').form(formValidate).api({
                action:'add intermediary',
                method: 'POST',
                serializeForm: true,
                beforeSend:function(settings){
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess:function(data){
                    addIntermediary.hide();
                    $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 3});
                },
                onFailure: function (data) {
                    $.uiAlert({type: "danger", textHead: '保存失败', text: data.msg, time: 3});
                }
            })
        })
    },
};
addIntermediary.show();
addIntermediary.addSubmit();



//保存表单数据
$('#bill_setp3Form').click(function(){
    var loanSubjectId = $('select[name="loanSubjectId"]').val();
    if(loanSubjectId == ""){
        $.uiAlert({type: "danger", textHead: '保存失败', text: "未选择放款主体", time: 3});
        return false;
    }
    $('#billAll_info_form').form(settingsForm).api({
        action:'add bill loan',
        method: 'POST',
        beforeSend: function (settings) {
            var is_intermediary = $('#js_isIntermediary').prop('checked');
            var intermediary  =[];
            var $this = $('#billAll_info_form');
            settings.data['loanSubjectId'] = loanSubjectId;
            settings.data['totalAmount'] = $this.find('input[name="totalAmount"]').val();
            settings.data['interest'] = $this.find('input[name="interest"]').val();
            settings.data['amount'] = $this.find('input[name="amount"]').val();
            settings.data['discountTime'] = $this.find('input[name="discountTime"]').val();
            settings.data['accountName'] = $this.find('input[name="accountName"]').val();
            settings.data['accountBank'] = $this.find('input[name="accountBank"]').val();
            settings.data['accountNo'] = $this.find('input[name="accountNo"]').val().replace(/\s/g,"");
            settings.data['intermediaryId'] = $this.find('input[name="intermediaryId"]').val();
            settings.data['intermediaryTotalFee'] =$this.find('input[name="intermediaryTotalFee"]').val();

            settings.data['legalRepresentative'] = $this.find('input[name="legalRepresentative"]').val();
            settings.data['legalRepresentativePhone'] = $this.find('input[name="legalRepresentativePhone"]').val();
            settings.data['linkman'] = $this.find('input[name="linkman"]').val();
            settings.data['linkmanPhone'] = $this.find('input[name="linkmanPhone"]').val();
            settings.data['residence'] = $this.find('input[name="residence"]').val();
            if(is_intermediary){
                intermediary.push({
                    id:$this.find('input[name="intermeId"]').val(),
                    name:$this.find('input[name="name"]').val(),
                    idNumber:$this.find('input[name="idNumber"]').val(),
                    phone:$this.find('input[name="phone"]').val(),
                    bank:$this.find('input[name="bank"]').val(),
                    account:$this.find('input[name="account"]').val(),
                    address:$this.find('input[name="address"]').val(),
                })
                settings.data['intermediaryStr'] = JSON.stringify(intermediary);
            }else{
                settings.data['intermediaryStr'] = '';
            }

            settings.data['loanId'] = loanId;
            return settings;
        },
        onSuccess:function(response){
            $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 3});
        },
        onFailure:function(response){
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 4});
        }
    });
    $('#billAll_info_form').submit();
});

//提交表单审核 & 校验所有form表单
$('#bill_spForm').click(function(){
    var loanSubjectId = $('select[name="loanSubjectId"]').val();
    if(loanSubjectId == ""){
        $.uiAlert({type: "danger", textHead: '保存失败', text: "未选择放款主体", time: 3});
        return false;
    }
    reckon.sl_discountDays();
    var flag = true;
    var flag_1 = $('#billAll_info_form').form(settingsForm).form("validate form");
    $("#bill_info_form .bill_info").each(function (i,ele) {
        draw_Time = $(ele).find('input[name="drawTime"]').val();
        due_Time = $(ele).find('input[name="dueDate"]').val();
        bill_info_amount = $(ele).find('input[name="amount"]').val();
        min_cost_rate = $(ele).find('input[name="minCost"]').val();
        var s_flag = $(this).form(formSettings).form("validate form");
        flag = flag && s_flag && flag_1;
    });
    if(flag){
        reckonAmount();
        $(document).api({
            on:'now',
            action:'add bill loan submit',
            method:'POST',
            beforeSend:function(settings){
                var $thisStep1 = $("#addForm");
                var $thisStep3 = $('#billAll_info_form');
                var billLoanRepayStr = [], medias = [], intermediary = [], errors = [];
                var is_intermediary = $('#js_isIntermediary').prop('checked');

                //step1
                settings.data["productId"] = prdId;
                settings.data["saleId"] = $thisStep1.find('input[name="saleId"]').val();
                settings.data["loanSubjectId"] =$thisStep1.find('select[name="loanSubjectId"]').val();
                settings.data["masterBorrowerId"] = $thisStep1.find('input[name="masterBorrowerId"]').val();
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

                settings.data['loanId'] = loanId;
                //step2
                $('.bill_info ').each(function(i,ele){
                    var $this = $(ele);
                    billLoanRepayStr.push({
                        'billNo':$this.find('input[name="billNo"]').val(),
                        'drawTime':$this.find('input[name="drawTime"]').val(),
                        'payer':$this.find('input[name="payer"]').val(),
                        'payerId':$this.find('input[name="payerId"]').val(),
                        'payee':$this.find('input[name="payee"]').val(),
                        'amount':$this.find('input[name="amount"]').val(),
                        'interest':$this.find('input[name="interest"]').val(),
                        'dueDate':$this.find('input[name="dueDate"]').val(),
                        'bankName':$this.find('input[name="bankName"]').val(),
                        'bankAddress':$this.find('input[name="bankAddress"]').val(),
                        'overdueDays':$this.find('input[name="overdueDays"]').val(),
                        'riskRank':$this.find('select[name="riskRank"]').val(),
                        'disDate':$this.find('input[name="disDate"]').val(),
                        'disDays':$this.find('input[name="disDays"]').val(),
                        'actualDueDate':$this.find('input[name="actualDueDate"]').val(),
                        'costRate':$this.find('input[name="costRate"]').val(),
                        'intermediaryFee':$this.find('input[name="intermediaryFee"]').val(),
                        'minCost':$this.find('input[name="minCost"]').val(),
                        'payerAccount':$this.find('input[name="payerAccount"]').val(),
                        'payeeAccount':$this.find('input[name="payeeAccount"]').val(),
                        'payeeBankName':$this.find('input[name="payeeBankName"]').val(),
                        'depositRate':$this.find('input[name="depositRate"]').val(),
                        'depositFlag':$this.find('input[name="depositFlag"]').val()
                    })
                });
                settings.data["billLoanRepayStr"] = JSON.stringify(billLoanRepayStr);
                //step3
                settings.data['totalAmount'] = $thisStep3.find('input[name="totalAmount"]').val();
                settings.data['interest'] = $thisStep3.find('input[name="interest"]').val();
                settings.data['discountTime'] = $thisStep3.find('input[name="discountTime"]').val();
                settings.data['accountName'] = $thisStep3.find('input[name="accountName"]').val();
                settings.data['accountBank'] = $thisStep3.find('input[name="accountBank"]').val();
                settings.data['accountNo'] = $thisStep3.find('input[name="accountNo"]').val().replace(/\s/g,"");
                settings.data['amount'] = $thisStep3.find('input[name="amount"]').val();
                settings.data['intermediaryId'] = $thisStep3.find('input[name="intermediaryId"]').val();
                settings.data['intermediaryTotalFee'] =$thisStep3.find('input[name="intermediaryTotalFee"]').val();

                settings.data['legalRepresentative'] = $thisStep3.find('input[name="legalRepresentative"]').val();
                settings.data['legalRepresentativePhone'] = $thisStep3.find('input[name="legalRepresentativePhone"]').val();
                settings.data['linkman'] = $thisStep3.find('input[name="linkman"]').val();
                settings.data['linkmanPhone'] = $thisStep3.find('input[name="linkmanPhone"]').val();
                settings.data['residence'] = $thisStep3.find('input[name="residence"]').val();

                if(is_intermediary){
                    intermediary.push({
                        id:$thisStep3.find('input[name="intermeId"]').val(),
                        name:$thisStep3.find('input[name="name"]').val(),
                        idNumber:$thisStep3.find('input[name="idNumber"]').val(),
                        phone:$thisStep3.find('input[name="phone"]').val(),
                        bank:$thisStep3.find('input[name="bank"]').val(),
                        account:$thisStep3.find('input[name="account"]').val(),
                        address:$thisStep3.find('input[name="address"]').val(),
                    });
                    settings.data['intermediaryStr'] = JSON.stringify(intermediary);
                }else{
                    settings.data['intermediaryStr'] = '';
                }

                if(errors.length == 0) {
                    return settings;
                } else {
                    $(".ui.error.message ul").html(errors.join(""));
                    $("#addForm").addClass("error");
                    return false;
                }
            },
            onSuccess:function(response){
                $.uiAlert({
                    type: "success",
                    textHead: '保存成功',
                    text: '',
                    time: 2,
                    onClosed:function(){
                        window.location.href = comeFrom;
                    }
                });
            },
            onFailure:function(response){
                $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 4});
            }
        });
    }
});


//数据填充
function renderBillInfo(){
    $(document).api({
        on:"now",
        action: "query bill",
        method: "POST",
        beforeSend:function(settings){
            settings.data["loanId"] = loanId;
            return settings;
        },
        onSuccess:function(response){
            //企业资料填充
            if(loanId !=""){
                var _data = response;
                _data.media =function(){
                    return _data.productMediaAttachList;
                };
                _data.code = function(){
                    return _data.billLoan.loan.code;
                };
                _data.url = function(){
                    if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                        return true;
                    }else{
                        return false;
                    }
                };

                var $riskFormTemplate = utils.render("#billMediaTemplate", _data);
                $("#billMedia").html($riskFormTemplate);
                bindUpload()//渲染后绑定事件
                upArryList();//创建数组
                var n = $('.js-uploadBtn').length;
                var detail = [];
                for(var i=0;i<n;i++){
                    $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
                        mediaDetail[i].push({
                            url:$(ele).attr("href"),
                            attachName:$(ele).text()
                        })
                    });
                    detail.push(JSON.stringify(mediaDetail[i]));
                    $('tr.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail[i]);
                }
            }
            //子票据信息数据填充
            var n = response.billLoanRepayList.length ;
            if(n>1){
                for(var i=1;i<n;i++){
                    addBillInfo();//clone
                    bindDropdown();//绑定下拉
                }
            }
            if(n>0){
                var data_1 = response.billLoanRepayList;
                for(var l=0;l<n;l++){
                    var val_1 = data_1[l];
                    var val_2 = val_1.loanRepay;
                    var $thisBillInfo = $('.bill_info:eq('+l+')');
                    var text_label = $thisBillInfo.find('.js-text');
                    if(val_1['depositFlag']== '1'){
                        $thisBillInfo.find('.js-textName').text('打款利息（元）');
                        text_label.text('打款利率');
                        $thisBillInfo.find('.js-mustChange1').attr('name','interest');
                        $thisBillInfo.find('.js-mustChange2').attr('name','depositRate');
                    }else{
                        $thisBillInfo.find('.js-textName').text('打款利率');
                        text_label.text('打款利息（元）');
                        $thisBillInfo.find('.js-mustChange1').attr('name','depositRate');
                        $thisBillInfo.find('.js-mustChange2').attr('name','interest');
                    }
                    $('.bill_info:eq('+l+') input').each(function(i,ele){
                        var name = $(ele).attr('name');

                        if(name == 'drawTime' || name == 'dueDate' || name =='actualDueDate' || name =='disDate'){
                            $(ele).val(moment(val_1[name]).format('YYYY-MM-DD'));
                        }else{
                            $(ele).val(val_1[name]);
                        }

                        for(m in val_2){
                            if(m != 'overdueDays'){
                                if(m == name){
                                    if(m == 'drawTime' || m == 'dueDate' || m =='actualDueDate' || m =='disDate'){
                                        $(ele).val(moment(val_2[m]).format('YYYY-MM-DD'));
                                    }else{
                                        $(ele).val(val_2[m]);
                                    }
                                }
                            }
                        }
                    });

                    $('.bill_info:eq('+l+') select').each(function(i,ele){
                       var name = $(ele).attr('name');
                        for(j in val_1){
                            if(j == name){
                                $(ele).find('option[value='+val_1[j]+']').attr('selected',true);
                            }
                        }
                    });
                }
            }

            //付款信息数据填充
            $('#billAll_info_form input').each(function(i,ele){
                var name = $(ele).attr('name');
                if(!response.billLoan.totalAmount){
                    reckonAmount();
                }else{
                    $('#billAll_info_form input[name="amount"]').val(response.billLoan.loan.amount);
                }
                if(name in response.billLoan){
                    if(name =='discountTime'){
                        $(ele).val(moment(response.billLoan[name]).format('YYYY-MM-DD'));
                    }else{
                        $(ele).val(response.billLoan[name]);
                    }
                }else if(response.intermediary && name in response.intermediary){
                    $(ele).val(response.intermediary[name]);
                }
            });

            //付款信息-户名-账号-开户行
            if(response.loanBorrower){
                var loan_borrower = response.loanBorrower;
                //新老数据判断，有一个不为空则取loanBorrower数据
                if(loan_borrower.accountName!='' || loan_borrower.account!='' || loan_borrower.bankName!=''){
                    var $this = $('#billAll_info_form');
                    $this.find('input[name="accountName"]').val(loan_borrower.accountName);
                    $this.find('input[name="accountNo"]').val(loan_borrower.account);
                    $this.find('input[name="accountBank"]').val(loan_borrower.bankName);

                    $this.find('input[name="linkman"]').val(loan_borrower.linkman);
                    $this.find('input[name="legalRepresentative"]').val(loan_borrower.legalRepresentative);
                    $this.find('input[name="legalRepresentativePhone"]').val(loan_borrower.legalRepresentativePhone);
                    $this.find('input[name="linkmanPhone"]').val(loan_borrower.linkmanPhone);
                    $this.find('input[name="residence"]').val(loan_borrower.residence);

                }
            }

            //
            if(response.intermediary && response.intermediary!=''){
                $('#js_isIntermediary').attr('checked',true);
                var _data = response.intermediary;
                var $this = $('#billAll_info_form');
                $this.find('input[name="intermeId"]').val(_data.id);
                $this.find('input[name="name"]').val(_data.name);
                $this.find('input[name="idNumber"]').val(_data.idNumber);
                $this.find('input[name="phone"]').val(_data.phone);
                $this.find('input[name="bank"]').val(_data.bank);
                $this.find('input[name="account"]').val(_data.account);
                $this.find('input[name="address"]').val(_data.address);
            }else{
                $('#js_isIntermediary').attr('checked',false);
                $('#js_intermediaryInfo').addClass('ks-hidden');
            }

            reckon.sl_discountDays();//计算
        },
        onFailure:function(response){
            $.uiAlert({type: "danger", textHead: '获取数据失败', text: response.msg, time: 3});
        }
    })
}
if(loanId != ''){
    renderBillInfo();
}