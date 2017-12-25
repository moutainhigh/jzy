/**
 * Created by pengs on 2016/12/13.
 */


// 绑定上传事件
function bindUpload(){
    $(".js-uploadBtn").each(function(index) {
        var uploader = Object.create(Uploader_oneFile).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
        });
        uploader.init();
    });
}

function profitShow(){
    $('#profit_detail').modal('show');
}

/**
* 转义符
* */
function escape2Html(str) {
    var arrEntities={'lt':'<','gt':'>','nbsp':' ','amp':'&','quot':'"'};
    return str.replace(/&(lt|gt|nbsp|amp|quot);/ig,function(all,t){return arrEntities[t];});
}

var DATA = {
    code:'',
    id:"",
    taskId:"",
};


function renderBasicInfo() {
    $(document).api({
        on: "now",
        method: 'get',
        action: "get base",
        data: {
            id: utils.getUrlParam("id")
        },
        onSuccess: function (data) {
            var tab = utils.getUrlParam("tab");
            var _data = data.data;
            console.log(_data)
            DATA.code=_data.code;
            _data.businessLineCN = function () {
                return enums.business_line[this.businessLine];
            };
            _data.channel_type = function(){
                if(this.channelType =='0'){
                    return true;
                }else{
                    return false;
                }
            };

            /**
            * 默认填充展期
            * */
            if(tab == 'extension' || tab =='slBusiness' || tab =='postLoan'){
                var prd_termType = _data.termType
                _data.isExtension = function(){
                    if(utils.getUrlParam("isExtension") == 'true'){
                        return true;
                    }else{
                        return false;
                    }
                };

                switch(prd_termType){
                    case 'DAYS':
                        var interestValAboutTermType = $('#stepData').attr('data-dayInterestType');
                        break;
                    case 'MOTHS':
                        var interestValAboutTermType = $('#stepData').attr('data-monthInterestType');
                        break;
                    case 'FIXED_DATE':
                        var interestValAboutTermType = $('#stepData').attr('data-interestType');
                        break;
                    case 'SEASONS':
                        var interestValAboutTermType = $('#stepData').attr('data-seasonInterestType');
                        break;
                }

                _data.repayMethodChoices = getBinChoices("repayMethod",$('#all_repayMethod').val(),_data.repayMethod)
                _data.termTypeChoices = getBinChoices("termType",$('#stepData').attr('data-termType'),_data.termType)
                _data.interestTypeChoices = getBinChoices("interestType",interestValAboutTermType,_data.loanLimitType)

                _data.interest = function(){
                    if(this.loanLimitType =='FIX_RATE'){
                        return this.interestRate
                    }else{
                        return this.interestAmount
                    }
                };

                _data.calculate_val = function(){
                    if(this.calculateMethodAboutDay){
                        return enums.calculateMethodAboutDay[this.calculateMethodAboutDay]
                    }else{
                        return ''
                    }

                }

                extensionForm = utils.render("#extensionFormTemp",_data);

            }

            var $basicInfoTemplate = utils.render("#basicInfoTemplate", _data);
            $("#basicInfo").html($basicInfoTemplate);
        }
    });

}


