/**
 * Created by yangzb01 on 2017-11-24.
 */
var id = utils.getUrlParam('id');
var order_type= {
    CAR_LOAN:'CHEDAI_template',
    HOUSE_MORTGAGE_LOAN:'HONGBEN_template',
    BANK_HOUSE_LOAN:'SHULOU_template',
    PERSONAL_LOAN:'GERENDAI_template',
    FACTORING:'BAOLI_template',
    BILL:'PIAOJU_template',
    BANK_BILL:'YINPIAO_template'
};
var DATA ={
    itemType:'',
    loanPushId:'',
    billInfo:[],
    code:'',
    basicInfoAmount:''
};

$.fn.form.settings.rules.num = function(value){
    var reg = /^[1-9]([0-9]+)?$/;
    return reg.test(value) || value =='';
};

$.fn.form.settings.rules.compareBill = function(value){
    if(DATA.billInfo.length > 0 && (DATA.itemType == 'BANK_BILL' || DATA.itemType == 'BILL')){
        var maxAmount = 0;
        for(i in DATA.billInfo){
            var _data = DATA.billInfo[i];
            maxAmount  = add(_data.billAmount, maxAmount);
        }
        return value <= maxAmount
    }else{
        return true;
    }
};

$.fn.form.settings.rules.compareAmount = function(value){
    if(DATA.itemType != 'BANK_BILL' && DATA.itemType != 'BILL'){
        return value <= DATA.basicInfoAmount
    }else{
        return true;
    }
};

$.fn.form.settings.rules.termRange = function(value){
    var type = $('select[name="termType"]').val();
    var reg = /^[0-9]+$/;
    if(type != 'FIXED_DATE'){
        return reg.test(value) && value>0;
    }else{
        return true;
    }
};


//匹配平台借款人
function platBorrower(){
    $(document).api({
        on:'now',
        action:'get platform borrower info',
        method:'post',
        data:{
            mobile:$('#mobile').val()
        },
        onSuccess:function(data){
            if(data.head.errorCode == 0){
                $.uiAlert({
                    type: "success",
                    textHead: '匹配成功',
                    text: '',
                    time: 2
                });
                $('#platformBorrower').val(data.body.name);
                $('input[name="platformBorrowerId"]').val(data.body.userId)

            }else{
                $.uiAlert({
                    type: "danger",
                    textHead: '关联失败',
                    text: data.head.errorMsg,
                    time: 2
                });
            }
        },
        onFailure:function(data){
            $.uiAlert({
                type: "danger",
                textHead: '关联失败',
                text: data.msg,
                time: 2
            });
        }
    })
}

function resetBorrower(){
    $('#mobile,#platformBorrower,input[name="platformBorrowerId"]').val('');
}

//期限渲染
function changeTermType(){
    var termType = $('select[name="termType"]');
    termType.val() =='FIXED_DATE'?$('input[name="term"]').attr('type','date'):$('input[name="term"]').attr('type','text')
    termType.on('change',function(){
        $(this).val() == 'FIXED_DATE'? $('input[name="term"]').attr('type','date'):$('input[name="term"]').attr('type','text')
    });
}

// list为content json数据 填充模板数据
function setProjectInfo(list){
    for(i in list){
        var _data = list[i];
        if(_data.type != 'list'){
            $('#loanTmpInfo').find('[name='+i+']').val(_data.value);
        }else{
            for(K in _data.value){
                $('#loanTmpInfo').find('[name='+i+']').each(function(n,ele){
                    var ele = $(ele);
                    if(ele.attr('data-key') == K){
                        ele.prop('checked',true);
                    }
                })
            }
        }
    }
}

//组装项目信息
function jsonContentData(){
    var data={
        audittype:{
            title:'',
            type:'list',
            value:{}
        }
    };
    $('#loanTmpInfo').find('.item').each(function(n,ele){
        var key = $(this).attr('name'),
            title = $(this).prev('label').html(),
            val = $(this).val();
        data[key] = {
            title:title,
            type:'text',
            value:val
        }
    });
    $('#loanTmpInfo').find("input[name='audittype']").each(function(n,ele){
        if($(this).prop('checked')){
            var key = $(this).attr('data-key');
            var val = $(this).next('label').html();
            data.audittype.value[key] = val
        }
    });
    return JSON.stringify(data);
}

