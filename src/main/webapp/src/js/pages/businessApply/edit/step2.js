
//一些下拉框的选项 此处 "11"暂时写死$("#stepData").data("repay_date_type")
var $step2Template = utils.render("#step2Template", {
    "termTypeChoices": getBinChoices("termType", $("#stepData").data("term_type")+"", $("#stepData").data("loan_term_type")),
    "repayMethodChoices": getBinChoices("repayMethod", $("#stepData").data("repay_method")+"", $("#stepData").data("loan_repay_method")),
    "repayDateTypeChoices": getBinChoices("repayDateType", "11", $("#stepData").data("loan_repay_date_type")),
    "calculateDayTypeChoices": getTypeChoices($("#stepData").data("loan_calculate_method_about_day"))
});
$("#divStep2").html($step2Template);

var minMonths = parseInt($("#stepData").data("min_months"));
var maxMonths = parseInt($("#stepData").data("max_months"));
var minDays = parseInt($("#stepData").data("min_days"));
var maxDays = parseInt($("#stepData").data("max_days"));

function getBinChoice(type,value) {
    /* 根据类型和二进制代码显示下拉选项 */
    var h = "";
    var termType = $('select[name="termType"]').val();
    if(termType == 'DAYS'){
        code = $("#stepData").data("interest_day_type")+"";
    }else if(termType == 'MOTHS'){
        code = $("#stepData").data("interest_month_type")+"";
    }else{
        code = $("#stepData").data("interest_type")+"";
    }
    $('select[name="loanLimitType"]').html('');
    $.each(code.split(""), function(i, item) {
        if(item == "1") {
            var option = binChoices[type][i];
            if(value == option["value"]) {
                $('select[name="loanLimitType"]').append('<option value="'+option["value"]+'" selected></option>');
            } else {
                $('select[name="loanLimitType"]').append('<option value="'+option["value"]+'"></option>');
            }
        }
    });
}
getBinChoice('interestType',$("#stepData").data("loan_limit_type"));


function selectForPaymethod(){
    var _termTypeVal = $('select[name="termType"]').find('option:selected').val();
    var $repayMethod = $('select[name="repayMethod"]');
    var selectRepayethod = function (value) {
        var arry = [];
        if (value.indexOf('/') > -1) {
            arry = value.split('/');
            $repayMethod.find('option').attr('disabled', false);
            for (n = 0; n < arry.length; n++) {
                var _value = arry[n];
                $repayMethod.find('option[value="' + _value + '"]').attr('disabled', true);
            }
        } else {
            $repayMethod.find('option').attr('disabled', false);
            $repayMethod.find('option[value="' + value + '"]').attr('disabled', true);
        }
    };

    if (_termTypeVal == 'DAYS') {
        selectRepayethod('INTEREST_MONTHS/EQUAL_INSTALLMENT');
        $("#minInterestAmountDiv").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").removeClass("ks-hidden");
    } else if (_termTypeVal == 'MOTHS') {
        selectRepayethod('INTEREST_DAYS');
        $("#calculateMethodAboutDay").attr("disabled", true);
    } else {
        $("#minInterestAmountDiv").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").removeClass("ks-hidden");
        selectRepayethod('INTEREST_MONTHS/EQUAL_INSTALLMENT');
    }
};

changeRepayMethod();
selectForPaymethod();