var renderTablPage = {
    /**
     * 业务申请信息
     */
    businessApply: function renderBusinessApply( _data) {
        var _DATA = _data;
        /*$(document).api({
            on:'now',
            method:'post',
            action: "view profit",
            data:{
                loanId:utils.getUrlParam("id")
            },
            onSuccess:function(data){
                if(data.loanProfit){
                    var _data = data.loanProfit;
                    _data.isBill = ProductTempType.isBill();
                    _data.loanProfit = data.loanProfit
                    _data.is_loanProfit = function(){
                        if(data.code =='001' || data.loanProfit){
                            return true;
                        }else{
                            return false;
                        }
                    };
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
                $("#profitDetail").append($profitTemplate);
            }
        });*/

        $(document).api({
            on: "now",
            method: 'get',
            action: "get loan",
            data: {
                id: utils.getUrlParam("id")
            },
            onSuccess: function (data) {
                var _data = data.data;
                _data.certifTypeInCN = function () {
                    return enums.certifType[this.certifType];
                }
                _data.fixedAmount = function () {
                    return accounting.formatNumber(this.amount, 2,",")
                };
                _data.fixedLoanAmount = function () {
                    return accounting.formatNumber(this.loan.amount,2,",")
                };
                _data.loanTermType = function(){
                    return  enums.loanTermType[this.loan.termType];
                };
                _data.isFixDate = function(){
                    if(this.loan.termType == 'FIXED_DATE'){
                        return true;
                    }
                };
                _data.calculateMethod = function(){
                    return enums.calculateMethodAboutDay[this.loan.calculateMethodAboutDay];
                };
                _data.isCalculateMethod = function(){
                    if(this.loan.calculateMethodAboutDay){
                        return true;
                    }
                };
                _data.loanRepayMethod = function(){
                    return  enums.loanRepayMethod[this.loan.repayMethod];
                };
                _data.feeCycleTypeInCN = function(){
                    return enums.feeCycleType[this.feeCycle];
                };
                _data.feeRuleCN = function(){
                    var dayText = "";
                    if (this.feeType=="OVERDUE_FEE" ||this.feeType=="PREPAYMENT_FEE") {
                        dayText = "*天数";
                    }
                   if(this.chargeType=='FIXED_AMOUNT'){
                       return enums.feeChargeType[this.chargeType]+'('+this.feeAmount+'元)'+dayText;
                   }else if(this.chargeType=='LOAN_REQUEST_INPUT'){
                       return enums.feeChargeType[this.chargeType]+'('+this.minFeeAmount+"元~"+this.maxFeeAmount+'元)'+dayText;
                   }else{
                       return enums.feeChargeType[this.chargeType]+'('+this.feeRate+'%)'+dayText;
                   }
                };
                _data.chargeNodeInCN = function(){
                    return enums.feeChargeNode[this.chargeNode];
                };
                _data.feeTypeInCN = function(){
                    return enums.feeType[this.feeType];
                };
                _data.feeAmountDetail = function(){
                    if (this.feeType=="OVERDUE_FEE" ||this.feeType=="PREPAYMENT_FEE") {
                        return "--";
                    } else {
                        if (undefined != this.feeAmount && typeof this.feeAmount === 'number' && NaN != this.feeAmount) {
                            return accounting.formatNumber(this.feeAmount, 2, ",") + "元";
                        } else {
                            return "";
                        }
                    }
                };
                _data.loanIsFixAmount=function(){
                    return this.loan.loanLimitType==="FIX_AMOUNT"
                };
                _data.loanMethod = function(){
                    return enums.loanTermTypeForRate[this.loan.termType]
                };
                _data.loanIsGang=function(){
                    return this.loan.loanLimitType==="--"
                };
                _data.loanIsFixRate=function(){
                    return this.loan.loanLimitType==="FIX_RATE"
                };
                _data.loanInterestRateFixed2= function(){
                    return this.loan.interestRate;
                };
                _data.repayMethodIsByMonth = function(){
                    return this.loan.repayMethod =="INTEREST_MONTHS" || this.loan.repayMethod =='INTEREST_SEASONS' ;
                };
                _data.loan_grace = function(){
                    if(this.loan.grace){
                        return this.loan.grace;
                    }
                }
                _data.repayDateTypeInCN = function(){
                    return enums.repayDateType[this.loan.repayDateType];
                };
                _data.min_InterestAmount = function(){
                    return accounting.formatNumber(this.loan.minInterestAmount,2,",");
                };
                _data.actual_Amount = function(){
                    return accounting.formatNumber(this.loan.actualAmount,2,",");
                }
                _data.url = function (){
                    if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                        return true;
                    }else{
                        return false;
                    }
                };
                var $businessApplyTemplate = utils.render("#businessApplyTemplate", _data);
                $("#businessApply").prepend($businessApplyTemplate);
                $("#businessApply").prepend($('#profitDetail'));
                $('#profitDetail').removeClass('ks-hidden');
                //renderApplyTmpl
                if(!ProductTempType.isBill()){
                    renderLoanRepay();
                }
                renderApplyTpml(function(){
                    //渲染高管详情
                    if(utils.getUrlParam('tab') == 'senior'){
                        cloneBusiness()
                    }

                });
            }
        });

        if(_DATA.type === "Y" && utils.getUrlParam('process')==='true'){
            var $relationCode = utils.render("#relationTemplateCode", '');
            $("#businessApplyForm").append($relationCode);
            var $businessFormTemplate = utils.render("#businessFormTemplate", _DATA);
            $("#businessApplyForm").append($businessFormTemplate);
            initBusinessForm();
            clickAgree("businessForm");
            $("#businessApplyForm").show();
            $("#businessFlowSubmitBnt").on("click",function(){
                submitFlowApprove($("#businessForm"));
            });
        }

        function cloneBusiness(){
            var htm = $('#loanApplyDetail').clone();
            $('#seniorControl').prepend(htm);
            $('#seniorControl').find('.seniorItem').remove();
        }
    },
    /**
     *  业务展期
     */
    businessExtension:function(_data){
        var tab = utils.getUrlParam("tab");
        var tabList = ['slBusiness','postLoan'];
        if(tab == 'extension' || tabList.indexOf(tab)>-1){
            //历史还款计划
            $(document).api({
                on: "now",
                method: 'get',
                action: "extension query history",
                data: {
                    loanId: utils.getUrlParam("id")
                },
                onSuccess:function(data){
                    data.isBill = ProductTempType.isBill();
                    data.isExtension = utils.getUrlParam('isExtension');
                    var termType = utils.getUrlParam('isDays');

                    var is_Days = utils.getUrlParam('isDays');
                    data.isDays = function(){
                          if(is_Days =='DAYS' || is_Days == 'FIXED_DATE'){
                              return true
                          }else{
                              return false;
                          }
                    };
                    data.sl_isExtension = function(){
                        if(tabList.indexOf(tab)>-1 && data.isExtension !='true' && !data.loanExtension ){
                            return true;
                        }
                    };

                    data.is_extension = function(){
                        return this.isExtension == 'true';
                    };
                    data.f_dueDate=function(){
                        return moment(this.dueDate).format("YYYY-MM-DD");
                    };
                    data.f_amount=function(){
                        return accounting.formatNumber(this.amount,2,",");
                    };
                    data.f_interest=function(){
                        return accounting.formatNumber(this.interest,2,",");
                    };
                    data.f_total=function(){
                        return accounting.formatNumber(this.feeAmount,2,",");
                    };
                    data.f_outstanding=function(){
                        return accounting.formatNumber(this.outstanding,2,",");
                    };
                    data.f_repay_total = function(){
                        return accounting.formatNumber(this.totalAmount,2,",");
                    };
                    data.f_name=function(){
                        return enums.feeType[this.feeType];
                    };
                    data.f_feeAmount=function(){
                        return accounting.formatNumber(this.feeAmount,2,",")
                    };
                    data.f_status=function(){
                        if(null != this.loanRepay){
                            return enums.Loan_repay_status[this.status];
                        }
                        return enums.Loan_repay_status[this.status];
                    };
                    data.f_remark = function (){
                        if(this.remark){
                            return this.remark;
                        }else{
                            return '--'
                        }
                    };
                    data.last_Date = function(){
                        if(this.newRepayList){
                            var due_date = this.newRepayList[this.newRepayList.length-1].dueDate;
                            return moment(due_date).format("YYYY-MM-DD");
                        }else if(this.oldRepayList.length > 0){
                            var $this = this.oldRepayList[this.oldRepayList.length-1].length;
                            var due_date = this.oldRepayList[this.oldRepayList.length-1][$this-1].dueDate;
                            return moment(due_date).format("YYYY-MM-DD");
                        }else{
                            return '';
                        }
                    };

                    //展期详情
                    data.loanExtension_rate = function() {
                        if (this.loanLimitType == "FIX_AMOUNT") {
                            return this.interestAmount + '元/'+enums.loanTermTypeForRate[this.termType];
                        } else {
                            return this.interestRate + '%/'+enums.loanTermTypeForRate[this.termType];
                        }
                    };
                    data.loanExtension_repayMethod = function(){
                        return enums.loanRepayMethod[this.repayMethod];
                    };
                    data.loanExtension_term = function(){
                        if(this.termType !='FIXED_DATE'){
                            return this.term + enums.loanTermType[this.termType];
                        }else{
                            return '至'+this.term;
                        }
                    };
                    data.loanExtension_agreement = function(){
                        var htm = '';
                        if(this.enterpriseAgreement){
                            var agreement = JSON.parse(this.enterpriseAgreement);
                            for(i in agreement){
                                var val = agreement[i];
                                htm += '<a target="_blank" href="'+val.fileUrl+'">'+val.fileName+'</a>&emsp;'
                            }
                        }
                        return htm;
                    };
                    data.loanExtension_repayDateType = function(){
                        return enums.repayDateType[this.repayDateType]
                    };
                    data.loanExtension_calculateMethod = function(){
                        return enums.calculateMethodAboutDay[this.calculationMethod]
                    };

                    data.hasExtensionId = function(){
                        return this.extensionId ? true:false;
                    };

                    data.f_approvalStatusType = function(){
                        return enums.approvalStatusTypeList[this.approvalStatusType];
                    }

                    data.f_position = function(){
                        return this.position +1
                    }

                    var repayList = data.oldRepayList;
                    if(data.newRepayList)
                        repayList.push(data.newRepayList)
                    data.repayList = repayList;


                    var extensionHtm = utils.render("#extensionTemplate",data);
                    $('#businessExtension').append(extensionHtm);

                    //将字符串转为html
                    $('.js_agreement').each(function(){
                        var htm = escape2Html($(this).html());
                        $(this).html(htm).removeClass('ks-hidden');
                    });

                    var showTitle = function(n,decimal){
                        $('.js_title').each(function(i,ele){
                            if(n == i){
                                $(this).find('.title').html('第'+decimal+'次展期：');
                                var k = n-1;
                                var htm = $('#extensionInfo .js_detail_list:eq('+k+')').clone();
                                $(this).find('.detailInfo').append(htm);
                                $(this).find('.detailInfo .js_detail_list').removeClass('ks-hidden')
                            }
                        });
                    };

                    $('.js_his_list').each(function(n,ele){
                        var val = data.repayList[n];
                        var H='';
                        if(val[0].position > 0){
                            var decimal = val[0].position;
                            showTitle(n,decimal)
                        }
                        for(var i=0;i<val.length;i++){
                            var value = val[i];
                            H +='<tr>';
                            H +='<td>'+value.period+'</td>';
                            H +='<td >'+moment(value.dueDate).format("YYYY-MM-DD")+'</td>';
                            H +='<td class="right aligned">'+accounting.formatNumber(value.amount,2,",")+'</td>';
                            H +='<td class="right aligned">'+accounting.formatNumber(value.interest,2,",")+'</td>';
                            H +='<td class="right aligned">'+accounting.formatNumber(value.feeAmount,2,",")+'</td>';
                            H +='<td class="right aligned">'+accounting.formatNumber(value.totalAmount,2,",")+'</td>';
                            H +='<td>'+enums.Loan_repay_status[value.status]+'</td>';
                            H +='<td><a href="javascript:;" onclick="view1(\''+value.id+'\')">查看</a></td>';
                            H +='<td>'+(value.remark?value.remark:'--')+'</td>';
                            H +='</tr>';
                        }
                        $(ele).html(H);
                    });

                    if(data.rejectExtensionList){
                        for(i in data.rejectExtensionList){
                            var _data = data.rejectExtensionList[i];
                            var position = _data.position + 1;
                            var htm = $('.js_reject_list.ks-hidden:eq('+i+')').clone().removeClass('ks-hidden');
                            var positionList = [];
                            for(k in data.repayList){
                                var K = data.repayList[k];
                                positionList.push(K[0].position)
                                if(K[0].position == position){
                                    $('.js_reject_info:eq('+position+')').append(htm);
                                }
                            }
                            if(positionList.indexOf(position) == -1) $('#reject_list').append(htm);

                        }
                    }

                    // if(data.isExtension != 'true'){
                    //     $('.js_new_loan_repay').removeClass('ks-hidden');
                    //     var extensionHtm = utils.render("#extensionLoanRepayTmp",data);
                    //     $('#new_loan_repay').append(extensionHtm);
                    //
                    // }else if(data.isExtension =='true' && data.newRepayList){
                    //     $('.js_old_lastLoan_repay').removeClass('ks-hidden');
                    //     var extensionHtm = utils.render("#extensionLoanRepayTmp",data);
                    //     $('#lastLoan_repay').append(extensionHtm);
                    // }

                    $('#extensionDetailFrom').html(extensionForm);
                    var selectTermType = $("#extensionDetailFrom select[name='termType']").val();
                    if (selectTermType == 'FIXED_DATE') {
                        $('#extensionDetailInfo input[name="term"]').addClass('laydate');
                    }
                    if(data.isExtension =='true'){
                        bindUpload();
                        var code = (new Date()).Format("yyyyMMddhhmmss");
                        $('#extensionDetailFrom .js-uploadBtn').attr('data-code',code);
                    }

                    select.selectForPaymethod('extensionDetailInfo');//初始化还息类型下拉
                    select.calculate('extensionDetailInfo','calculationMethod');
                    setRepayDate('extensionDetailInfo','repayDateType');
                    if (selectTermType == 'FIXED_DATE' || selectTermType == 'DAYS') {
                        $('#extensionDetailInfo select[name="calculationMethod"]').val("CALCULATE_HEAD_AND_TAIL");
                    }


                    if(data.status && data.status == 'IN_EDIT'){
                        $('#extensionDetailInfo').form('set values',{
                            'termType':data.extension.termType,
                            'term':data.extension.term,
                            'repayMethod':data.extension.repayMethod,
                            'loanLimitType':data.extension.loanLimitType,
                            'repayDateType':data.extension.repayDateType,
                            'calculationMethod':data.extension.calculationMethod,
                            'enterpriseExplain':data.extension.enterpriseExplain,
                            'interest':data.extension.loanLimitType=='FIX_RATE' ? data.extension.interestRate:data.extension.interestAmount
                        });

                        if(data.extension.enterpriseAgreement !='[]'){
                            var list = JSON.parse(data.extension.enterpriseAgreement);
                            for(i in list){
                                var data = list[i];
                                $('.js_fileBox').append('' +
                                    '<div class="js_file mb_5">' +
                                    '<a href="'+data.fileUrl+'" target="_blank" class="js-fileName">'+data.fileName+'</a>' +
                                    '&emsp;<span class="ui red button mini js_clear">删除</span>' +
                                    '</div>' +
                                    '')
                            }
                        }

                    }

                }
            });
        }
    },
    /**
     * 风控信息
     */
    riskMng: function rendeRiskControl(_data) {
        if(_data.type === "F"  && utils.getUrlParam('process')==='true'){
            //没有风控信息，全部是表单
            $(document).api({
                on: "now",
                method: 'get',
                action: "get risk  media manifest",
                data: {
                    loanId: utils.getUrlParam("id")
                },
                onSuccess: function (data) {
                    // $.extend(_data,data.data);
                    _data.media = data.data;
                    _data.code =function(){
                        return DATA.code;
                    };
                    _data.url = function(){
                        if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                            return true;
                        }else{
                            return false;
                        }
                    };
                    DATA.riskMediaIsComplate = _data.media.flag;
                    var $riskFormTemplate = utils.render("#riskFormTemplate", _data);
                    $("#riskControl").append($riskFormTemplate);

                    var $relationCode = utils.render("#relationTemplateCode", '');
                    $("#riskFlowForm").prepend($relationCode);

                    var n = $("#risk_info").find('.js-uploadBtn').length;
                    if(ProductTempType.isBill()){
                        var $riskBillFormTemplate = utils.render("#riskBillInfo", _data);
                        $("#riskBill_info").append($riskBillFormTemplate);
                        upArryList();//创建数组
                        var b = $("#riskBill_info").find('.js-uploadBtn').length;
                        for(var i=0;i < n+b ;i++){
                            $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
                                mediaDetail[i].push({
                                    url:$(ele).attr("href"),
                                    attachName:$(ele).text()
                                })
                            });
                            var detail = JSON.stringify(mediaDetail[i]);
                            $('.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail)
                        }
                    }else{
                        upArryList();//创建数组
                        for(var i=0;i < n ;i++){
                            $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
                                mediaDetail[i].push({
                                    url:$(ele).attr("href"),
                                    attachName:$(ele).text()
                                })
                            });
                            var detail = JSON.stringify(mediaDetail[i]);
                            $('.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail)
                        }
                    }
                    clickAgree("riskFlowForm");
                    initRiskMediaForm();
                    initRiskFlowForm();
                    $("#riskFlowSubmitBnt").on("click",function(){
                        submitFlowApprove($("#riskFlowForm"));
                    });
                }
            });


        }else{
            //没有表单，全是风控信息
            $(document).api({
                on: "now",
                method: 'get',
                action: "get risk  media manifest",
                data: {
                    loanId: utils.getUrlParam("id")
                },
                onSuccess: function (data) {
                    var oData = data.data;
                    oData.url = function(){
                        if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                            return true;
                        }else{
                            return false;
                        }
                    };
                    var $riskTemplate = utils.render("#riskTemplate", oData);
                    $("#riskControl").prepend($riskTemplate);
                    if(ProductTempType.isBill()){
                        var $riskBillTemplate = utils.render("#riskBillTemplate", oData);
                        $("#detail_bill_info").prepend($riskBillTemplate);
                    }
                }
            });
        }
    },
    /**
     * 财务信息
     */
    finance:function(_data){
        if(_data.type === "C"  && utils.getUrlParam('process')==='true'){
            //没有财务信息，全部是表单
            var $financialFormTemplate = utils.render("#financialFormTemplate", _data);
            $("#financialContainer").append($financialFormTemplate);

            var $relationCode = utils.render("#relationTemplateCode", '');
            $("#financialForm").prepend($relationCode);

            clickAgree("financialForm");
            initFinancialForm();
            $("#financialFlowSubmitBnt").on("click",function(){
                submitFlowApprove($("#financialForm"));
            });

        }else{
            $.get("/loan/fetch_by_id",{
                loanId:utils.getUrlParam("id")
            },function (res) {
                var loanStatus = res.loanStatus;
                if(loanStatus=="APPROVEEND"){//待放款
                    $(document).api({
                        on: "now",
                        method: 'get',
                        action: "fetch loan info",
                        data: {
                            loanId: utils.getUrlParam("id")
                        },
                        onSuccess: function (data) {
                            data.isBill = ProductTempType.isBill();
                            var htm = utils.render("#financeFormTemplate",data);
                            $("#financialContainer").prepend(htm);
                            $("#loanTime").val(moment(new Date()).format('YYYY-MM-DD'));
                            genRepayPlan(moment(new Date()).format('YYYY-MM-DD'));

                            if(ProductTempType.productTempType == 'RRC'){
                                $("#loanAmount").text(accounting.formatNumber(data.loan.actualAmount,2,","));
                            }else{
                                $("#loanAmount").text(accounting.formatNumber(data.loan.amount,2,","));
                            }

                            $("input[name='amount']").val(data.loan.amount);
                            var loanSubject = data.loanSubject;
                            var accounts = loanSubject.accounts;
                            $("input[name='loanSubjectType']").each(function(){
                                if($(this).val()==loanSubject.type){
                                    $(this).attr("checked","checked");
                                    fillSubjectByType(loanSubject.type);
                                }
                            });
                            $("#loanSubjectName").val(loanSubject.id);
                            if(accounts.length>0){
                                $("#loanSubjectAccount").empty();
                                for(i in accounts){
                                    $("#loanSubjectAccount").append("<option value='"+accounts[i].id+"'>"+accounts[i].alias+"</option>");
                                }
                            }else{
                                $("#loanSubjectAccount").empty();
                            }

                            var loanFeeList = data.loanFeeList;
                            var d = {data:loanFeeList};
                            d.f_name=function(){
                                return enums.feeType[this.feeType];
                            };
                            d.f_feeAmount=function(){
                                return accounting.formatNumber(this.feeAmount,2,",");
                            }
                            var htm = utils.render("#feeTemplate",d);
                            $(".loanFee").html(htm);

                            $("input[name='repayDate']").each(function(){
                                $(this).val(moment(new Date()).format('YYYY-MM-DD'));
                            });
                        }
                    });
                }else if(loanStatus=='LOANED' || loanStatus=='CLEARED' || loanStatus=='OVERDUE'){//已放款
                    $(document).api({
                        on: "now",
                        method: 'get',
                        action: "fetch loan info",
                        data: {
                            loanId: utils.getUrlParam("id")
                        },
                        onSuccess: function (data) {
                            var loanSubject=data.loanSubject;
                            data.f_subjectType=function(){
                                return loanSubject.type=="ENTERPRISE"?"企业":"个人";
                            }
                            data.f_subjectAccount=function(){
                                if(loanSubject.accounts){
                                    var accounts = loanSubject.accounts;
                                    var loan = data.loan;
                                    for(i in accounts){
                                        if(loan.loanSubjectAccountId==accounts[i].id){
                                            return accounts[i].alias;
                                        }
                                    }
                                }
                                return "";
                            }
                            data.isBill = ProductTempType.isBill();
                            if(data.isBill){
                                data.f_dueDate = function(){
                                    return moment(this.loanRepay.dueDate).format("YYYY-MM-DD");
                                }
                                data.f_amount = function () {
                                    return accounting.formatNumber(this.loanRepay.amount,2,",");
                                };
                                data.f_status=function(){
                                    if(null != this.loanRepay){
                                        return enums.Loan_repay_status[this.loanRepay.status];
                                    }
                                    return enums.Loan_repay_status[this.status];
                                }
                                data.f_clear=function(){
                                    if(this.status=="CLEARED" || this.status=="OVERDUE_CLEARED" || this.status=="AHEAD_CLEARED"){
                                        return true;
                                    }
                                    return false;
                                }
                            }else{
                                data.f_dueDate=function(){
                                    return moment(this.dueDate).format("YYYY-MM-DD");
                                }
                                data.f_amount=function(){
                                    return accounting.formatNumber(this.amount,2,",");
                                }
                                data.f_interest=function(){
                                    return accounting.formatNumber(this.interest,2,",");
                                }
                                data.f_total=function(){
                                    return accounting.formatNumber(this.amount+this.interest,2,",");
                                }
                                data.f_outstanding=function(){
                                    return accounting.formatNumber(this.outstanding,2,",");
                                }
                                data.f_name=function(){
                                    return enums.feeType[this.feeType];
                                };
                                data.f_feeAmount=function(){
                                    return accounting.formatNumber(this.feeAmount,2,",")
                                }
                                data.f_repayFeeAmount=function(){
                                    return accounting.formatNumber(this.repayFeeAmount,2,",");
                                }
                                data.f_repayDate=function(){
                                    return moment(this.repayDate).format('YYYY-MM-DD');
                                }
                                data.f_status=function(){
                                    if(null != this.loanRepay){
                                        return enums.Loan_repay_status[this.loanRepay.status];
                                    }
                                    return enums.Loan_repay_status[this.status];
                                }
                                data.f_clear=function(){
                                    if(this.status=="CLEARED" || this.status=="OVERDUE_CLEARED" || this.status=="AHEAD_CLEARED"){
                                        return true;
                                    }
                                    return false;
                                }
                            }

                            data.f_actualAmount=function(){
                                return accounting.formatNumber(data.loan.actualAmount,2,",");
                            }
                            data.f_loanTime=function(){
                                return moment(data.loan.loanTime).format("YYYY-MM-DD");
                            };

                            var htm = utils.render("#financeTemplate",data);
                            $("#financialContainer").prepend(htm);

                            //放款记录
                            var loan_data = data;
                            loan_data.loan_amount = function(){
                                return accounting.formatNumber(this.loanAmount,2,",");
                            };
                            loan_data.amount_type = function(){
                                return enums.amountType[this.amountType]
                            };
                            loan_data.loan_date = function(){
                                return moment(this.loanDate).format('YYYY-MM-DD');
                            };
                            loan_data.loan_status = function(){
                                if("LOANED"==this.loanStatus){
                                    return "已放款";
                                }else {
                                    return enums.loan_status[this.loanStatus];
                                }

                            };

                            var htm = utils.render("#financeLoanRecordTemplate",loan_data);
                            $("#loanRecordInfo").html(htm);
                        }
                    });
                }
            });
        }
    },

    /**
    *  高管信息
    * */
    senior:function(_data){
        if(_data.type === "G"  && utils.getUrlParam('process')==='true'){
            //没有财务信息，全部是表单
            var $seniorFormTemplate = utils.render("#seniorFormTemplate", _data);
            $("#seniorControl").append($seniorFormTemplate);

            clickAgree("seniorForm");
            initSeniorForm();
            $("#seniorFlowSubmitBnt").on("click",function(){
                submitFlowApprove($("#seniorForm"));
            });
        }
    },


    /**
     * 贷后信息
     */
    postLoan:function(){
        var postLoanFlag = utils.getUrlParam("postLoanFlag");
        var tab = utils.getUrlParam("tab");
        if(postLoanFlag=="gather"){//进入收款
            $(document).api({
                on: "now",
                method: 'get',
                action: "query loan repay",
                data: {
                    loanId: utils.getUrlParam("id")
                },
                onSuccess: function (data) {
                    data.isBill = ProductTempType.isBill();
                    if(data.isBill){
                        data.f_dueDate = function(){
                            return moment(this.loanRepay.dueDate).format("YYYY-MM-DD");
                        }
                        data.f_amount = function () {
                            return accounting.formatNumber(this.loanRepay.amount,2,",");
                        };
                        data.f_status=function(){
                            return enums.Loan_repay_status[this.loanRepay.status];
                        }
                        data.f_clear=function(){
                            if(this.loanRepay.status=="CLEARED" || this.loanRepay.status=="OVERDUE_CLEARED" || this.loanRepay.status=="AHEAD_CLEARED"){
                                return true;
                            }
                            return false;
                        }
                    }else{
                        data.f_dueDate=function(){
                            return moment(this.dueDate).format("YYYY-MM-DD");
                        }
                        data.f_amount=function(){
                            return accounting.formatNumber(this.amount,2,",");
                        }
                        data.f_interest=function(){
                            return accounting.formatNumber(this.interest,2,",");
                        }
                        data.f_total=function(){
                            return accounting.formatNumber(this.amount+this.interest,2,",");
                        }
                        data.f_outstanding=function(){
                            return accounting.formatNumber(this.outstanding,2,",");
                        }
                        data.f_status=function(){
                            return enums.Loan_repay_status[this.status];
                        }
                        data.f_clear=function(){
                            if(this.status=="CLEARED" || this.status=="OVERDUE_CLEARED" || this.status=="AHEAD_CLEARED"){
                                return true;
                            }
                            return false;
                        }
                        data.f_repay=function(){
                            return data.period==this.period;
                        }
                    }
                    data.f_actualAmount=function(){
                        return accounting.formatNumber(data.loan.actualAmount,2,",");
                    }
                    data.f_loanTime=function(){
                        return moment(data.loan.loanTime).format("YYYY-MM-DD");
                    }
                    var htm = utils.render("#gatherTemplate",data);
                    $("#postControl").prepend(htm);
                }
            });
        }else{
            $(document).api({
                on: "now",
                method: 'get',
                action: "fetch postLoan",
                data: {
                    id: utils.getUrlParam("id"),
                    repayId: utils.getUrlParam("repayId"),
                    type: utils.getUrlParam("type"),
                },
                onSuccess: function (data) {
                    var _data = data.data;
                    _data.isBill = ProductTempType.isBill();
                    _data.post_loan = function(){
                        if(JSON.stringify(this.postLoan) == '{}'){
                            return false;
                        }else{
                            return true;
                        }
                    };
                    _data.loan_time = function(){
                        if(this.loanTime){
                            return true;
                        }else{
                            return false;
                        }
                    };
                    _data.sl_loanTime = function(){
                        if(this.loanTime){
                            return moment(this.loanTime).format("YYYY-MM-DD");
                        }else{
                            return '--';
                        }
                    };
                    if(_data.isBill){
                        _data.p_dueDate = function(){
                            return moment(this.loanRepay.dueDate).format("YYYY-MM-DD");
                        }
                        _data.p_amount = function () {
                            return accounting.formatNumber(this.loanRepay.amount,2,",");
                        };
                        _data.p_status=function(){
                            if(null != this.loanRepay){
                                return enums.Loan_repay_status[this.loanRepay.status];
                            }
                            return enums.Loan_repay_status[this.status];
                        }
                    }else{
                    _data.p_dueDate = function () {
                        return moment(this.dueDate).format("YYYY-MM-DD");
                    }
                    _data.p_amount = function () {
                        return accounting.formatNumber(this.amount,2)
                    }
                    _data.p_interest = function () {
                        return accounting.formatNumber(this.interest,2)
                    }
                    _data.p_leftAmount = function () {
                        return accounting.formatNumber(this.outstanding,2)
                    }
                    _data.p_status = function () {
                        return enums.Loan_repay_status[this.status]
                    }
                    }
                    _data.s_dueDate = function () {
                        if(_data.postLoan.dueDate){
                            return moment(_data.postLoan.dueDate).format("YYYY-MM-DD")
                        }else{
                            return '--';
                        }
                    }
                    _data.s_amount = function () {
                        return accounting.formatNumber(_data.postLoan.amount,2)
                    }
                    _data.s_interest = function () {
                        return accounting.formatNumber(_data.postLoan.interest,2)
                    }
                    _data.s_overdueFee = function () {
                        return accounting.formatNumber(_data.postLoan.overdueFee,2)
                    }

                    _data.f_feeType_post=function(){
                        return enums.feeType[this.feeType]
                    }
                    _data.f_feeAmount_post=function(){
                        return accounting.formatNumber(this.feeAmount,2,",")
                    }

                    var postLoanFeeList = {postLoanFeeList:_data.postLoanFeeList};
                    if(postLoanFeeList.length>3){
                        var postLoanFeeListOther = {};
                        for(var i = 3 ; i<=postLoanFeeList.length ;i ++){
                            postLoanFeeListOther.add(postLoanFeeList[i]);
                        }
                        _data.f_feeType_post_other=function(){
                            return enums.feeType[this.feeType]
                        }
                        _data.f_feeAmount_post_other=function(){
                            return accounting.formatNumber(this.feeAmount,2,",")
                        }
                        //$("#dynamicFee2").html(utils.render("#fee-summary-post2-template",postLoanFeeList2));
                    }
                    //$("#dynamicFee").html(utils.render("#fee-summary-post-template",postLoanFeeList));
                    var $loanedTemplate = utils.render("#loanedTemplate", _data);
                    $("#postControl").prepend($loanedTemplate);
                    if(tab =='slBusiness'){
                        $('#js_al_loanTime').removeClass('ks-hidden');
                    }
                }
            });
        }
    },
    /**
     * 推单信息
     */
    pushes:function(){
        $(document).api({
            on: "now",
            method: 'get',
            action: "query loanPushed",
            data: {
                id: utils.getUrlParam("id"),
            },
            onSuccess: function (data) {
                var _data = data.data;
                _data.push_termType = function(){
                    return  enums.loanTermType[this.termType];
                };
                _data.push_term = function(){
                    if(this.termType == 'FIXED_DATE'){
                        return  '至'+this.term;
                    }else{
                        return  this.term;
                    }

                };
                _data.push_pushTarget = function(){
                    return  enums.pushTarget[this.pushTarget];
                };
                _data.push_repayMethod = function(){
                    return  enums.loanRepayMethod[this.repayMethod];
                };
                _data.push_amount = function(){
                    return  accounting.formatNumber(this.amount,2)
                };
                _data.push_interestRate = function(){
                    return  accounting.formatNumber(this.interestRate,2)
                };
                _data.push_pushTime = function(){
                    return  moment(this.pushTime).format("YYYY-MM-DD")
                };
                _data.push_lastDueDate = function(){
                    return  moment(this.lastDueDate).format("YYYY-MM-DD")
                };
                _data.push_status = function(){
                    return  enums.loan_push_status[this.status];
                };
                var $pushesTemplate = utils.render("#pushesTemplate", _data);
                $("#pushesControl").prepend($pushesTemplate);
                if(null != utils.getUrlParam("pushType") && 'waitPush' == utils.getUrlParam("pushType")){
                    $("#push_action").show();
                }else {
                    $("#push_action").hide();
                }

            }
        });
    },


    /**
    * 审批流程详情
    * */
    flows:function(){
        $(document).api({
            on: "now",
            method: 'get',
            action: "query approval",
            data: {
                loanId: utils.getUrlParam("id"),
                flowConfigureType:'BORROW_APPLY'

            },
            onSuccess: function (data) {
                var _data = data;
                _data.approval_code_code = function () {
                    return enums.approval_code_code[this.approvalCode]
                }
                _data.approval_code_desc = function () {
                    return enums.approval_code_desc[this.approvalCode]
                }
                _data.isEditing = function () {
                    return this.nodeCode === "Y0" ;
                }
                _data.isCancel = function () {
                    return (this.nodeCode === "Y0" && this.isCancel === "true");
                }
                _data.start_Time = function(){
                    if(this.startTime){
                        return this.startTime
                    }else{
                        return '--'
                    }
                }
                _data.flowStatus = function(){
                    return enums.flowType[this.approvalType];
                }
                _data.timeRange = function(){
                    if(this.duration == 0){
                        return '--'
                    }else{
                        return this.duration
                    }
                }
                var $businessFlowTemplate = utils.render("#businessFlowTemplate", _data);
                $("#flowInfo").append($businessFlowTemplate);
            }
        });
    }

}


