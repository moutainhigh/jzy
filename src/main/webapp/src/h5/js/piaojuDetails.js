/**
 * Created by pengs on 2016/12/26.
 */
/**
 * 渲染详情全部信息
 */
var renderDetails = function (id) {

    var $details = $(utils.render("#detailsTemplate", {}))
    $details.on('animationend webkitAnimationEnd', function () {
        $details.removeClass('slideIn').addClass('js_show');
    });
    $("#container").append($details);

    renderApproveCode(id);
    renderBasic(id, function () {
        renderBusinessApply(id);
    });
}


/**
 * 渲染基本信息
 */
var renderBasic = function (id, callback) {
    $.ajax({
        type: "get",
        url: "/m/business_apply/fetch_base",
        data: {
            id: id
        },
        async: true,
        success: function (response) {
            var _data = response;
            _data.businessLineCN = function () {
                return enums.business_line[this.businessLine];
            }
            var $detailsBasicTemplate = utils.render("#detailsBasicTemplate", _data);
            $("#basicContainer").html($detailsBasicTemplate);
            callback();
        }
    });
}

/**
 * 渲染审批结果radio
 */
var renderApproveCode = function (id) {
    $.ajax({
        type: "get",
        url: "/m/flow/query_user_approval",
        data: {
            loanId: id,
            flowConfigureType:'BORROW_APPLY'
        },
        async: true,
        success: function (response) {
            var _data = response.data;
            if (_data) {
                DATA.id = _data.workItem.id;
                DATA.taskId = _data.workItem.taskId;
                DATA.orderId = _data.workItem.orderId;
                DATA.type = _data.type;

            } else {
                _data = {}
            }

            initForm(DATA.type);

            _data.resultEnums = function () {
                var r = [];

                for (var x in enums.approval_code_desc) {
                    r.push({
                        value: x,
                        name: enums.approval_code_desc[x]
                    })
                }
                return r;
            }
            var $detailsApproveCodeTemplate = utils.render("#detailsApproveCodeTemplate", _data);
            $("#approveCodeContainer").html($detailsApproveCodeTemplate);
            if(_data.enterprise == true){
                var $detailApprovalSealTemplate = utils.render("#detailsApproveSealTemplate", _data);
                $('#approveSeal').html($detailApprovalSealTemplate);
                $('#approveSeal').addClass('active')
            }

            if($('#productInfoTmpl').val().indexOf('piaojuView') >-1|| $('#productInfoTmpl').val().indexOf('yinpiaoView') >-1){
                var $detailsApproveIntermediaryTemplate = utils.render("#detailsApproveIntermediaryTemplate",'');
                $('#approveIntermediary').html($detailsApproveIntermediaryTemplate)
            }

            clickAgree();
            $("textarea[name='content']").on("propertychange input", function () {
                var textLength = $(this).val().length;
                $(".js-textareaNum").html(textLength);
            })
            //增加验证
            $("input[name='approvalCode']").first().attr({
                "required": "",
                "tips": "请选择一个审批结果"
            })
        }
    });
}


/**
 * 渲染申请信息
 */