var step2FormOptions = {
    fields: {
        'term': {
            identifier: 'term',
            rules: [{
                type: 'empty',
                prompt: '借款期限不能为空'
            }, {
                type: 'validateTerm1',
                prompt: '借款期限为整数，且在' + minMonths + '-' + maxMonths + '个月'
            }, {
                type: 'validateTerm2',
                prompt: '借款期限为整数，且在' + minDays + '-' + maxDays + '天'
            }, {
                type: 'validateTerm3',
                prompt: '借款期限格式不对'
            }]
        },
        'repayMethod':{
            rules: [{
                type: 'empty',
                prompt: '请选择还款方式'
            }]
        },
        'interestAmount': {
            identifier: 'interestAmount',
            rules: [{
                type: 'empty',
                prompt: '借款利息不能为空'
            }, {
                type: 'validateInterestAmount[FIXED_DATE]',
                prompt: '借款利息（金额计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_amount")) + '元)'
            },{
                type: 'validateInterestAmount[DAYS]',
                prompt: '借款利息（金额计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_day_amount")) + '元)'
            },{
                type: 'validateInterestAmount[MOTHS]',
                prompt: '借款利息（金额计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_month_amount")) + '元)'
            },{
                type:'newCanBeDecimal[14,2]',
                prompt:'金额计息2位小数，小数点前14位整数'
            }]
        },
        'interestRate': {
            identifier: 'interestRate',
            rules: [{
                type: 'empty',
                prompt: '借款利息不能为空'
            }, {
                type: 'validateInterestRate[FIXED_DATE]',
                prompt: '借款利息（比例计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_rate")) + '%)'
            },{
                type: 'validateInterestRate[DAYS]',
                prompt: '借款利息（比例计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_day_rate")) + '%)'
            },{
                type: 'validateInterestRate[MOTHS]',
                prompt: '借款利息（比例计息）不正确(不小于' + parseFloat($("#stepData").data("min_interest_month_rate")) + '%)'
            },{
                type:'newCanBeDecimal[14,10]',
                prompt:'比例计息10位小数，小数点前14位整数'
            }]
        },
        'loanLimitType': {
            identifier: 'loanLimitType',
            rules: [{
                type:'changeMethod',
                prompt:'还款方式为“等额本息”，借款利率只能选择“比例计息（%）'
            }]
        },
        'minInterestAmount':{
            identifier:'minInterestAmount',
            rules:[{
                type:'empty',
                prompt: '最小利息金额不能为空(可填0)'
            },{
                type:'validateNumFloat[0-99999999.99]',
                prompt:'最多为2位小数,在0-99999999.99之间'
            }]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};


// 请求更新Loan费用
function ajaxUpdateLoanFee(callback) {
    $(document).api({
        on:"now",
        action: "update loan fee",
        method: "post",
        beforeSend: function(settings) {
            var loan_id = $("#stepData").data("loan_id");
            settings.data["id"] = loan_id;
            var fees = [];
            $("tr.feeItem").each(function() {
                var $item = $(this);
               var amount = $item.find("input[name='feeAmount_"+ $item.find("input[name='feeId']").val()+"']").val();
                if(amount==undefined){
                    amount = null;
                }
                if($item.find("input[name='chargeType']").val()=="LOAN_REQUEST_INPUT"){
                    fees.push({
                        'loanId': loan_id,
                        'id': $item.find("input[name='feeId']").val(),
                        'feeName': $item.find("input[name='feeName']").val(),
                        'formulaText': $item.find("input[name='formulaText']").val(),
                        'feeType': $item.find("input[name='feeType']").val(),
                        'feeCycle': $item.find("input[name='feeCycle']").val(),
                        'chargeType': $item.find("input[name='chargeType']").val(),
                        'chargeNode': $item.find("input[name='chargeNode']").val(),
                        'feeRate': $item.find("input[name='feeRate']").val(),
                        'feeAmount': amount,
                        'minFeeAmount': $item.find("input[name='minFeeAmount']").val(),
                        'maxFeeAmount': $item.find("input[name='maxFeeAmount']").val()
                    })
                }
            });
            settings.data["fees"] = JSON.stringify(fees);
            return settings;
        },
        onSuccess: function (response) {
            queryLoanFee($("#step2Form select[name='repayMethod']").val());
            callback();
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
        }
    });
}

function isInteger(obj) {
    var re = /^[1-9]+[0-9]*]*$/;
    return re.test(obj)
}

function compareDate(strDate1,strDate2) {
    var date1 = new Date(strDate1.replace(/\-/g, "\/"));
    var date2 = new Date(strDate2.replace(/\-/g, "\/"));
    return date1-date2;
}