function initTAbs( type ) {
    //todo 根据链接参数选中对应的tab;
    var tab = utils.getUrlParam("tab");
    var typeStatus = utils.getUrlParam("typeStatus");
    var mortgage = utils.getUrlParam("mortgage");
    var $js_DetailsMenu = $(".js-detailsMenu");
    switch (tab){
        case "business":
            $js_DetailsMenu.find(".item[data-tab='businessApply']").addClass("active");
            $(".js-tab[data-tab='businessApply']").addClass("active");
            break;
        case "extension":
            $js_DetailsMenu.find(".item[data-tab='businessExtension']").addClass("active");
            $(".js-tab[data-tab='businessExtension']").addClass("active");
            break;
        case "riskControl":
            $js_DetailsMenu.find(".item[data-tab='riskMng']").addClass("active");
            $(".js-tab[data-tab='riskMng']").addClass("active");
            break;
        case "financial":
            $js_DetailsMenu.find(".item[data-tab='finance']").addClass("active");
            $(".js-tab[data-tab='finance']").addClass("active");
            //解押流程详情超链接隐藏其他tab
            if(null != mortgage && "mortgage"==mortgage){
                $js_DetailsMenu.find(".item[data-tab='finance']").text("贷后信息");
                $js_DetailsMenu.find(".item[data-tab='finance']").addClass("active");
                $js_DetailsMenu.find(".item[data-tab='businessApply']").remove();
                $(".js-tab[data-tab='businessApply']").remove();
                $js_DetailsMenu.find(".item[data-tab='riskMng']").remove();
                $(".js-tab[data-tab='riskMng']").remove();
                $js_DetailsMenu.find(".item[data-tab='flows']").remove();
                $(".js-tab[data-tab='flows']").remove();
                $("#loanBasicDetail").remove();
            }
            break;
        case "senior":
            $js_DetailsMenu.find(".item[data-tab='senior']").addClass("active");
            $(".js-tab[data-tab='senior']").addClass("active");
        case "postLoan":
            if(null != typeStatus && "CLEARED"==typeStatus){
                $js_DetailsMenu.find(".item[data-tab='finance']").text("贷后信息");
                $js_DetailsMenu.find(".item[data-tab='finance']").addClass("active");
                $(".js-tab[data-tab='finance']").addClass("active");
                $js_DetailsMenu.find(".item[data-tab='postLoan']").remove();
                $(".js-tab[data-tab='postLoan']").remove();
            }else {
                $js_DetailsMenu.find(".item[data-tab='postLoan']").addClass("active");
                $(".js-tab[data-tab='postLoan']").addClass("active");
            }
            break;
        case "pushes":
            $js_DetailsMenu.find(".item[data-tab='pushes']").addClass("active");
            $(".js-tab[data-tab='pushes']").addClass("active");
            break;
        case "flows":
            $js_DetailsMenu.find(".item[data-tab='flows']").addClass("active");
            $(".js-tab[data-tab='flows']").addClass("active");
            break;
        default:
            //默认到第一个选项卡
            $(".js-detailsMenu .item:first").addClass("active")
            $(".js-tab:first").addClass("active")
            break;
    }



    /**
     *  根据tab来决定渲染对应的详情页面
     */
    $(".js-detailsMenu__item").each(function (index, element) {
        renderTablPage[$(this).data("tab")]( type );
    })


}


