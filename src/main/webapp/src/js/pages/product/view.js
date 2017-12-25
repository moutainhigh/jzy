/**
 * Created by pengyueyang on 2016/12/13.
 */



$(document).ready(function () {
    //根据id获取产品信息
    var id = $("#updateProductId").val();
    //根据Id获取到数据信息
    $(document).api({
                    url:"/product/fetchById",
                    on:"now",
                    method:"post",
                    data:{"id":id},
                    onSuccess:function(data){
                        $("#updateForm")[0].reset();
                            var product = data.data;
                            $("#productTypeId").val(product.typeId);
                            $("#typeId").val(product.typeId);
                            $("#code").val(product.code);
                            $("#productCode").val(product.code);
                            $("#name").val(product.name);
                            $("#alias").val(product.alias);
                            $("#description").val(product.description);

                            //初始化复选框
                            //借款期限
                            var loanTermTypeArr = product.loanTermType.split("");
                            $("#loanTermType").val(product.loanTermType);
                            if(loanTermTypeArr[0]==1){
                                $("#loanTermType_day").attr("checked",true);
                                $("#minDays").val(product.minDays);
                                $("#maxDays").val(product.maxDays);
                            }

                            if(loanTermTypeArr[1]==1){
                                $("#loanTermType_month").attr("checked",true);
                                $("#minMonths").val(product.minMonths);
                                $("#maxMonths").val(product.maxMonths);
                            }
                            if(loanTermTypeArr[2]==1){
                                $("#loanTermType_date").attr("checked",true);
                            }
                            if(loanTermTypeArr[3]==1){
                                $("#loanTermType_season").attr("checked",true);
                                $("#minSeason").val(product.minSeasons);
                                $("#maxSeason").val(product.maxSeasons);
                            }

                            //还款方式
                            var repayMethodArr =  product.repayMethod.split("");
                            $("#repayMethod").val(product.repayMethod);
                            if(repayMethodArr[0]==1){
                                $("#repayMethod_1").attr("checked",true);
                                //还款时间
                                $("#repayDateType").val(product.repayDateType);
                                var repayDateTypeArr =  product.repayDateType.split("");
                                if(repayDateTypeArr[0]==1){
                                    $("#repayDateType_1").attr("checked",true);
                                }

                                if(repayDateTypeArr[1]==1){
                                    $("#repayDateType_2").attr("checked",true);
                                }
                            }
                            if(repayMethodArr[1]==1){
                                $("#repayMethod_2").attr("checked",true);
                            }
                            if(repayMethodArr[2]==1){
                                $("#repayMethod_3").attr("checked",true);
                            }
                            if(repayMethodArr[3]==1){
                                $("#repayMethod_4").attr("checked",true);
                            }
                            if(repayMethodArr[4]==1){
                                $("#repayMethod_5").attr("checked",true);

                                //还款时间
                                $("#repayDateType").val(product.repayDateType);
                                var repayDateType =  product.repayDateType;
                                if(repayDateType[0]==1){
                                    $("#repayDateType_1").attr("checked",true);
                                }
                                if(repayDateType[1]==1){
                                    $("#repayDateType_2").attr("checked",true);
                                }
                            }


                            //计息方式
                            var interestType =  product.interestType;
                            if (interestType) {
                              $("#interestType").val(product.interestType);
                              if(interestType[0]==1){
                                  $("#interestType_1").attr("checked",true);
                                  $("#interestAmount").val(accounting.formatNumber(product.interestAmount, 2, ""));
                                  $("#minInterestAmount").val(accounting.formatNumber(product.minInterestAmount, 2, ""));
                              }

                              if(interestType[1]==1){
                                  $("#interestType_2").attr("checked",true);
                                  $("#interestRate").val(product.interestRate);
                                  $("#minInterestRate").val(product.minInterestRate);
                              }
                            }
                            var dayInterestType =  product.dayInterestType;
                            if (dayInterestType) {
                              $("#dayInterestType").val(product.dayInterestType);
                              if(dayInterestType[0]==1){
                                  $("#dayInterestType_1").attr("checked",true);
                                  $("#dayInterestAmount").val(accounting.formatNumber(product.dayInterestAmount, 2, ""));
                                  $("#dayMinInterestAmount").val(accounting.formatNumber(product.dayMinInterestAmount, 2, ""));
                              }

                              if(dayInterestType[1]==1){
                                  $("#dayInterestType_2").attr("checked",true);
                                  $("#dayInterestRate").val(product.dayInterestRate);
                                  $("#dayMinInterestRate").val(product.dayMinInterestRate);
                              }
                            }

                            var monthInterestType =  product.monthInterestType;
                            if (monthInterestType) {
                              $("#monthInterestType").val(product.monthInterestType);
                              if(monthInterestType[0]==1){
                                  $("#monthInterestType_1").attr("checked",true);
                                  $("#monthInterestAmount").val(accounting.formatNumber(product.monthInterestAmount, 2, ""));
                                  $("#monthMinInterestAmount").val(accounting.formatNumber(product.monthMinInterestAmount, 2, ""));
                              }

                              if(monthInterestType[1]==1){
                                  $("#monthInterestType_2").attr("checked",true);
                                  $("#monthInterestRate").val(product.monthInterestRate);
                                  $("#monthMinInterestRate").val(product.monthMinInterestRate);
                              }
                            }

                        if(product.seasonInterestType){
                            var seasonInterestType = product.seasonInterestType;
                            $("#seasonInterestType").val(seasonInterestType);
                            if(seasonInterestType[0]==1){
                                $("#seasonInterestType_1").attr("checked",true);
                                $("#seasonInterestAmount").val(accounting.formatNumber(product.seasonInterestAmount, 2, ""));
                                $("#seasonMinInterestAmount").val(accounting.formatNumber(product.seasonMinInterestAmount, 2, ""));
                            }

                            if(seasonInterestType[1]==1){
                                $("#seasonInterestType_2").attr("checked",true);
                                $("#seasonInterestRate").val(product.seasonInterestRate);
                                $("#seasonMinInterestRate").val(product.seasonMinInterestRate);
                            }
                        }

                            $("#overdueDays").val(product.overdueDays);
                            $("#minAmount").val(accounting.formatNumber(product.minAmount, 2, ""));
                            $("#maxAmount").val(accounting.formatNumber(product.maxAmount, 2, ""));
                            $("#infoTmp").val(product.infoTmpId);
                            $("#mediaTmp").val(product.mediaTmpId);
                            $("#repayNotifyEarlyDays").val(product.repayNotifyEarlyDays);

                            $("input[name='product.isSendSms'][value=" + product.isSendSms + "]").attr("checked", true);
                            $("input[name='product.status'][value=" + product.status + "]").attr("checked", true);

                            //加载费用信息

                            //加载机构关联信息

                    },onFailure: function (data) {
                        $.uiAlert(
                            {
                                type: "danger",
                                textHead: '获取产品信息失败！',
                                text: data.msg,
                                time: 1
                            });
                    }
                }).trigger("ajax");

    //关闭
    $("#goBack").on("click",function(){
        window.location.href = "/product/list";
    });
    initFeeTable(id);

});