var init= {
    validate:{
        inline: true,
        on: 'blur',
        fields:{
            channelRate: {
                identifier: 'channelRate',
                rules: [{
                    type:'empty',
                    prompt:'{name}不为空'
                },{
                    type:'validateNumFloat[0-999999999.99]',
                    prompt:'请输入利率范围在0-999999999.99'
                }]
            },
            amount:{
                identifier:'amount',
                rules: [{
                    type:'empty',
                    prompt:'{name}不为空'
                },{
                    type:'validateNumFloat[0-99999999.99]',
                    prompt:'请输入金额范围在0-99999999.99'
                },{
                    type:'compareAmount',
                    prompt:'{name}'
                },{
                    type:'compareBill',
                    prompt:'{name}'
                }]
            },
            platformBorrowerId:{
                identifier:'platformBorrowerId',
                rules: [{
                    type:'empty',
                    prompt:'请匹配正确的平台借款用户'
                }]
            },
            term:{
                identifier:'term',
                rules: [{
                    type:'empty',
                    prompt:'推单期限不为空'
                },{
                    type:'termRange',
                    prompt:'期限为正整数且大于0'
                }]
            },
            maxInvestor:{
                identifier:'maxInvestor',
                rules: [{
                    type:'empty',
                    prompt:'{name}不为空'
                },{
                    type:'num',
                    prompt:'请输入正整数'
                },{
                    type:'maxLength[13]',
                    prompt:'字符长度超过13位'
                }]
            },
            billCode:{
                identifier:'billCode',
                rules: [{
                    type:'empty',
                    prompt:'请选择票号'
                }]
            }
        }
    },
    /**
     * 获取票据列表 & 填充
     * */
    getBillList:function(billList){
        for(i in billList){
            var _data = billList[i];
            $('#billList').append('<option value='+_data.id+'>'+_data.billNo+'')
        }
        $('#billList').attr('disabled',false);
        $('#billRelation').removeClass('disabled ks-hidden');
        var maxAmount = 0;
        $('#billList').dropdown({
            allowAdditions: true,
            onAdd: function(value, text, $selectedItem) {
                for(i in billList){

                    var _data = billList[i];
                    if(value == _data.id){
                        maxAmount = add(billList[i].billAmount,maxAmount);
                        DATA.billInfo.push({
                            id:_data.id,
                            pushId:_data.pushId,
                            billNo:_data.billNo,
                            billAmount:_data.billAmount,
                            version:_data.version,
                            pushOrderId:_data.pushOrderId?_data.pushOrderId:''
                        })
                    }
                }
                $('input[name="amount"]').attr('placeholder','推单金额小于'+maxAmount+'(所选票号金额总和)')
            },
            onRemove:function(value,removedText,$selectedItem){
                for(i in DATA.billInfo){
                    var  d_info = DATA.billInfo[i];
                    if(d_info.id == value){
                        maxAmount = sub(maxAmount,billList[i].billAmount);
                        DATA.billInfo.splice(i,1);
                    }
                }
                $('input[name="amount"]').attr('placeholder','推单金额小于'+maxAmount+'(所选票号金额总和)')
            }
        });

        var values = [];
        for(i in billList){
            var _data = billList[i];
            if(_data.pushOrderId){
                values.push(_data.id)
            }
        }

        $('#billList').dropdown('set selected',values)
    },

    getDetail:function () {
        $(document).api({
            on:'now',
            method:'post',
            action:'get order detail',
            data:{
                'id':id
            },
            onSuccess:function(data){
                var repayMethod = [];
                DATA.code = data.order.code;
                for(i in data.pushRepayMethods){
                    var flag = false;
                    if(data.order.repayMethod){
                        flag = i== data.order.repayMethod ? true:false
                    };
                    repayMethod.push({
                        key:i,
                        name:data.pushRepayMethods[i],
                        selected:flag
                    })
                }
                data.repayMethod = repayMethod;

                DATA.itemType = data.order.itemType;
                DATA.loanPushId = data.order.pushId;
                DATA.basicInfoAmount = data.loanAmount;
                var $basicTmpInfo = utils.render('#basicTmpInfo',data);
                $('#info').html($basicTmpInfo);

                //初始化产品模板
                var $tmpInfo = utils.render('#'+order_type[DATA.itemType]+'','');
                $('#loanTmpInfo').html($tmpInfo);

                var list = JSON.parse(data.order.content);
                setProjectInfo(list);//填充产品模板信息

                if(data.billLoanPushList) init.getBillList(data.billLoanPushList);
                if(data.order.itemType!='BILL' && data.order.itemType !='BANK_BILL')
                    $('input[name="amount"]').attr('placeholder','推单金额小于'+data.loanAmount);

                $('select[name="termType"]').val(data.order.termType);
                changeTermType();
                $('input[name="term"]').val(data.order.term);
            },

        })
    },

    orderSave:function(flag,$this) {
        if(flag == 'false'){
            submitForm(flag);
        }else{
            var time = getTime();
            var text = '该资产单推送到'+$('select[name="pushTarget"] option:checked').text()+'。预计资产到期日为'+time;
            $.uiDialog(''+text+'',{
                onApprove:function(){
                    submitForm(flag)
                }
            });
        }

        function submitForm(flag){
            $('.ui.form').form(init.validate).api({
                action: 'order save',
                method: 'POST',
                beforeSend:function(settings){
                    var _data = {
                        "order" : {
                            "id":$('[name="id"]').val(),
                            "pushId":$('[name="pushId"]').val(),
                            "loanId":$('[name="loanId"]').val(),
                            "code":$('[name="code"]').val(),
                            "pushTarget":$('[name="pushTarget"]').val(),
                            "productTypeName": $('[name="productTypeName"]').val(),
                            "loanSubjectName":$('[name="loanSubjectName"]').val(),
                            "platformBorrower":$('[name="platformBorrower"]').val(),
                            "platformBorrowerName":$('[name="platformBorrowerName"]').val(),
                            "platformBorrowerId":$('[name="platformBorrowerId"]').val(),
                            "amount":$('[name="amount"]').val(),
                            "repayMethod":$('[name="repayMethod"]').val(),
                            "term": $('[name="term"]').val(),
                            "termType":$('[name="termType"]').val(),
                            "channelRate":$('[name="channelRate"]').val(),
                            "maxInvestor":$('[name="maxInvestor"]').val(),
                            'itemType':DATA.itemType,
                            "content": jsonContentData()
                        },
                        "billLoanPushList":DATA.billInfo,
                        "submit":flag
                    };
                    settings.data = JSON.stringify(_data);
                    return settings
                },
                onSuccess:function(data){
                    var text = flag =='true'? '推单成功':'保存成功';
                    $.uiAlert({
                        type: "success",
                        textHead: text,
                        text:'',
                        time: 1,
                        onClosed: function () {
                            window.location.href = '/loan_push_order/index?loanPushId='+DATA.loanPushId;
                        }
                    })
                },
                onFailure:function(data){
                    $.uiAlert({
                        type: "danger",
                        textHead: '操作失败',
                        text: data.msg,
                        time: 2
                    });
                }

            });
            $('.ui.form').submit();
        }
    },
};


function getTime(){
    var termType = $('select[name="termType"]').val();
    var val = $('input[name="term"]').val();
    type = {
        FIXED_DATE:'',
        YEAS:'y',
        MOTHS:'M',
        DAYS:'d'
    };
    var _val = val == ''? '':val;
    if(val != ''){
        if(type[termType] != ''){
            var time = moment().add(_val,type[termType]).format('YYYYMMDD')
        }else{
            var time = moment(_val).format('YYYYMMDD');
        }
    } else{
        var time = '[推单期限为空]';
    }
    return time;
}
init.getDetail()