/**
 * query approval
 */
function queerUserApproval(){
    $(document).api({
        on: "now",
        method: 'get',
        action: "query user approval",
        data: {
            "loanId": utils.getUrlParam("id"),
            "flowConfigureType":'BORROW_APPLY'
        },
        onSuccess: function (data) {
            var _data = data.data;
            if(_data){
                _data.isBill = ProductTempType.isBill();
                DATA.id= _data.workItem.id;
                DATA.taskId= _data.workItem.taskId;
                DATA.orderId= _data.workItem.orderId;
                DATA.type= _data.type;
                _data.resultEnums = function(){
                    var r=[];
                    for(var x in enums.approval_code_desc){
                        r.push({
                            value:x,
                            name:enums.approval_code_desc[x]
                        })
                    }
                    return r;
                }
            }else {
                _data ={
                    type:""
                }
            }
            initTAbs( _data );
        }
    });
}


$.fn.form.settings.rules.is_agree1 = function(value,obj){
    var spType = $('#'+obj+' input[name="approvalCode"]:checked').val();
    var yyType = $('#'+obj+' input[name="enterprise"]:checked').val();
    if(spType =='AGREE' && yyType!='true'){
        return false;
    }else{
        return true;
    }
};

$.fn.form.settings.rules.is_agree2 = function(value,obj){
    var spType = $('#'+obj+' input[name="approvalCode"]:checked').val();
    var yyType = $('#'+obj+' input[name="enterprise"]:checked').val();
    if(spType !='AGREE' && yyType =='true'){
        return false;
    }else{
        return true;
    }
};