var renderBusinessApply = function (id) {
    /*
    $.ajax({
        type:"post",
        url: "/m/product/view_loan_profit",
        data:{
            loanId:id
        },
        async: true,
        success:function(data){
            if(data.loanProfit){
                var _data = data.loanProfit;
                _data.isBill = function(){
                    var value = $('#productInfoTmpl').val();
                    if(value.indexOf('piaojuView') > -1 || value.indexOf('yinpiaoView') > -1){
                        return true;
                    }else{
                        return false
                    }
                };
                _data.is_loanProfit = function(){
                    if(data.code =='001' || data.loanProfit){
                        return true;
                    }else{
                        return false;
                    }
                };
                _data.loanProfit = data.loanProfit
                _data.p_interestRevenue = function(){
                    return accounting.formatNumber(this.interestRevenue, 2,",")
                };
                _data.p_capitalCost = function(){
                    return accounting.formatNumber(this.capitalCost, 2,",")
                };
                _data.p_valueAddedTax = function(){
                    return accounting.formatNumber(this.valueAddedTax, 2,",")
                };
                _data.p_surtax = function(){
                    return accounting.formatNumber(this.surtax, 2,",")
                };
                _data.p_laborCost = function(){
                    return accounting.formatNumber(this.laborCost, 2,",")
                };
                _data.p_administrativeExpenses = function(){
                    return accounting.formatNumber(this.administrativeExpenses, 2,",")
                };
                _data.p_badAssetsReserve = function(){
                    return accounting.formatNumber(this.badAssetsReserve, 2,",")
                };
                _data.p_operatingCost = function(){
                    return accounting.formatNumber(this.operatingCost, 2,",")
                };
                _data.p_brokerageFee = function(){
                    return accounting.formatNumber(this.brokerageFee, 2,",")
                };
                _data.p_profit = function(){
                    return accounting.formatNumber(this.profit,2,",")
                };
            }else{
                var _data = data
                _data.loanProfit = false;
            }
            var $profitTemplate = utils.render("#profitTemplate", _data);
            $("#profit").append($profitTemplate);
        }
    });*/

    $.ajax({
        type: "get",
        url: "/m/business_apply/query_bill",
        data: {
            loanId: id
        },
        async: true,
        success: function (response) {
            response.draw_Time = function () {
                if(this.drawTime)
                    return moment(this.drawTime).format('YYYY-MM-DD');
            }
            response.loanRepay_dueDate = function () {
                if(this.loanRepay.dueDate)
                    return moment(this.loanRepay.dueDate).format('YYYY-MM-DD');
            }
            response.actual_DueDate = function(){
                if(this.actualDueDate)
                    return moment(this.actualDueDate).format('YYYY-MM-DD');
            }
            response.cost_Rate = function(){
                if(this.costRate){
                    return this.costRate;
                }else{
                    return '0';
                }
            }
            response.intermediary_Fee = function () {
                if(this.intermediaryFee){
                    return accounting.formatNumber(this.intermediaryFee,6,",");
                }else{
                    return '0.00';
                }
            }
            response.dis_Date = function () {
                if(this.disDate)
                    return moment(this.disDate).format('YYYY-MM-DD');
            }
            response.discount_Time = function () {
                if(this.discountTime)
                    return moment(this.discountTime).format('YYYY-MM-DD');
            }

            response.loanRepay_amount = function (){
                if(this.loanRepay.amount)
                    return accounting.formatNumber(this.loanRepay.amount,2,",");
            };
            response.total_Amount = function (){
                if(this.totalAmount)
                    return accounting.formatNumber(this.totalAmount,2,",");
            };
            response.loan_amount = function(){
                if(this.loan.amount)
                    return accounting.formatNumber(this.loan.amount,2,",")
            };
            response.loanRepay_interest =function(){
                if(this.loanRepay.interest){
                    return accounting.formatNumber(this.loanRepay.interest,2,",") ;
                }else{
                    return '0.00';
                }
            };
            response.inTerest =function(){
                if(this.interest){
                    return accounting.formatNumber(this.interest,2,",") ;
                }else{
                    return '0.00';
                }
            };
            response.billLoan_intermediaryName = function () {
                return this.intermediaryName;
            }
            response.billLoan_intermediaryTotalFee = function () {
                if(this.intermediaryTotalFee){
                    return accounting.formatNumber(this.intermediaryTotalFee,6,",") ;
                }else{
                    return '0.00';
                }
            }

            var $detailsDetailTemplate = utils.render("#detailsDetailTemplate", response);
            $("#detailsdetailContainer").append($detailsDetailTemplate);
            var $applyTemplate = utils.render("#applyTemplate", response);
            $("#detailsdetailContainer").append($applyTemplate);
        }
    });
}


function submitApproveForm(loading,data,returnUrl) {
    var $ajax = function(loading,data,returnUrl){
        $.ajax({
            type: "POST",
            url: "/m/flow/node_approval",
            data: data,
            async: true,
            success: function (response) {
                loading.hide();
                if (response.ok == true) {
                    weui.alert('',function(){
                        window.location.href = returnUrl
                    },{title: '操作成功'});
                } else {
                    if (response.msg) {
                        if($("input[name='approvalCode']:checked").val()!=="AGREE"){
                            weui.alert('',function(){
                                window.location.href = returnUrl
                            },{title: '操作成功'});
                        }else {
                            weui.alert(response.msg, {title: '操作失败'});
                        }
                    } else {
                        weui.alert("不明原因", {title: '操作失败'});
                    }
                }

            }
        });
    }

    if($('#approveSeal').hasClass('active')){
        if(data.approvalCode =='AGREE' && data.enterprise !='true'){
            loading.hide();
            weui.alert("", {title: '您未同意用印，审批无法通过'});
            return false;
        }else if(data.intermediary != 'true' && data.approvalCode =='AGREE'){
            loading.hide();
            weui.alert("", {title: '不同意居间费立项，不允许审批通过'});
            return false;
        }else{
            $ajax(loading,data,returnUrl)
        }
    }else{
        loading.hide();
        $ajax(loading,data,returnUrl);
    }

}

/**
 * 表单
 */