function compare(date) {
    var now = new Date;
    var d = new Date(date);
    if (now > d) {
        return false;
    } else if (now < d) {
       return true;
    } else {
        return false
    }
}
//验证还款方式和借款利息
$.fn.form.settings.rules.changeMethod = function(value){
    var repayMehodVal = $('#step2Form select[name="repayMethod"]').val();
    if(repayMehodVal == 'EQUAL_INSTALLMENT'){
        if(value == 'FIX_RATE'){
            return true;
        }else{
            return false;
        }
    }else{
        return true;
    }
};

//借款利率
drop_list = {
    DAYS: {FIX_AMOUNT:"金额计息(元)/天",FIX_RATE:"比例计息(%)/天"},
    MOTHS: {FIX_AMOUNT:"金额计息(元)/月",FIX_RATE:"比例计息(%)/月"},
    FIXED_DATE:{FIX_AMOUNT:"金额计息(元)/天",FIX_RATE:"比例计息(%)/天"}

};
//最小值判断
termList = {
    DAYS: {amount:'min_interest_day_amount',rate:'min_interest_day_rate'},
    MOTHS: {amount:'min_interest_month_amount',rate:'min_interest_month_rate'},
    FIXED_DATE: {amount:'min_interest_amount',rate:'min_interest_rate'}
};




//初始化借款利率
function s_changeRate(){
    var termType = $('select[name="termType"] option:selected').val();
    $('select[name="loanLimitType"] option').each(function(i,ele){
        var val = $(ele).val();
        var txt = drop_list[termType][val];
        $(ele).text(txt);
    });
}
s_changeRate();

// 借款期限类型的变换 & 借款利率
$("#step2Form select[name='termType']").change(function() {
    rateTime();//收息时间
    selectForPaymethod();
    getBinChoice('interestType');
    var termType = $(this).val();
    if (termType == "DAYS") {
        s_changeRate();
        $("#minInterestAmountDiv").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").attr("disabled", false);
    }
    // if (termType != "DAYS"){
    //     $("#minInterestAmountDiv").addClass("ks-hidden");
    //     $('input[name="minInterestAmount"]').val(0.00);
    // }
    if(termType == "MOTHS"){
        $("#minInterestAmountDiv").addClass("ks-hidden");
        $("#calculateMethodAboutDay").addClass("ks-hidden");
        $('input[name="minInterestAmount"]').val(0.00);
        $("#calculateMethodAboutDay").attr("disabled", true);
        s_changeRate();
    }
    if(termType == "FIXED_DATE") {
        s_changeRate();
        $("#minInterestAmountDiv").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").removeClass("ks-hidden");
        $("#calculateMethodAboutDay").attr("disabled", false);
        $("#step2Form input[name='term']").val("");
        $('select[name="repayMethod"]').find('option[value=""]').prop('selected',true);
        $("#step2Form input[name='term']").on('click', function() {
            laydate({min:laydate.now(1)});
        });
    }
    if(termType != "FIXED_DATE"){
        $("#step2Form input[name='term']").val("");
        $('select[name="repayMethod"]').find('option[value=""]').prop('selected',true);
        $("#step2Form input[name='term']").off();
    }
    addValue();
    changeStatus();
});
if($("#step2Form select[name='termType']").val() == "FIXED_DATE") {
    $("#step2Form input[name='term']").on('click', function() {
        laydate({min:laydate.now(1)});
    });
}

$.fn.form.settings.rules.validateTerm1=function(value){
    var termType = $("#step2Form select[name='termType']").val();
    if(termType == "MOTHS") {
        var minMonths = parseInt($("#stepData").data("min_months"));
        var maxMonths = parseInt($("#stepData").data("max_months"));
        if(isNaN(value) || parseInt(value) < minMonths || parseInt(value) > maxMonths||!isInteger(value)) {
            return false;
        } else {
            return true;
        }
    }else{
        return true;
    }
};
$.fn.form.settings.rules.validateTerm2=function(value){
    var termType = $("#step2Form select[name='termType']").val();
    if(termType == "DAYS") {
        var minDays = parseInt($("#stepData").data("min_days"));
        var maxDays = parseInt($("#stepData").data("max_days"));
        if(isNaN(value) || parseInt(value) < minDays || parseInt(value) > maxDays||!isInteger(value)) {
            return false;
        } else {
            return true;
        }
    }else{
        return true;
    }
};
$.fn.form.settings.rules.validateTerm3=function(value){
    var termType = $("#step2Form select[name='termType']").val();
    if(termType == 'FIXED_DATE'){
        var term = $("#step2Form input[name='term']").val();
        var re = /^[1-2][0-9][0-9][0-9]-(0[1-9]|1[012])-(0[1-9]|[12]\d|3[01])$/;
        if(re.test(term)&&compare(term)){
            return true;
        }else{
            Msg = '时间格式不正确';
            return false;
        }
    }else{
        return true;
    }
};