$.fn.form.settings.rules.isAgree = function(value,obj){
    var spType = $('#'+obj+' input[name="approvalCode"]:checked').val();
    var intermediaryType = $('#'+obj+' input[name="intermediary"]:checked').val();
    if(intermediaryType != 'true' && spType =='AGREE'){
        return false;
    }else{
        return true;
    }
};

/**
 * 初始化业务审批 表单
 */
function initBusinessForm(){
    $("#businessForm").form({
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
                    },{
                        type:'is_agree2[businessApplyForm]',
                        prompt:'请在用审批结果选择“同意”'
                    }
                ]
            },
            enterprise:{
                identifier:'enterprise',
                rules: [{
                    type:'is_agree1[businessApplyForm]',
                    prompt:'您未同意用印，审批无法通过'
                }]
            },
            intermediary:{
                identifier:'intermediary',
                rules:[{
                    type:'isAgree[businessApplyForm]',
                    prompt:'请选择同意居间费立项'
                }]
            }

        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "flow node approval",
        method: 'POST',
        serializeForm: true,
        // data: _data,
        beforeSend: function (settings) {
            settings.data["loanId"] =  utils.getUrlParam("id");
            settings.data["orderId"] =  DATA.orderId;
            settings.data["taskId"] =  DATA.taskId;
            settings.data["approvalType"] =  DATA.type;
            settings.data["flowConfigureType"] =  'BORROW_APPLY';
            return settings;
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
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
    })
}


