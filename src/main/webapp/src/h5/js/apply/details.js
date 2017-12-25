/**
 * Created by pengs on 2016/12/26.
 */
/**
 * 渲染详情全部信息
 */
var renderDetails = function (id) {

    var $details = $(utils.render("#detailsTemplate", {}))
    // .addClass('slideIn');
    $details.on('animationend webkitAnimationEnd', function () {
        $details.removeClass('slideIn').addClass('js_show');
    });
    $("#container").append($details);

    renderBasic(id, function () {
        renderBusinessApply(id, function () {
            renderApplyTpml(id)
        })
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
            };
            _data.channel_type = function(){
                if(this.channelType =='0'){
                    return true;
                }else{
                    return false;
                }
            };
            var $detailsBasicTemplate = utils.render("#detailsBasicTemplate", _data);
            $("#basicContainer").html($detailsBasicTemplate);
            callback();
        }
    });
}


/**
 * 渲染申请信息
 */
var renderBusinessApply = function (id, callback) {
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
    });
    */

    $.ajax({
        type: "get",
        url: "/m/business_apply/fetch_loan",
        data: {
            id: id
        },
        async: true,
        success: function (response) {
            var _data = response.data;
            _data.certifTypeInCN = function () {
                return enums.certifType[this.certifType];
            }
            _data.fixedAmount = function () {
                return accounting.formatNumber(this.amount, 2, ",")
            };
            _data.fixedLoanAmount = function () {
                return accounting.formatNumber(this.loan.amount, 2, ",")
            };
            _data.is_RRC = function(){
                var value = $('#productInfoTmpl').val();
                if(value.indexOf('rrcView') > -1){
                    return true;
                }else{
                    return false
                }
            };
            _data.is_shulouPlat = function() {
                var value = $('#productInfoTmpl').val();
                if (value.indexOf('shulouPlatView') > -1) {
                    return true
                } else {
                    return false
                }
            };
            _data.is_BAOLI = function () {
                var value = $('#productInfoTmpl').val();
                if(value.indexOf('baoliView') > -1){
                    return true;
                }else{
                    return false
                }
            };
            _data.is_ELSE = function(){
                var value = $('#productInfoTmpl').val();
                var removeTmp = ['baoliView','rrcView','shulouPlatView'];
                var flag;
                for(i in removeTmp){
                    if(value.indexOf(removeTmp[i]) > -1){
                        return false;
                    }else{
                        flag = true;
                    }
                }
                return flag
            };
            _data.loanTermType = function () {
                return enums.loanTermType[this.loan.termType];
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
            _data.loanRepayMethod = function () {
                return enums.loanRepayMethod[this.loan.repayMethod];
            };
            _data.feeCycleTypeInCN = function () {
                return enums.feeCycleType[this.feeCycle];
            };
            _data.feeRuleCN = function () {
                var dayText = "";
                if (this.feeType == "OVERDUE_FEE" || this.feeType == "PREPAYMENT_FEE") {
                    dayText = "*天数";
                }
                if (this.chargeType == 'FIXED_AMOUNT') {
                    return enums.feeChargeType[this.chargeType] + '(' + this.feeAmount + '元)' + dayText;
                } else if (this.chargeType == 'LOAN_REQUEST_INPUT') {
                    return enums.feeChargeType[this.chargeType] + '(' + this.minFeeAmount + "元~" + this.maxFeeAmount + '元)' + dayText;
                } else {
                    return enums.feeChargeType[this.chargeType] + '(' + this.feeRate + '%)' + dayText;
                }
            }
            _data.chargeNodeInCN = function () {
                return enums.feeChargeNode[this.chargeNode];
            }
            _data.feeTypeInCN = function () {
                return enums.feeType[this.feeType];
            }
            _data.feeAmountDetail = function () {
                if (this.feeAmount) {
                    return accounting.formatNumber(this.feeAmount, 2, ",");
                } else {
                    return "0.00";
                }

            }
            _data.loanMethod = function(){
                return enums.loanTermTypeForRate[this.loan.termType]
            }
            _data.loanIsFixAmount = function () {
                return this.loan.loanLimitType === "FIX_AMOUNT"
            }
            _data.loanIsGang = function () {
                return this.loan.loanLimitType === "--"
            }
            _data.loanIsFixRate = function () {
                return this.loan.loanLimitType === "FIX_RATE"
            }
            _data.loanInterestRateFixed2 = function () {
                return this.loan.interestRate;
            }
            _data.repayMethodIsByMonth = function () {
                return this.loan.repayMethod === "INTEREST_MONTHS";
            };
            _data.loan_grace = function(){
                if(this.loan.grace){
                    return this.loan.grace
                }
            };
            _data.repayDateTypeInCN = function () {
                return enums.repayDateType[this.loan.repayDateType];
            };
            _data.hasFiles = function () {
                return this.productMediaAttachDetails.length>0;
            };
            var $detailsDetailTemplate = utils.render("#detailsDetailTemplate", _data);
            $("#detailsdetailContainer").append($detailsDetailTemplate);
            callback();
        }
    });
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