var initForm = function (type) {
    function submitForm(returnUrl) {
        var loading = weui.loading('loading', {
            className: "weui-animate-end",
        });
        var approvalCode = $("input[name='approvalCode']:checked").val();
        var data = {
            loanId: DATA.ID,
            orderId: DATA.orderId,
            taskId: DATA.taskId,
            approvalType: DATA.type,
            approvalCode: approvalCode,
            content: $("textarea[name='content']").val(),
            enterprise: $('input[name="enterprise"]:checked').val(),
            intermediary:$('input[name="intermediary"]:checked').val(),
            needRepeatFlow: false,
            flowConfigureType:'BORROW_APPLY'
        };
        if ("BACKBEGIN" === approvalCode) {
            loading.hide();
            weui.confirm('是否重走流程?', {
                title: '提示',
                buttons: [{
                        label: '取消',
                        type: 'default',
                        onClick: function(){}
                    },{
                    label: '是',
                    type: 'primary',
                    onClick: function(){
                        data.needRepeatFlow = true;
                        submitApproveForm(loading,data,returnUrl);
                    }
                }, {
                    label: '否',
                    type: 'primary',
                    onClick: function(){submitApproveForm(loading,data,returnUrl);}
                }]
            });
        } else {
            submitApproveForm(loading,data,returnUrl);
        }

    }

    //渲染风控信息
    if (type === "F" || type==="C") {
        $.ajax({
            type: "get",
            url: "/m/business_apply/query_risk_media_manifest",
            data: {
                loanId: DATA.ID,
            },
            async: true,
            success: function (response) {
                var _data = response.data;
                _data.hasFiles = function () {
                    return this.productMediaAttachDetails.length>0;
                };
                var riskMediaTemplate = utils.render("#riskMediaTemplate", _data);
                $("#riskMediaContainer").append(riskMediaTemplate);
                scrollToId();
            }
        });
    }

    weui.form.checkIfBlur('#approveForm');

    $(document).on("click", "#submitBtn", function () {
        if($("input[name='approvalCode']:checked").val()!=="AGREE"){
            $(".js-textareaNum").parent("div").prev("textarea").removeAttr("required");
        }
        weui.form.validate('#approveForm', function (error) {
            if (!error) {
                if (type == "Y") {
                    submitForm("/m/business_approval_list")
                } else if (type == "F") {
                    if("AGREE"===$("input[name='approvalCode']:checked").val()) {
                        $.ajax({
                                   type: "get",
                                   url: "/m/business_apply/risk_approve_info_complete",
                                   data: {
                                       loanId: DATA.ID
                                   },
                                   async: true,
                                   success: function (response) {
                                       if (response.isCompleted == true) {
                                           submitForm("/m/risk_approval_list");
                                       } else {
                                           weui.alert(response.msg, {title: '操作失败'});
                                       }
                                   }
                               });
                    } else {
                        submitForm("/m/risk_approval_list");
                    }
                }else if (type == "C") {
                    submitForm("/m/finance_approval_list")
                }else if(type == "G"){
                    submitForm("/m/senior_approval_list")
                }

            }
        });

    })
}


var DATA = {
    id: ''
}
function initPage() {
    id = utils.getUrlParam("id");
    DATA.ID = id;
    renderDetails(id);
}

initPage();

function clickAgree(){
    $('input[name="approvalCode"]').click(function(){
        var val =  $('input[name="approvalCode"]:checked').val();
        if(val == 'AGREE'){
            $('textarea[name="content"]').val("同意");
        }else{
            $('textarea[name="content"]').val("");
        }
    });
}

$(document).on("click", "#saveContentBtn", function () {
    weui.form.validate('#riskInfoForm', function (error) {
        if (!error) {
            $.ajax({
                       type: "post",
                       url: "/m/flow/save_loan_risk_info_content",
                       data: {
                           loanId: DATA.ID,
                           riskInfoContent: $("#loanRiskInfoContent").val(),
                       },
                       async: true,
                       success: function (response) {
                           if (response.ok == true) {
                               weui.alert("", {title: '操作成功'});
                           } else {
                               weui.alert('系统错误', {title: '操作失败'});
                           }

                       }
                   });
        }});
});

//跳转到固定锚点
function scrollToId() {
    var id = utils.getUrlParam("linkId");
    var $this = document.getElementById('js_link_'+id);
    if(id != null && $this){
        $this.scrollIntoView();
    }
}

function goHistory(){
    var type = utils.getUrlParam("from");
    if(type== 'Y'){
        location.href = '/m/business_approval_list';
    }else if(type== 'F'){
        location.href = '/m/risk_approval_list';
    }else if(type== 'C'){
        location.href ='/m/finance_approval_list';
    }else if(type== 'G'){
        location.href ='/m/senior_approval_list';
    }
}
