/**
 * Created by yangzb01 on 2017-11-29.
 */

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
    loanPushOrderId:'',
    taskId:utils.getUrlParam('taskId'),
    orderId:''
};

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
            $('#loanTmpInfo').find('[name='+i+']').val(_data.value).attr('readonly',true);
        }else{
            for(K in _data.value){
                $('#loanTmpInfo').find('[name='+i+']').each(function(n,ele){
                    var ele = $(ele);
                    ele.attr('disabled',true);
                    if(ele.attr('data-key') == K){
                        ele.prop('checked',true)
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

var init = {
    /**
     * 获取票据列表 & 填充
     * */
    getBillList:function(billList){
        for(i in billList){
            var _data = billList[i];
            $('#billList').append('<option value='+_data.id+'>'+_data.billNo+'')
        }

        $('#billList').dropdown({
            allowAdditions: true,
        });

        var values = [];
        for(i in billList){
            var _data = billList[i];
            if(_data.pushOrderId){
                values.push(_data.id)
            }
        }

        $('#billList').dropdown('set selected',values)
        $('#billList').attr('disabled',false);
        $('#billRelation').removeClass('disabled ks-hidden');
    },

    clickAgree:function(obj){
        var obj = obj ? '#'+obj : '';
        $(''+obj+' input[name="approvalCode"]').click(function(){
            var val =  $('input[name="approvalCode"]:checked').val();
            if(val == 'AGREE'){
                $(''+obj+' textarea[name="content"]').val("同意");
            }else{
                $(''+obj+' textarea[name="content"]').val("");
            }
        });
    },

    submitFlow:function(){
        $("#approvalForm").form({
            fields: {
                content: {
                    identifier: 'content',
                    rules: [
                        {
                            type:'maxLength[500]',
                            prompt:'审批意见不超过500个字符'
                        }
                    ]
                },
                approvalCode: {
                    identifier: 'approvalCode',
                    rules: [
                        {
                            type: 'checked',
                            prompt: '请选择一个状态'
                        }
                    ]
                }
            },
            onSuccess: function (e, fidlds) {
                e.preventDefault();
            }
        }).api({
            action: "order node approval",
            method: 'POST',
            serializeForm: true,
            beforeSend: function (settings) {
                settings.data["loanPushOrderId"] =  DATA.loanPushOrderId;
                settings.data["orderId"] =  DATA.orderId;
                settings.data["taskId"] =  DATA.taskId;
                settings.data["loanPushId"] =  DATA.loanPushId;
                return settings;
            },
            onSuccess: function (response) {
                $.uiAlert({
                    type: "success",
                    textHead:  '成功',
                    text: '成功',
                    time: 1,
                    onClosed: function () {
                        window.location.href = '/loan_push_order/approval_list'
                    }
                });
            },
            onFailure: function (response, element) {
                $.uiAlert({
                    type: "danger",
                    textHead:  '失败',
                    text: response.msg,
                    time: 3,
                });
            }
        });
    },

    submitFlowApprove:function(obj){
        var  type = obj.find('input[name="approvalCode"]:checked').val();
        if ("BACKBEGIN" === type) {
            $("input[name='needRepeatFlowRadio'][value='true']").prop("checked", "checked");
            $('#needRepeatFlowModal')
                .modal({
                    closable  : true,
                    onDeny    : function(){ return true;},
                    onApprove : function() {
                        var radioValue = $("input[name='needRepeatFlowRadio']:checked").val();
                        obj.children("input[name='needRepeatFlow']").val(radioValue);
                        obj.submit();
                    }
                }).modal('show');
        }else {
            obj.submit();
        }
    },

    getDetail:function () {
        $(document).api({
            on:'now',
            method:'post',
            action:'order approval detail',
            data:{
                taskId:utils.getUrlParam('taskId'),
                orderId:utils.getUrlParam('orderId')
            },
            onSuccess:function(data){
                DATA.itemType = data.order.itemType;
                DATA.loanPushId = data.order.pushId;
                DATA.loanPushOrderId = data.order.id;
                DATA.orderId = data.approvalInfo.orderId;

                data.resultEnums = function(){
                    var r=[];
                    for(var x in enums.approval_code_desc){
                        r.push({
                            value:x,
                            name:enums.approval_code_desc[x]
                        })
                    }
                    return r;
                };
                var $basicTmpInfo = utils.render('#basicTmpInfo',data);
                $('#info').html($basicTmpInfo);

                //初始化产品模板
                var $tmpInfo = utils.render('#'+order_type[DATA.itemType]+'','');
                $('#loanTmpInfo').html($tmpInfo);

                var list = JSON.parse(data.order.content);
                setProjectInfo(list);//填充产品模板信息

                if(data.billLoanPushList) init.getBillList(data.billLoanPushList);

                $('select[name="termType"]').val(data.order.termType);
                changeTermType();
                init.clickAgree('approvalForm')
                $('input[name="term"]').val(data.order.term);
                $('select[name="repayMethod"]').val(data.order.repayMethod);

                init.submitFlow();
            },

        })
    },
};

$(document).on('click','#businessFormBtn',function(){
    init.submitFlowApprove($('#approvalForm'));
});

init.getDetail();