function initFeeTable(productId) {
    return $("#productFeeTable").DataTable({
             serverSide: false, //服务端分页
             searching: true, //显示默认搜索框
             ordering: false, //开启排序
             "ajax": {
                 "url": '/product_fee/list',
                 "data": {"productId": productId},
                 "dataSrc": function (json) {
                     for (var i = 0; i < json.data.length; i++) {
                         json.data[i] = $.extend({
                             id: 0,
                             code: "",
                             feeType: "",
                             feeCycle: "",
                             chargeType: "",
                             chargeNode: "",
                             chargeBase: "",
                             channelType: ""
                         }, json.data[i]);
                     }
                     return json.data
                 },
                 "type": "POST"
             },
             columns: [{
                 data: 'code'
             }, {
                 data: 'feeType',
                 render: function (data) {
                     return enums.feeType[data];
                 }
             }, {
                 data: 'feeCycle',
                 render: function (data) {
                     return enums.feeCycleType[data];
                 }
             }, {
                 data: 'chargeType',
                 render: function (data,type,row) {
                     var daysText = "";
                     if (row.feeType=="OVERDUE_FEE" || row.feeType=="PREPAYMENT_FEE") {
                         daysText = "*天数";
                     }
                     return enums.feeChargeType[data]+daysText;
                 }
             }, {
                 data: 'chargeNode',
                 render: function (data) {
                     return enums.feeChargeNode[data];
                 }
             }, {
                 data: 'chargeBase',
                 render: function (data, type, row) {
                     if (row.chargeType == 'LOAN_REQUEST_INPUT') {
                         return "前端输入";
                     }
                     if (row.chargeType == 'FIXED_AMOUNT') {
                         return data + "元";
                     }
                     return data + "%";
                 }
             },{
                 data: 'repayMethod',
                 render: function (data) {
                     return enums.loanRepayMethod[data];
                 }
             },{
                 data: 'channelType',
                 render: function (data) {
                     if(data !='ALL' && data && data !=''){
                         return enums.channelType[data];
                     }else{
                         return '全部'
                     }
                 }
             }],
             "iDisplayLength": 20,
             "aLengthMenu": [
                 [20],
                 [20]
             ]
         });
}

