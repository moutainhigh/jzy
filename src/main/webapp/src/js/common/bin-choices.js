
// 二进制枚举, 顺序不能动, 后果自负
var binChoices = {
    termType: [
        {value: 'DAYS', name: '天'},
        {value: 'MOTHS', name: '月'},
        {value: 'FIXED_DATE', name: '固定时间还款'},
        {value: 'SEASONS', name:'季'}
    ],
    repayMethod: [
        {value: 'INTEREST_MONTHS', name: '先息后本（按月）'},
        {value: 'INTEREST_DAYS', name: '先息后本（按天）'},
        {value: 'EQUAL_INSTALLMENT', name: '等额本息'},
        {value: 'BULLET_REPAYMENT', name: '一次性还本付息'},
        {value: 'INTEREST_SEASONS', name:'按季付息，到期还本'}
    ],
    interestType: [
        {value: 'FIX_AMOUNT', name: '金额计息（元）'},
        {value: 'FIX_RATE', name: '比例计息（%）'},
    ],
    repayDateType: [
        {value: 'REPAY_PRE', name: '期初收息'},
        {value: 'REPAY_SUF', name: '期末收息'},
    ]
}


function getBinChoices(type, code, value) {
    /* 根据类型和二进制代码显示下拉选项 */
    var choices = [];
    if(code)
        $.each(code.split(""), function(i, item) {
            if(item == "1") {
                var option = binChoices[type][i];
                if(value == option["value"]) {
                    option["selected"] = true;
                } else {
                    option["selected"] = false;
                }
                choices.push(option);
            }
        });
    return choices;
}

var calculateDayType = [
        {value: 'CALCULATE_HEAD_AND_TAIL', name: '算头算尾'},
        {value: 'CALCULATE_HEAD_NOT_TAIL', name: '算头不算尾'},
        {value: 'CALCULATE_TAIL_NOT_HEAD', name: '算尾不算头'},
        
    ];

function getTypeChoices(value) {
    var choices = [];
    $.each(calculateDayType,function(i,item) {
        var option = item;
        if(value == option["value"]) {
            option["selected"] = true;
        } else {
            option["selected"] = false;
        }
        choices.push(option);
    });
    return choices;
}





var select = {
    /**
     * param{string} id
     * form表单ID
     *
     * */
    selectForPaymethod:function(id,isNull){
        var _termTypeVal = $('#'+id+' select[name="termType"]').find('option:selected').val();
        var $repayMethod = $('#'+id+' select[name="repayMethod"]');
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
            if(isNull)
                $repayMethod.val('');
        };

        if (_termTypeVal == 'DAYS') {
            selectRepayethod('INTEREST_MONTHS/EQUAL_INSTALLMENT');
        } else if (_termTypeVal == 'MOTHS') {
            selectRepayethod('INTEREST_DAYS');
        } else if(_termTypeVal =='SEASONS'){
            selectRepayethod('INTEREST_MONTHS/INTEREST_DAYS/EQUAL_INSTALLMENT/BULLET_REPAYMENT');
        } else {
            selectRepayethod('INTEREST_MONTHS/EQUAL_INSTALLMENT');
        }
    },


    /**
     * param{string} id form表单ID
     *
    * */
    calculate:function(id,name){
        var _termTypeVal = $('#'+id+' select[name="termType"]').find('option:selected').val();
        var $calculate = $('#'+id+' select[name="'+name+'"]');
        if(_termTypeVal == 'DAYS' || _termTypeVal =='FIXED_DATE'){
            $calculate.attr('disabled',false);
        }else{
            $calculate.val('');
            $calculate.attr('disabled',true);
        }
    },


    /**
     * 收息时间
     *
    * */

    repayDate:function(id,name){
        var _methodVal = $('#'+id+' select[name="repayMethod"]').find('option:selected').val();
        var repaydate = $('#'+id+' select[name="'+name+'"]');

        if(_methodVal == "INTEREST_MONTHS") {
            repaydate.removeClass('disabled')
        }else if(_methodVal == 'INTEREST_DAYS'){
            repaydate.val("REPAY_PRE").addClass('disabled');
        } else {
            repaydate.val("REPAY_SUF").addClass('disabled');
        }
    }

};







