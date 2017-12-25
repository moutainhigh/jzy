/**
 * Created by yangzb01 on 2017-11-08.
 */

var loanId = utils.getUrlParam('loanId');
var id = utils.getUrlParam('id');
var feeList = ['prepaymentFee','prepaymentFeeRate','overdueFee','manageFee','guaranteeFee','serviceFee','interest'];
var DATA = {
    ids:[],
    isView:false
};

window.onload= function(){
    var view = utils.getUrlParam('view');
    if(view){
        DATA.isView = true;
        $('#businessSearchDiv').addClass('disabled')
        $('#viewRemoveBtn').remove();
    }
};

/**
 * 自定义一个数组del操作，删除指定元素（前到后），
 * */
Array.prototype.del = function(val){
    for(var i=0; i<this.length;i++){
        if(val == this[i]){
            this.splice(i,1);
            break;
        }
    }
};

$("#businessSearch").search({
    apiSettings: {
        method: "post",
        url: '/product/get_user' + '?keyWord={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (data) {
        $("#businessSearch input[name='businessId']").val(data.id);
    }
});

$("input[name='businessName']").on('input propertychange',function(){
    $("input[name='businessId']").val('')
});

var init = {

    //还款计划
    getRepayPlan:function(){
        $(document).api({
            on:'now',
            method:'post',
            action:'get cost loan repay',
            data:{
                loanId:loanId
            },
            onSuccess:function(data){
                data.repayList = data;
                data.f_dueDate = function(){
                    return moment(this.dueDate).format('YYYY-MM-DD')
                };
                data.f_amount = function(){
                    return accounting.formatMoney(this.amount,'',2,',','.')
                };
                data.f_interest = function(){
                    return accounting.formatMoney(this.interest,'',2,',','.')
                };
                data.f_feeAmount = function(){
                    return accounting.formatMoney(this.feeAmount,'',2,',','.')
                };
                data.f_totalAmount = function(){
                    return accounting.formatMoney(this.totalAmount,'',2,',','.')
                };
                data.f_repayAmount = function(){
                    return accounting.formatMoney(this.repayAmount,'',2,',','.')
                };
                data.f_repayInterest = function(){
                    return accounting.formatMoney(this.repayInterest,'',2,',','.')
                };
                data.f_repayFeeAmount = function(){
                    return accounting.formatMoney(this.repayFeeAmount,'',2,',','.')
                };
                data.f_repayTotalAmount = function(){
                    return accounting.formatMoney(this.repayTotalAmount,'',2,',','.')
                };
                data.f_status = function(){
                    return enums.Loan_repay_status[this.status]
                };
                data.view = DATA.isView;
                data.statusFlag = function(){
                    var $status = this.status;
                    if($status == 'OVERDUE' || $status == 'LOANED'){
                        return true;
                    }else{
                        return false
                    }
                };
                var $loanRepay = utils.render('#loanRepayTmp',data);
                $('#repayTable').html($loanRepay)
            },
        })
    },


    //减免费用
    subFee:function(repayId,period){
        $(document).api({
            on:'now',
            method:'post',
            action:'cost loan repay fee',
            data:{
                repayId:repayId,
            },
            onSuccess:function(data){
                var list = [];
                for(i in data){
                    if(data[i] != 0){
                        list.push({
                            type:i,
                            cnType:enums.feeList[i],
                            amountStr:accounting.formatMoney(data[i],'',2,',','.'),
                            amount:data[i]
                        })
                    }
                }
                if(list.length > 0 ){
                    var flag = true;
                    for(i in DATA.ids){
                        if(repayId == DATA.ids[i]){
                            $.uiAlert({type: "danger", textHead: '重复操作', text:'请勿重复减免', time: 3});
                            return flag = false;
                        }
                    }
                    if(flag){
                        DATA.ids.push(repayId);
                    }

                    data.list = list;
                    data.period = period;
                    data.repayId = repayId;
                    var $costInfoTmp = utils.render('#costInfoTmp',data);
                    $('#costInfoBox').append($costInfoTmp);
                }else{
                    $.uiAlert({type: "danger", textHead: '添加减免失败', text:'该期无费用可减免', time: 3});
                }

            },
            onFailure: function (data) {
                $.uiAlert({type: "danger", textHead: '获取减免费用信息失败', text: data.msg, time: 3});
            }
        })
    },

    //移除操作
    removeCost:function(id,$this){
        $($this).parents('.js_demoInfo').remove();
        DATA.ids.del(id);
    },


    //还款记录
    getRepayRecord:function(repayId){
        $(document).api({
            on:'now',
            method:'post',
            action:'get loan record',
            data:{
                repayId:repayId,
                loanId:loanId
            },
            onSuccess:function(data){
                data.f_repayDate=function(){
                    return moment(this.repayDate).format("YYYY-MM-DD");
                };
                data.f_repayAmount=function(){
                    return accounting.toFixed(this.repayAmount,2);
                };
                data.f_repayInterest=function(){
                    return accounting.toFixed(this.repayInterest,2);
                };
                data.f_repayTotalAmount=function(){
                    return accounting.toFixed(this.repayTotalAmount,2);
                };
                data.f_feeType=function(){
                    return enums.feeType[this.feeType];
                };
                $("#repayViewTableExtension").html(utils.render("#repayViewTableExtension-template-forQuery",data));
            }
        });

        $("#gatherRecordViewModal2").modal({
            observeChanges: true,
            blurring: true
        }).modal('show')
    },

    /**
     * 校验
     * */
    validate:function(){
        var flag = true;
        if($('.js_demoInfo').length == 0){
            $.uiAlert({type: "danger", textHead: '请添加至少一笔费用减免', text:'', time: 3});
            flag = false;
            return false
        }
        if($('input[name="businessId"]').val() == ''){
            $.uiAlert({type: "danger", textHead: '请选择已有业务员', text:'', time: 3});
            flag = false;
            return false
        }
        $('.js_demoInfo').each(function(i,ele){
            var reason = $(ele).find('textarea[name="exemptionReason"]').val();
            var reg = /^\d+(\.\d{1,2})?$/;

            $(ele).find('input[name="amount"]').each(function(n,ths){
                var val = $(ths).val();
                var maxVal = $(ths).attr('data-max');
                if(val == ''){
                    $.uiAlert({type: "danger", textHead: '请输入减免金额', text:'', time: 3});
                    flag = false;
                    return false
                }

                if(!reg.test(val)){
                    $.uiAlert({type: "danger", textHead: '减免金额格式不对', text:'', time: 3});
                    flag = false;
                    return false
                }

                if( parseFloat(val) > parseFloat(maxVal)){
                    $.uiAlert({type: "danger", textHead: '减免金额小于剩余应还金额', text:'', time: 3});
                    flag = false;
                    return false;
                }
            });

            if(reason =='' || reason.length > 500){
                $.uiAlert({type: "danger", textHead: '减免原因不能为空，且减免原因小于500个字符', text:'', time: 3});
                flag = false;
                return false;
            }
        });
        return flag;
    },

    /**
     * 保存减免费用
     * */
    save:function(callBack){
        if(this.validate()){
            var costExemptionItemList = [];
            $('.js_demoInfo').each(function (i,ele) {
                costExemptionItemList.push({
                    'id':$(ele).find('input[name="id"]').val(),
                    'repayId':$(ele).find('input[name="repayId"]').val(),
                    'period':$(ele).find('input[name="period"]').val(),
                    'exemptionReason':$(ele).find('textarea[name="exemptionReason"]').val()
                });
                $(ele).find('.item').each(function(n,ths){
                    var type = $(ths).attr('data-type');
                    var preType = $(ths).attr('data-pretype');
                    costExemptionItemList[i][type] = $(ths).val();
                    costExemptionItemList[i][preType] = $(ths).attr('data-max');
                });
            });

            $(document).api({
                on:'now',
                action:'submit cost apply',
                method:'post',
                data:JSON.stringify({
                    'costExemptionItemList':costExemptionItemList,
                    'id':id,
                    'loanId':loanId,
                    'businessName':$('input[name="businessName"]').val(),
                    'businessId':$('input[name="businessId"]').val(),
                }),
                onSuccess:function(data){

                    if(callBack && typeof callBack =='function'){
                        callBack()
                    }else{
                        $.uiAlert({type: "success", textHead: '保存成功', text: data.msg, time: 3});
                    }
                },
                onFailure: function (data) {
                    $.uiAlert({type: "danger", textHead: '保存失败', text: data.msg, time: 3});
                }
            });
        }
    },

    /**
     * 提交审批
     * */
    submitApproval:function(){
        var $this= this;
        $.uiDialog('你申请了一笔费用免除，你确定要提交',{
            onApprove:function(){
                $this.save(function(){
                    $(document).api({
                        on:'now',
                        action:'submit cost approval',
                        method:'post',
                        data:{
                            'id':id,
                        },
                        onSuccess:function(data){
                            $.uiAlert({type: "success", textHead: '提交成功', text: '申请减免提交成功', time: 1,
                                onClosed:function(){
                                    window.location.href = '/cost_exemption/index';
                                }
                            });
                        },
                        onFailure: function (data) {
                            $.uiAlert({type: "danger", textHead: '保存失败', text: data.msg, time: 3});
                        }
                    });
                });
            }
        });
    },

    /**
     * 获取详情
     * */
    getDetail:function() {
        $(document).api({
            on: 'now',
            action: 'get cost detail',
            method: 'post',
            data: {
                id: id
            },
            onSuccess: function (data) {

                $('input[name="businessName"]').val(data.businessName);
                $('input[name="businessId"]').val(data.businessId);
                if (data.costExemptionItemList.length > 0) {
                    var costInfo = data.costExemptionItemList;
                    for (var i=0;i<costInfo.length;i++){
                        var lists = [];
                        var data = costInfo[i];
                        for(n in data){
                            if(feeList.indexOf(n) > -1){
                                lists.push({
                                    'f_amount':data[n],
                                    'type':n,
                                    'cnType':enums.feeList[n],
                                    'amount':data[n+'Pre'],
                                    'amountStr':accounting.formatMoney(data[n+'Pre'],'',2,',','.'),
                                })
                            }
                        }
                        costInfo[i].list = lists;
                        DATA.ids.push(costInfo[i].repayId);
                        costInfo[i].view = DATA.isView;
                        var $costInfoTmp = utils.render('#costInfoTmp', costInfo[i]);
                        $('#costInfoBox').append($costInfoTmp);
                    }
                }
            }
        })

    }
};

init.getRepayPlan();
init.getDetail();