function clearMedia($item){
    $item.find("input[name='url']").val("");
    $item.find("input[name='attachName']").val("");
}


/**
 * 初始化 风控审批信息 表单
 */
function initRiskMediaForm(){

// 绑定上传事件
    $(".js-uploadBtn").each(function(index) {;
        var uploader = Object.create(Uploader).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
        });
        uploader.init();
    });
// 点击上传按钮
    $(document).on("click",".js-uploadBtn",function() {
        var $item = $(this).parents("tr");
        // clearMedia($item);
    });

//点击按钮删除对应文件
    $(document).on('click','.js-remove',function(){
        var parentsDel = $(this).parents('.js-canBeDel');
        var k = $('tr.mediaItem').index($(this).parents('tr.mediaItem'));
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

    $("#riskMediaForm").form({
        fields: {
            content: {
                identifier: 'content',
                rules: [
                    {
                        type: 'empty',
                        prompt: '{name}不能为空'
                    },{
                        type:'maxLength[500]',
                        prompt:'授信方案不超过500个字符'
                    }
                ]
            }
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "update risk  media",
        method: 'POST',
        serializeForm: true,
        // data: _data,
        beforeSend: function (settings) {
            var loanId = utils.getUrlParam("id")
            settings.data["loanId"] = loanId;
            var medias = [];
            var errors = [];
            $("#riskMediaForm tr.mediaItem").each(function() {
                var $item = $(this);
                var required = $item.find("input[name='required']").val();
                var mediaInfo = $item.find("input[name='mediaDetail']").val();
                var productMediaAttachId = $item.find("input[name='productMediaAttachId']").val();
                if(required == "true" && (mediaInfo == "" || mediaInfo == "[]")) {
                    $item.addClass("error");
                    errors.push('<li>必填项必须上传</li>');
                } else {
                    $item.removeClass("error");
                }

                medias.push({
                    'loanId': loanId,
                    'itemName': $item.find("input[name='itemName']").val(),
                    'required': required,
                    'productMediaAttachDetails':mediaInfo,
                    'mediaItemType': $item.find("input[name='mediaItemType']").val(),
                    'id':productMediaAttachId
                })
            });
            settings.data["medias"] = JSON.stringify(medias);

            if(errors.length == 0) {
                return settings;
            } else {
                $("#riskMediaForm .ui.error.message ul").html(errors.join(""));
                $("#riskMediaForm").addClass("error");
                return false;
            }
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
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
    })
}

/**
 * 初始化 风控审批流程 表单
 */
function initRiskFlowForm(){
    $("#riskFlowForm").form({
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
            enterprise:{
                identifier:'enterprise',
                rules: [{
                    type:'is_agree1[riskFlowForm]',
                    prompt:'您未同意用印，审批无法通过'
                }]
            },
            intermediary:{
                identifier:'intermediary',
                rules:[{
                    type:'isAgree[riskFlowForm]',
                    prompt:'不同意居间费立项，不允许审批通过'
                }]
            }

        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "flow node approval",
        method: 'POST',
        serializeForm: true,
        // data: _data,
        beforeSend: function (settings) {
            settings.data["loanId"] =  utils.getUrlParam("id");
            settings.data["orderId"] =  DATA.orderId;
            settings.data["taskId"] =  DATA.taskId;
            settings.data["approvalType"] =  DATA.type;
            settings.data["flowConfigureType"] =  'BORROW_APPLY';
            if(DATA.riskMediaIsComplate || DATA.approvalCode !='AGREE'){
                return settings
            }else{
                $.uiAlert({
                    type: "danger",
                    textHead:  '失败',
                    text:"请确认资料全部已提交",
                    time: 3,
                });
                return false;
            }

        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
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
    })
}



/**
 * 初始化财务审批 表单
 */
function initFinancialForm(){
    $("#financialForm").form({
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
            enterprise:{
                identifier:'enterprise',
                rules: [{
                    type:'is_agree1[financialForm]',
                    prompt:'您未同意用印，审批无法通过'
                }]
            },
            intermediary:{
                identifier:'intermediary',
                rules:[{
                    type:'isAgree[financialForm]',
                    prompt:'不同意居间费立项，不允许审批通过'
                }]
            }

        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "flow node approval",
        method: 'POST',
        serializeForm: true,
        // data: _data,
        beforeSend: function (settings) {
            settings.data["loanId"] =  utils.getUrlParam("id");
            settings.data["orderId"] =  DATA.orderId;
            settings.data["taskId"] =  DATA.taskId;
            settings.data["approvalType"] =  DATA.type;
            settings.data["flowConfigureType"] =  'BORROW_APPLY';
            return settings
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
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
    })
}


/**
* 初始化高管审批表单
* */
function initSeniorForm(){
    $("#seniorForm").form({
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
            enterprise:{
                identifier:'enterprise',
                rules: [{
                    type:'is_agree1[seniorForm]',
                    prompt:'您未同意用印，审批无法通过'
                }]
            },
            intermediary:{
                identifier:'intermediary',
                rules:[{
                    type:'isAgree[seniorForm]',
                    prompt:'不同意居间费立项，不允许审批通过'
                }]
            }

        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "flow node approval",
        method: 'POST',
        serializeForm: true,
        // data: _data,
        beforeSend: function (settings) {
            settings.data["loanId"] =  utils.getUrlParam("id");
            settings.data["orderId"] =  DATA.orderId;
            settings.data["taskId"] =  DATA.taskId;
            settings.data["approvalType"] =  DATA.type;
            settings.data["flowConfigureType"] =  'BORROW_APPLY';
            return settings
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
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
    })
}



function bindEvents() {
    $(".js-detailsMenu .item").tab();

}

function initPage() {
    renderBasicInfo();
    // initTAbs();
    bindEvents();

    queerUserApproval();
}

initPage();

//初始化推单信息
$(document).on("click", "#push",function (e) {
    if (!initPushLoanFlag) {

        $(document).api({
            on: "now",
            method: 'get',
            url: "/loan_push/init_loan_push",
            data: {
                loanId: utils.getUrlParam("id"),
            },
            onSuccess: function (data) {
                //推送目标
                var pushTargeData = data.pushTargetList;
                pushTargeData.text = function () {
                    return enums.pushTarget[this.value]
                }
                var $pushTargetTemplate = utils.render("#pushTargetTemplate", pushTargeData);
                $(".js-pushTarget .menu").append($pushTargetTemplate);
                $(".js-pushTargetDropdown").dropdown();
                $(".js-pushTarget .text").text("佳兆业金服");
                $("#pushTarget").val("KAISAFAX");

                //期限类型
                var termTypeData = data.termTypeList;
                termTypeData.text = function () {
                    return enums.pushTermType[this.value]
                }
                var $termTypeTemplate = utils.render("#termTypeTemplate", termTypeData);
                $(".js-termType .menu").append($termTypeTemplate);
                $(".js-termTypeDropdown").dropdown();

                //付款方式
                var repayMethodData = data.repayMethodList;
                repayMethodData.text = function () {
                    return enums.loanRepayMethod[this.value]
                }
                var $repayMethodTemplate = utils.render("#repayMethodTemplate", repayMethodData);
                $(".js-repayMethod .menu").append($repayMethodTemplate);
                $(".js-repayMethodDropdown").dropdown();

                $("#productTypeName").val(data.list[0].productType.name);
                $('input[name="loanPush.loanId"]').val(data.list[0].id);
                $('input[name="loanPush.masterBorrowerId"]').val(data.list[0].masterBorrowerId);
                $("#borrserName").val(data.list[0].masterBorrower.name);
                $('input[name="loanPush.productTypeId"]').val(data.list[0].productTypeId);
                $("#telephone").val(data.list[0].masterBorrower.phone);
                initModelSearchTypeDropdown();
                $("#pushTime").val(moment(new Date()).format('YYYY-MM-DD'));
                addLoanPush();

                initPushLoanFlag = true;
                $("#push_modal").modal({
                    autofocus:false
                }).modal("show");
            },
            onFailure: function (data) {
                $.uiAlert(
                    {
                        type: "danger",
                        textHead: '推单失败',
                        text: data.msg,
                        time: 2,
                        onClosed: function () {
                            $('#push_modal').modal('hide');
                        }
                    });
            }
        });

    }else{
        $("#push_modal").modal({
            autofocus:false,
            observeChanges: true,
        }).modal("show");
    }


});

// function getDate(){
//     var mydate = new Date();
//     var str = "" + mydate.getFullYear() + "-";
//     if(mydate.getMonth()<10){
//         str += "0"+(mydate.getMonth()+1) + "-";
//     }else {
//         str += (mydate.getMonth()+1) + "-";
//     }
//     str += mydate.getDate();
//     return str;
// }

// 项目期限类别下拉列表改变
function initModelSearchTypeDropdown() {
    $(".js-termTypeDropdown").dropdown({
        onChange: function (value, text, $selectedItem) {
            if (value == "FIXED_DATE") {
                $("#term")[0].type = 'date';
            }else {
                $("#term")[0].type = 'text';
            }
        }
    });
}

var initPushLoanFlag = false;

//提交表单
$(document).on("click", "#addBnt",function () {
    $("#push_form").submit();
})


//推单表单校验
var validateOptions = {
    inline: true,
    fields: {
        pushTarget: {
            identifier: 'loanPush.pushTarget',
            rules: [
                {
                    type: 'empty',
                    prompt: '推送目标不能为空'
                }
            ]
        },
        termType: {
            identifier: 'loanPush.termType',
            rules: [
                {
                    type: 'empty',
                    prompt: '期限类型不能为空'
                }

            ]
        },
        amount: {
            identifier: 'loanPush.amount',
            rules: [
                {
                    type: 'empty',
                    prompt: '项目金额不能为空'
                },
                {
                    type: 'maxLength[12]',
                    prompt: '最大长度为12位'
                }

            ]
        },
        repayMethod: {
            identifier: 'loanPush.repayMethod',
            rules: [
                {
                    type: 'empty',
                    prompt: '还款方式不能为空'
                }
            ]
        },
        term: {
            identifier: 'loanPush.term',
            rules: [
                {
                    type: 'empty',
                    prompt: '项目期限不能为空'
                }
            ]
        },
        interestRate: {
            identifier: 'loanPush.interestRate',
            rules: [
                {
                    type: 'empty',
                    prompt: '年化利率不能为空'
                },
                {
                    type: 'maxLength[12]',
                    prompt: '最大长度为12位'
                },
                {
                    type: 'canBeDecimal[10]',
                    prompt: '请输入数字或者小数，小数点后不能超过10位的小数'
                }
            ]
        },  serviceFee: {
            identifier: 'loanPush.serviceFee',
            rules: [
                {
                    type: 'empty',
                    prompt: '借款服务费不能为空'
                },
                {
                    type: 'maxLength[12]',
                    prompt: '最大长度为12位'
                },
                {
                    type: 'canBeDecimal[10]',
                    prompt: '请输入数字或者小数，小数点后不能超过10位的小数'
                }
            ]
        },  guaranteeFee: {
            identifier: 'loanPush.guaranteeFee',
            rules: [
                {
                    type: 'empty',
                    prompt: '借款担保费不能为空'
                },
                {
                    type: 'maxLength[12]',
                    prompt: '最大长度为12位'
                },
                {
                    type: 'canBeDecimal[10]',
                    prompt: '请输入数字或者小数，小数点后不能超过10位的小数'
                }
            ]
        },  manageFee: {
            identifier: 'loanPush.manageFee',
            rules: [
                {
                    type: 'empty',
                    prompt: '资金管理费不能为空'
                },
                {
                    type: 'maxLength[12]',
                    prompt: '最大长度为12位'
                },
                {
                    type: 'canBeDecimal[10]',
                    prompt: '请输入数字或者小数，小数点后不能超过10位的小数'
                }
            ]
        },
        pushTime: {
            identifier: 'loanPush.pushTime',
            rules: [
                {
                    type: 'empty',
                    prompt: '推单日期不能为空'
                }
            ]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};
//推单
function addLoanPush() {
    $('#push_form').form(validateOptions).api({
        action: 'add loanPush',
        method: 'POST',
        serializeForm: true,
        onSuccess: function (data) {
            $.uiAlert(
                {
                    type: "success",
                    textHead: '推单成功',
                    text: '推单成功',
                    time: 1,
                    onClosed: function () {
                        $('#push_modal').modal('hide');
                        window.location.reload();
                    }
                });
        },
        onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '推单失败',
                    text: data.msg,
                    time: 2,
                    onClosed: function () {
                        $('#push_modal').modal('hide');
                    }
                });
        }
    });
}
//查看推单详情
function viewLoanPushInfo(id) {
    $(document).api({
        on: "now",
        method: 'get',
        action: "fetch loanPushDetail",
        data: {
            id : id,
        },
        onSuccess: function (data) {
            var _data = data.list;
            _data[0].repayMethodInfo = function () {
                return enums.loanRepayMethod[this.repayMethod]
            }
            _data[0].termTypeInfo = function () {
                return enums.loanTermType[this.termType]
            }
            _data[0].pushTargetInfo = function () {
                return enums.pushTarget[this.pushTarget]
            }
            _data[0].s_amount = function () {
                return accounting.formatNumber(this.amount,2)
            }
            _data[0].s_interestRate = function () {
                return accounting.formatNumber(this.interestRate,2)
            }
            _data[0].s_guaranteeFee = function () {
                return accounting.formatNumber(this.guaranteeFee,2)
            }
            _data[0].s_serviceFee = function () {
                return accounting.formatNumber(this.serviceFee,2)
            }
            _data[0].s_manageFee = function () {
                return accounting.formatNumber(this.manageFee,2)
            }
            _data[0].s_pushTime = function () {
                return moment(this.pushTime).format("YYYY-MM-DD")
            }
            var $pushDetailTemplate = utils.render("#pushDetailTemplate", _data[0]);
            $("#loanPushInfo").html($pushDetailTemplate);
            $("#view_modal").modal("show");
        }
    });
}
//完成推单
function finishPushLoan() {

    $.uiDialog("您确认该笔借款已完全推送完成吗？", {
        onApprove: function () {
            var $modal = $(this);
            $(document).api({
                on: "now",
                method: 'post',
                action: "update loanPush",
                data: {
                    id: utils.getUrlParam("id"),
                },
                onSuccess: function (data) {
                    $.uiAlert({
                        type: "success",
                        textHead: '推送完成',
                        text: '推送完成',
                        time: 1,
                    });
                    $("#push").attr("disabled", true);
                },
                onFailure: function (data) {
                    $.uiAlert({
                        type: "danger",
                        textHead: '推送失败',
                        text: data.msg,
                        time: 2
                    });
                }
            });
        }
    })
}
//关闭窗口
function closeModal() {
    $("#push_modal").modal("hide");
    //window.location.reload();
}
//查看还款记录
function view_post_repay(repayId){
    $("#repayId").val(repayId);
    $.get("/loan/query_repay_record",{
        loanId:utils.getUrlParam("id"),
        repayId:repayId
    },function(data){
        data.isBill = ProductTempType.isBill();
        data.f_repayDate=function(){
            return moment(this.repayDate).format("YYYY-MM-DD");
        }
        data.f_repayAmount=function(){
            return accounting.formatNumber(this.repayAmount,2);
        }
        data.f_repayInterest=function(){
            return accounting.formatNumber(this.repayInterest,2);
        }
        data.f_repayTotalAmount=function(){
            return accounting.formatNumber(this.repayTotalAmount,2);
        }
        data.f_feeType=function(){
            return enums.feeType[this.feeType];
        }
        $("#repayViewTable").html(utils.render("#repay-template",data));
    });
    $("#gatherRecordViewModalForPost").modal({
        observeChanges: true,
        blurring: true
    }).modal('show')
}

//同意
function clickAgree(obj){
    var obj = obj ? '#'+obj : '';
    $('input[name="approvalCode"]').click(function(){
        var val =  $('input[name="approvalCode"]:checked').val();
        DATA.approvalCode = val;
        if(val == 'AGREE'){
            $(''+obj+' textarea[name="content"]').val("同意");
        }else{
            $(''+obj+' textarea[name="content"]').val("");
        }
    });
}

function submitFlowApprove(obj) {
     if ("BACKBEGIN" === DATA.approvalCode) {
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
}

