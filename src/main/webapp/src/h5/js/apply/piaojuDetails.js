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
            $("#borrowerInfo").append($detailsDetailTemplate);
            var $applyTemplate = utils.render("#applyTemplate", response);
            $("#detailsdetailContainer").append($applyTemplate);
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
    getAttachs();
}

initPage();


function getAttachs() {
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

       }
   });
}
