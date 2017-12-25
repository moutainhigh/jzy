/**
 * Created by yangzb01 on 2017/2/27.
 */
var loanId = $("#stepData").data("loan_id");
var loanTmp = $("#stepData").data("loan_type");

//添加票据单
function addBillInfo(){
    var cloneHtml = $('#demo').find('.js-bill_demo').addClass('bill_info');
    $('.js-canBeAdd').append(cloneHtml.clone());
    $('#demo .js-bill_demo').removeClass('bill_info');
    $('.js-canBeAdd').find('.bill_info').removeClass('js-bill_demo ks-hidden');
    $('.js-canBeAdd').find('.js_removeHouse').removeClass('ks-hidden');
    if($('#discountTime').val() !=''){
        $('input[name="disDate"]').val($('#discountTime').val());
    }
    $('#bill_info_form .js-billNum').each(function(i,ele){
        var i = i+2;
        $(this).html(i);
    });
}

if($('#discountTime').length >0){
    laydate({
        elem:'#discountTime',
        event: 'click',
        choose: function(dates){
            $('input[name="disDate"]').val(dates);
            $('input[name="discountTime"]').val(dates);
        }
    });
}

function bindDropdown(){
    $('.combo.dropdown')
        .dropdown({
            action: 'select',
            onChange:function(value,ele){
                var text = ele.text();
                var $this = ele.parents('.bill_info')
                $this.find('.js-textName').html(text);
                $this.find('input[name="depositFlag"]').val(value);
                var text_label = $this.find('.js-text')
                if(value =='1'){
                    text_label.text('打款利率');
                    $this.find('.js-mustChange1').attr('name','interest');
                    $this.find('.js-mustChange2').attr('name','depositRate');
                }else{
                    text_label.text('打款利息（元）');
                    $this.find('.js-mustChange1').attr('name','depositRate');
                    $this.find('.js-mustChange2').attr('name','interest');
                }
            }
        });
}
bindDropdown();


// 付款人查询
$(document).on('click','.js-search',function(){
    $(this).search({
        apiSettings: {
            method: "post",
            url: $(document).api.settings.api['query enterprise'] + '?name={query}'
        },
        fields: {
            results: 'data',
            title: 'name',
            price: ''
        },
        onSelect: function (data) {
            $(this).parent().siblings().find("select[name='riskRank']").val(data.level);
            $(this).find('input').attr('value',data.name);
            $(this).find('input[name="payerId"]').attr('value',data.id);
            $(this).parents('.bill_info').find('input[name="costRate"],input[name="minCost"]').val(data.price);
            $(this).parents('.bill_info').find('input[name="costRate"]').attr('data-min_rate',data.price)
        }
    })
});

$("#payerSearch").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['query enterprise'] + '?name={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        price: ''
    },
    onSelect: function (data) {
        $(this).parent().siblings().find("select[name='riskRank']").val(data.level);
        $(this).find('input[name="payer"]').attr('value',data.name);
        $(this).find('input[name="payerId"]').attr('value',data.id);
        $(this).parents('.bill_info').find('input[name="costRate"],input[name="minCost"]').val(data.price);
    }
});

$('input[name="payer"]').on('input propertychange',function(){
    $(this).parents('.bill_info').find('input[name="minCost"]').val("");
})


$('.js-add_bill_info').click(function(){
    addBillInfo();//clone
    bindDropdown();//绑定下拉
});

//删除票据单
$(document).on('click','.js_removeHouse',function(){
   $(this).parents('.bill_info').remove();
});

//校验到期日
$.fn.form.settings.rules.dueTimeCompare = function(value){
    if(draw_Time!=''){
        var dueDate = Date.parse(value);
        var drawDate = Date.parse(draw_Time);
        if(dueDate > drawDate){
            return true;
        }else{
            return false;
        }
    }else{
        return true;
    }
};

//校验出票日
$.fn.form.settings.rules.drawTimeCompare = function(value){
    if(value!=''){
        var drawDate = Date.parse(value);
        var dueDate = Date.parse(due_Time);
        if(dueDate > drawDate){
            return true;
        }else{
            return false;
        }
    }else{
        return true;
    }
};

//校验宽限期
$.fn.form.settings.rules.Number = function(value){
    return /^\d{1,9}$/.test(value) || value == "";
};

//校验付款人为空需校验
$.fn.form.settings.rules.payerIsNull = function(value){
    if(loanTmp =='PIAOJU' && value == '') {
        return false;
    }else{
        return true;
    }
};


//校验payBankIsNull
$.fn.form.settings.rules.payBankIsNull = function (value){
    if(loanTmp =='YINPIAO' && value == '') {
        return false;
    }else{
        return true;
    }
};

//校验子票据信息比较金额利息
$.fn.form.settings.rules.comInterest = function (value){
    var interest = parseFloat(value);
    if(interest >= bill_info_amount){
        return false;
    }else{
        return true;
    }
};

//成本报价
$.fn.form.settings.rules.cost_minRate = function(value){
    if(min_cost_rate != ''){
        var cost_rate = parseFloat(min_cost_rate);
        var val = parseFloat(value);
        if(val < cost_rate){
            return false;
        }else{
            return true;
        }
    }else {
        return true;
    }
};

