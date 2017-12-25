
var loanPushId = utils.getUrlParam('loanPushId');


$(document).api({
    on:'now',
    method:'post',
    action:'get loan order list',
    data:{
        'loanPushId':loanPushId
    },
    onSuccess:function(data){
        data.isPushed = function(){
            return  this.base.status == 'PUSHED'?true:false;
        };

        data.f_pushTarget = function(){
            return this.pushTarget == 'KAISAFAX'? '佳兆业金服':'--';
        };
        data.f_status = function(){
            return enums.psuh_order_status[this.status]
        };
        data.f_amount = function(){
            return accounting.formatMoney(this.amount,'',2,',','.');
        };
        data.f_pushDateTime = function(){
            return this.pushDateTime?moment(this.pushDateTime).format('YYYY-MM-DD'):'--';
        };
        data.f_platformLoanTime = function(){
            return this.platformLoanTime?moment(this.platformLoanTime).format('YYYY-MM-DD'):'--';
        };
        data.f_platformLoanNextDueDate = function(){
            return this.platformLoanNextDueDate?moment(this.platformLoanNextDueDate).format('YYYY-MM-DD'):'--';
        };
        data.f_platformLoanDueDate = function(){
            return this.platformLoanDueDate?moment(this.platformLoanDueDate).format('YYYY-MM-DD'):'--';
        };
        data.f_platformLoanClearedDate = function(){
            return this.platformLoanClearedDate?moment(this.platformLoanClearedDate).format('YYYY-MM-DD'):'--';
        };

        data.isLoan = function(){
            return this.status == ('LOANED'||'OVERDUE'||'CLEARED') ? true:false;
        };
        data.isEdit = function(){
            return this.status == 'EDIT' ? true:false;
        };

        var $basicTmpInfo = utils.render('#basicTmpInfo',data);
        $('#info').html($basicTmpInfo);
    },
    onFailure: function(response) {
        $.uiAlert({type: "danger", textHead: '获取数据失败', text: response.msg, time: 3});
    }
});

function pushed(){
    $(document).api({
        on:'now',
        method:'post',
        action:'order get status',
        data:{
            pushId:loanPushId
        },
        onSuccess:function(data){
            console.log(data);
            var completeText = data.isComplete == true? '应该推单完成':'应该还未推单完成，你是否确定推单完成。'
            $.uiDialog('根据计算，业务单号'+data.loanCode+''+completeText,{
                onApprove:function(){

                    $(document).api({
                        on:'now',
                        method:'post',
                        action:'order complete push',
                        data:{
                            pushId:loanPushId
                        },
                        onSuccess:function(data){
                            $.uiAlert({type: "success", textHead: '成功', text:'成功', time: 2,
                                onClosed:function(){
                                    window.location.href ='/loan_push/index';
                                }
                            });
                        },
                        onFailure: function(data) {
                            $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 3});
                        }
                    })
                }
            })
        },
        onFailure: function(data) {
            $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 3});
        }
    })
}