// 验证借款利息
$.fn.form.settings.rules.validateInterestAmount = function(value,dataVal) {
    var loanLimitType = $("#step2Form select[name='loanLimitType']").val();
    var termType = $("#step2Form select[name='termType']").val();
    if(dataVal != termType) {
        return true;
    }else{
        if(loanLimitType == "FIX_AMOUNT") {
            var minInterestAmount = parseFloat($("#stepData").data(''+termList[dataVal].amount+''));
            if(isNaN(value) || parseFloat(value) < minInterestAmount) {
                return false;
            } else {
                return true;
            }
        }else{
            return true
        }
    }
};

$.fn.form.settings.rules.validateInterestRate = function(value,dataVal) {
    var loanLimitType = $("#step2Form select[name='loanLimitType']").val();
    var termType = $("#step2Form select[name='termType']").val();
    if(dataVal != termType) {
        return true;
    }else {
        if (loanLimitType == "FIX_RATE") {
            var minInterestRate = parseFloat($("#stepData").data('' + termList[dataVal].rate + ''));
            if (isNaN(value) || parseFloat(value) < minInterestRate) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
};
var day_amount = $('#prdDate').data('day_amount'),
    day_rate = $('#prdDate').data('day_rate'),
    month_amount = $('#prdDate').data('month_amount'),
    month_rate = $('#prdDate').data('month_rate'),
    fixAmount = $('#prdDate').data('fix_amount'),
    fixRate = $('#prdDate').data('fix_rate'),
    loan_amount = $('#prdDate').data('loan_amount'),
    loan_rate = $('#prdDate').data('loan_rate')
var loanLimitType = $('#prdDate').data('loan_limittype');
var amount = $('input[name="interestAmount"]'),
    rate = $('input[name="interestRate"]');
function addValue(){

    var termType = $('select[name="termType"] option:selected').val();
    if(loanLimitType == '' || loanLimitType == undefined){
        if(termType == 'DAYS'){
            amount.val(day_amount);
            rate.val(day_rate);
        }else if(termType == 'MOTHS'){
            amount.val(month_amount);
            rate.val(month_rate);
        }else if(termType == 'FIXED_DATE'){
            amount.val(fixAmount);
            rate.val(fixRate);
        }
    }else{
        amount.val(loan_amount);
        rate.val(loan_rate);
    }
}
function changeStatus(){
    var loanLimitType = $('select[name="loanLimitType"]').val();
    if(loanLimitType == 'FIX_AMOUNT'){
        amount.removeClass('ks-hidden').attr('disabled',false);
        rate.addClass('ks-hidden').attr('disabled',true);
    }else if(loanLimitType == 'FIX_RATE'){
        amount.addClass('ks-hidden').attr('disabled',true);
        rate.removeClass('ks-hidden').attr('disabled',false);
    }
}

$('select[name="loanLimitType"]').on('change',function(){
    changeStatus()
});
addValue();
changeStatus();

/*
* 还款方式change;
* 关联还款计划/收息时间/最小利息金额
* */
$("#step2Form select[name='repayMethod']").change(function() {
    $("#step2Form select[name='repayDateType']").removeClass('disabled');
    var termType = $("#step2Form select[name='termType']").val();
    var repayMethod = $(this).val();
    changeRepayMethod(termType,repayMethod);
    if(repayMethod == "INTEREST_MONTHS") {
        $("#step2Form select[name='repayDateType']").removeClass('disabled');
    }else if(repayMethod == 'INTEREST_DAYS'){
        $("#step2Form select[name='repayDateType']").val("REPAY_PRE");
        $("#step2Form select[name='repayDateType']").addClass('disabled');
    } else {
        $("#step2Form select[name='repayDateType']").val("REPAY_SUF");
        $("#step2Form select[name='repayDateType']").addClass('disabled');
    }
    queryLoanFee(repayMethod);
});

/*
 * 根据还款方式显示最小利息金额
 * value1 : '借款期限'
 * value2 : '还款方式'
 * */
function changeRepayMethod(value1,value2){
    if(!value1 && !value2){
        value1 = $('select[name="termType"]').val();
        value2 = $('select[name="repayMethod"]').val();
    }
    if(value1 == 'MOTHS' && value2 == 'BULLET_REPAYMENT'){
        $('#minInterestAmountDiv').removeClass('ks-hidden');
        $('#minInterestAmountDiv').find('select[name="minInterestAmount"]').attr('disabled',false);
    }else if(value1 == 'MOTHS' && value2 != 'BULLET_REPAYMENT'){
        $('#minInterestAmountDiv').addClass('ks-hidden');
        $('#minInterestAmountDiv').find('select[name="minInterestAmount"]').attr('disabled',true);
    }
}
changeRepayMethod();

//收息时间
var rateTime = function(){
    if($("#step2Form select[name='repayMethod']").val() != "INTEREST_MONTHS") {
        $("#step2Form select[name='repayDateType']").addClass('disabled');
    }else if($("#step2Form select[name='repayMethod']").val() == "INTEREST_DAYS"){
        $("#step2Form select[name='repayDateType']").val("REPAY_PRE");
        $("#step2Form select[name='repayDateType']").addClass('disabled');
    }else{
        $("#step2Form select[name='repayDateType']").removeClass('disabled');
    }
};
rateTime();


function accMul(arg1,arg2)
{
    var m=0,s1=arg1.toString(),s2=arg2.toString();
    try{m+=s1.split(".")[1].length}catch(e){}
    try{m+=s2.split(".")[1].length}catch(e){}
    return Number(s1.replace(".",""))*Number(s2.replace(".",""))/Math.pow(10,m)
}

function accDiv(arg1,arg2){
    var t1=0,t2=0,r1,r2;
    try{t1=arg1.toString().split(".")[1].length}catch(e){}
    try{t2=arg2.toString().split(".")[1].length}catch(e){}
    with(Math){
        r1=Number(arg1.toString().replace(".",""))
        r2=Number(arg2.toString().replace(".",""))
        return (r1/r2)*pow(10,t2-t1);
    }
}
queryLoanFee($("#step2Form select[name='repayMethod']").val());

function queryLoanFee(repayMethod){
    //请求获取当前用户的数据
    $(document).api({
        on: "now",
        method: 'get',
        action: "query loan fee",
        data: {
            loanId: utils.getUrlParam("id"),
            loanRepayMethod: repayMethod
        },
        onSuccess: function (data) {
            var _data = data.data;
            $("#edit_loan_fee").html("");
            var _h = "";
            if(_data){
                for(var i=0; i<_data.length; i++){
                var chargeType  = "";
                    if(_data[i].chargeType=="FIXED_AMOUNT"){
                        chargeType = _data[i].feeAmount+"元";
                    }else if(_data[i].chargeType=="LOAN_REQUEST_INPUT"){
                        chargeType = "("+_data[i].minFeeAmount+"~"+_data[i].maxFeeAmount+")元"
                    }else{
                        chargeType = _data[i].feeRate+"%";
                    }
                 var daysText = "";
                 var inputText = "";
                    if ("OVERDUE_FEE" == _data[i].feeType || "PREPAYMENT_FEE" == _data[i].feeType) {
                        inputText = "";
                        daysText = "*天数";
                    } else {
                        if (_data[i].chargeType == "FIXED_AMOUNT") {
                            inputText = accounting.formatNumber(_data[i].feeAmount, 2, ",") + "元";
                        } else if (_data[i].chargeType == "LOAN_REQUEST_INPUT") {
                            inputText += "<span class='red-required'>*</span>";
                            inputText += "<div class='field required inline-block'>";
                            inputText += "<div class='ui right labeled input'>";
                            inputText +=
                                "<input class='feeAmount' type='number' data-min_fee_amount="
                                + _data[i].minFeeAmount + " data-max_fee_amount="
                                + _data[i].maxFeeAmount + " name='feeAmount_" + _data[i].id
                                + "' value='" + _data[i].feeAmount + "'>";
                            inputText += " <div class='ui basic label'>元</div>";
                            inputText += "  </div>";
                            inputText += "  </div>";
                        } else if (_data[i].chargeType == "LOAN_AMOUNT_RATE") {
                            var amount = $("#step2Form").find("input[name='amount']").val();
                            inputText =
                                accounting.formatNumber(accounting.toFixed(
                                    eval("+(" + amount + "*" + _data[i].feeRate + ")/100"), 2), 2,
                                                        ",") + "元"; //accDiv(accMul(amount,_data[i].feeRate),100);
                            if (inputText == undefined) {
                                inputText = "";
                            }
                        }
                    }
                    _h += "<tr class='feeItem'>";
                    _h += " <input type='hidden' name='feeId' value="+_data[i].id+" />";
                    _h += " <input type='hidden' name='feeName' value="+_data[i].feeName+" />";
                    _h += " <input type='hidden' name='formulaText' value="+_data[i].formulaText+"  />";
                    _h += " <input type='hidden' name='feeType' value="+_data[i].feeType+"  />";
                    _h += " <input type='hidden' name='feeCycle'  value="+_data[i].feeCycle+"   />";
                    _h += "<input type='hidden' name='chargeType'  value="+_data[i].chargeType+"  />";
                    _h += " <input type='hidden' name='chargeNode'  value="+_data[i].chargeNode+" />";
                    _h += " <input type='hidden' name='feeRate'  value="+_data[i].feeRate+" />";
                    _h += " <input type='hidden' name='minFeeAmount'  value="+_data[i].minFeeAmount+" />";
                    _h += " <input type='hidden' name='maxFeeAmount'  value="+_data[i].maxFeeAmount+" />";
                    _h += "<td>"+enums.feeType[_data[i].feeType]+"</td>";
                    _h += "<td>"+enums.feeCycleType[_data[i].feeCycle]+"</td>";
                    _h += "<td> "+enums.feeChargeType[_data[i].chargeType]+" "+chargeType+daysText+"</td>";
                    _h +=" <td>"+enums.feeChargeNode[_data[i].chargeNode]+"</td>";
                    _h += "<td> "+inputText+"</td>";
                   _h+= "</tr>";
                }

            }
            $("#edit_loan_fee").html( _h);
            // 验证收费金额
            $(".feeAmount").each(function() {
                var minFeeAmount = $(this).data("min_fee_amount");
                var maxFeeAmount = $(this).data("max_fee_amount");
                step2FormOptions['fields'][$(this).attr("name")] = {
                    identifier: $(this).attr("name"),
                    rules: [{
                        type: 'empty',
                        prompt: '收费金额不能为空'
                    }, {
                        type: 'between['+ minFeeAmount + ',' + maxFeeAmount +']',
                        prompt: '收费金额不正确('+ minFeeAmount + '~' + maxFeeAmount +'元)'
                    }]
                }
            });


            // renderApplyTpml();
        }
    });
}















$("#btn-submit-step2").click(function() {
    $("#step2Form").form(step2FormOptions).api({
        action: "update loan",
        method: 'POST',
        serializeForm: true,
        beforeSend: function(settings) {
            var $this  = $('#step2Form');

            var _step = "";
            if($("#linkStep1").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            _step += "1"
            if($("#linkStep3").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            if($("#linkStep4").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            settings.data["step"] = _step;
            for(i in settings.data){
                var val = settings.data[i];
                settings.data[i] = $.trim(val);
            }
            return settings;
        },
        onSuccess: function (response) {
            ajaxUpdateLoanFee(function() {
                $("#linkStep2").addClass("completed");
                goStep("Step3");
            });
        },
        onFailure: function(response) {
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
        }
    });
    $("#step2Form").submit();
})