$.fn.form.settings.rules.compareTime = function(value){
    var dueDate = Date.parse(due_Time);
    var discountTime = Date.parse(value);
    if(discountTime >= dueDate){
        return false;
    }else{
        return true
    }
}


//票据信息校验
var formSettings = {
    inline: true,
    fields:{
        billNo:{
            identifier:'billNo',
            rules:[{
                type: 'empty',
                prompt: '票号不能为空'
            }]
        }
        ,
        'drawTime':{
            identifier:'drawTime',
            rules:[{
                type: 'empty',
                prompt: '出票日期不能为空'
            },{
                type:'drawTimeCompare',
                prompt:'出票日期应早于到期日'
            }]
        },
        'payer':{
            identifier:'payer',
            rules:[{
                type: 'empty',
                prompt: '不能为空'
            }]
        },
        'riskRank':{
            identifier:'riskRank',
            rules:[{
                type:'empty',
                prompt:'请选择资信等级'
            }]
        },
        'amount':{
            identifier:'amount',
            rules:[{
                type: 'empty',
                prompt: '出票金额不能为空'
            },{
                type:'newCanBeDecimal[14,2]',
                prompt:'出票金额整数位不超过14位，小数位不超过2位，且不为负数'
            }]
        },
        'interest':{
            identifier:'interest',
            rules:[{
                type:'empty',
                prompt: '打款利息不为空'
            },{
                type:'newCanBeDecimal[14,2]',
                prompt:'打款利息整数位不超过14位，小数位不超过2位'
            },{
                type:'comInterest',
                prompt:'打款利息应小于出票金额'
            }]
        },
        'dueDate':{
            identifier:'dueDate',
            rules:[{
                type: 'empty',
                prompt: '到期日不能为空'
            },{
                type:'dueTimeCompare',
                prompt:'到期日应迟于出票日期'
            }]
        },
        'bankName':{
            identifier:'bankName',
            rules:[{
                type: 'payBankIsNull',
                prompt: '付款行全称不为空'
            }]
        },
        'overdueDays':{
            identifier:'overdueDays',
            rules:[{
                type: 'empty',
                prompt: '宽限期不能为空'
            },{
                type:'Number',
                prompt:'宽限期为1-9位正整数'
            }]
        },
        'costRate':{
            identifier:'costRate',
            rules:[{
                type: 'empty',
                prompt: '成本报价不为空'
            },{
                type:'canBeDecimal[2]',
                prompt:'2位小数'
            },{
                type:'cost_minRate',
                prompt:'不能小于自动填充的成本报价'
            }]
        },
        'intermediaryFee':{
            identifier:'intermediaryFee',
            rules:[{
                type: 'empty',
                prompt: '居间费不为空'
            }]
        },
        'disDate':{
            identifier:'disDate',
            rules:[{
                type: 'empty',
                prompt: '贴现日期不为空'
            },{
                type:'compareTime',
                prompt:'贴现日期不大于到期日'
            }]
        },
        'actualDueDate':{
            identifier:'actualDueDate',
            rules:[{
                type: 'empty',
                prompt: '实际到期日不为空'
            }]
        },
        'disDays':{
            identifier:'disDays',
            rules:[{
                type: 'empty',
                prompt: '实际贴现天数不为空'
            }]
        },
        'depositRate':{
            identifier:'depositRate',
            rules:[{
                type: 'empty',
                prompt: '打款利率不为空'
            }]
        },
        'payerAccount':{
            identifier:'payerAccount',
            rules:[{
                type: 'bankCard',
                prompt: '账号不正确（6-30位）'
            }]
        },
        'payee':{
            identifier:'payee',
            rules:[{
                type: 'empty',
                prompt: '不能为空'
            }]
        },
        payeeAccount:{
            identifier:'payeeAccount',
            rules:[{
                type: 'bankCard',
                prompt: '账号不正确（6-30位）'
            }]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};

//保存子表单数据
$('#bill_setp2Form').click(function(){
    reckon.sl_discountDays();
    var flag = true;
    $("#bill_info_form .bill_info").each(function (i,ele) {
        draw_Time = $(ele).find('input[name="drawTime"]').val();
        due_Time = $(ele).find('input[name="dueDate"]').val();
        bill_info_amount = $(ele).find('input[name="amount"]').val();
        min_cost_rate = $(ele).find('input[name="minCost"]').val();
        var s_flag = $(this).form(formSettings).form("validate form");
        flag = flag && s_flag;
    });
    if(flag){
        $('#bill_info_form').api({
            action: "add bill repay",
            method: 'POST',
            beforeSend:function(settings){
                var billLoanRepayStr = [];
                settings.data["loanId"] = loanId;
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
                for(n in billLoanRepayStr){
                    var val_step = billLoanRepayStr[n];
                    for(k in val_step){
                        val_step[k] = $.trim(val_step[k]);
                    }
                }
                settings.data["billLoanRepayStr"] = JSON.stringify(billLoanRepayStr);
                return settings;
            },
            onSuccess:function(response){
                reckonAmount();
                $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 3});
            },
            onFailure:function(response){
                $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 4});
            }
        });
        $("#bill_info_form").submit();
    }
});

