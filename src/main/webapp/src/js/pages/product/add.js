/**
 * Created by pengyueyang on 2016/12/8.
 */
function add() {
    $("#productCode").val($("#add_product_code").val());
    $("#addForm").submit();
}

$.fn.form.settings.rules.productCodeSpecial = function(value){
    return /[A-Z|a-z|\-|0-9]+$/.test(value) || value == "";
};

function initAddForm() {
    $("#addForm").form({
       fields: {
           typeId: {
               identifier: 'product.typeId',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择产品大类'
                   }
               ]
           },
           input_code: {
               identifier: 'input_code',
               rules: [
                   {
                       type: 'empty',
                       prompt: '产品编号不能为空'
                   },{
                       type: 'exactLength[8]',
                       prompt: '产品编号只能为8位字符'
                   },{

                       type: 'productCodeSpecial',
                       prompt: '产品编号只允许输入数字、字母和‘-’'
                   }
               ]
           },
           name: {
               identifier: 'product.name',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请输入产品名称'
                   }, {
                       type: 'maxLength[50]',
                       prompt: '产品名称不能多于50个字符'
                   },{

                       type: 'validateNotSpecial',
                       prompt: '产品名称不允许输入特殊字符'
                   }
               ]
           },
           alias: {
               identifier: 'product.alias',
               rules: [
                   {
                       type: 'maxLength[50]',
                       prompt: '产品别名不能多于50个字符'
                   },{

                       type: 'validateNotSpecial',
                       prompt: '产品别名不允许输入特殊字符'
                   }
               ]
           },
           description: {
               identifier: 'product.description',
               rules: [
                   {
                       type: 'maxLength[300]',
                       prompt: '输入产品描述不能多于300个字符'
                   },{

                       type: 'validateNotSpecial',
                       prompt: '输入产品描述不允许输入特殊字符'
                   }
               ]
           },
           loanTermType: {
               identifier: 'product.loanTermType',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择借款期限'
                   }
               ]
           }, minDays: {
               identifier: 'product.minDays',
               depends    : 'loanTermType_day',
               rules: [
                   {
                       type: 'integer[0..1000]',
                       prompt: '请输入正确的最小借款期限天数（0-1000）'
                   }
               ]
           },maxDays: {
               identifier: 'product.maxDays',
               depends    : 'loanTermType_day',
               rules: [
                   {
                       type: 'integer[0..1000]',
                       prompt: '请输入正确的最大借款期限天数（0-1000）'
                   },{
                       type: 'greater[minDays]',
                       prompt: '借款期限最大天数必须大于最小天数'
                   }
               ]
           }, minMonths: {
                identifier: 'product.minMonths',
                depends    : 'loanTermType_month',
                rules: [
                    {
                        type: 'integer[0..36]',
                        prompt: '请输入正确的最小借款期限月数（0-36）'
                    }
                ]
            },maxMonths: {
                identifier: 'product.maxMonths',
                depends    : 'loanTermType_month',
                rules: [
                    {
                        type: 'integer[0..36]',
                        prompt: '请输入正确的最大借款期限月数（0-36）'
                    },{
                        type: 'greater[minMonths]',
                        prompt: '借款期限最大月数必须大于最小月数'
                    }
                ]
            },minSeasons:{
               identifier: 'product.minSeasons',
               depends:'loanTermType_season',
               rules:[{
                   type: 'integer[0..999]',
                   prompt: '请输入正确的最小季度数（0-999）'
               }]
           },maxSeasons:{
               identifier: 'product.maxSeasons',
               depends:'loanTermType_season',
               rules:[{
                   type: 'integer[0..999]',
                   prompt: '请输入正确的最大季度数（0-999）'
               },{
                   type: 'greater[minSeason]',
                   prompt: '借款期限最大季度数必须大于最小季度数'
               }]
           },dayInterestType: {
               identifier: 'product.dayInterestType',
               depends: 'loanTermType_day',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择计息方式（按天计算）'
                   }
               ]
           },dayInterestAmount: {
                identifier: 'product.dayInterestAmount',
                depends    : 'dayInterestType_1',
                rules: [
                    {
                        type: 'validateDecimal2',
                        prompt: '请输入正确的计息固定费用（按天计算,最多两位小数）'
                    },{
                        type:'maxLength[16]',
                        prompt:'最长16位'
                    }
                ]
            },dayMinInterestAmount: {
               identifier: 'product.dayMinInterestAmount',
               depends    : 'dayInterestType_1',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的计息固定费用最小值（按天计算,最多两位小数）'
                   },{
                       type: 'smaller[dayInterestAmount]',
                       prompt: '请输入正确的计息固定费用最小值,必须小于等于固定费用（按天计算）'
                   },{
                       type:'maxLength[16]',
                       prompt:'最长16位'
                   }
               ]
           },dayInterestRate: {
               identifier: 'product.dayInterestRate',
               depends    : 'dayInterestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率（按天计算,最多十位小数）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },dayMinInterestRate: {
               identifier: 'product.dayMinInterestRate',
               depends    : 'dayInterestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率最小值（按天计算,最多十位小数）'
                   },{
                       type: 'smaller[dayInterestRate]',
                       prompt: '请输入正确的计息固定费率最小值,必须小于等于固定费率（按天计算）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },monthInterestType: {
               identifier: 'product.monthInterestType',
               depends: 'loanTermType_month',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择计息方式（按月计算）'
                   }
               ]
           },monthInterestAmount: {
                identifier: 'product.monthInterestAmount',
                depends    : 'monthInterestType_1',
                rules: [
                    {
                        type: 'validateDecimal2',
                        prompt: '请输入正确的计息固定费用（按月计算,最多两位小数）'
                    },{
                        type:'maxLength[16]',
                        prompt:'最长16位'
                    }
                ]
            },monthMinInterestAmount: {
               identifier: 'product.monthMinInterestAmount',
               depends    : 'monthInterestType_1',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的计息固定费用最小值（按月计算,最多两位小数）'
                   },{
                       type: 'smaller[monthInterestAmount]',
                       prompt: '请输入正确的计息固定费用最小值,必须小于等于固定费用（按月计算）'
                   },{
                       type:'maxLength[16]',
                       prompt:'最长16位'
                   }
               ]
           },monthInterestRate: {
               identifier: 'product.monthInterestRate',
               depends    : 'monthInterestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率（按月计算,最多十位小数）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },monthMinInterestRate: {
               identifier: 'product.monthMinInterestRate',
               depends    : 'dayInterestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率最小值（按月计算,最多十位小数）'
                   },{
                       type: 'smaller[monthInterestRate]',
                       prompt: '请输入正确的计息固定费率最小值,必须小于等于固定费率（按月计算）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },seasonInterestType:{
               identifier: 'product.seasonInterestType',
               depends:'loanTermType_season',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择计息方式（按季计算）'
                   }
               ]
           },seasonInterestAmount: {
                identifier: 'product.monthInterestAmount',
                depends    : 'seasonInterestType_1',
                rules: [
                    {
                        type: 'validateDecimal2',
                        prompt: '请输入正确的计息固定费用（按月计算,最多两位小数）'
                    },{
                        type:'maxLength[16]',
                        prompt:'最长16位'
                    }
                ]
           },seasonMinInterestAmount: {
               identifier: 'product.seasonMinInterestAmount',
               depends    : 'seasonInterestType_1',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的计息固定费用最小值（按季计算,最多两位小数）'
                   },{
                       type: 'smaller[seasonInterestAmount]',
                       prompt: '请输入正确的计息固定费用最小值,必须小于等于固定费用（按季计算）'
                   },{
                       type:'maxLength[16]',
                       prompt:'最长16位'
                   }
               ]
           },seasonInterestRate: {
                identifier: 'product.seasonInterestRate',
                depends    : 'seasonInterestType_2',
                rules: [
                    {
                        type: 'canBeDecimal[10]',
                        prompt: '请输入正确的计息固定费率（按季计算,最多十位小数）'
                    },{
                        type:'maxLength[24]',
                        prompt:'最长24位'
                    }
                ]
           },seasonMinInterestRate: {
               identifier: 'product.seasonMinInterestRate',
               depends    : 'seasonInterestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率最小值（按季计算,最多十位小数）'
                   },{
                       type: 'smaller[seasonInterestRate]',
                       prompt: '请输入正确的计息固定费率最小值,必须小于等于固定费率（按季计算）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },


           interestType: {
               identifier: 'product.interestType',
               depends: 'loanTermType_date',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择计息方式（固定时间还款）'
                   }
               ]
           },interestAmount: {
                identifier: 'product.interestAmount',
                depends    : 'interestType_1',
                rules: [
                    {
                        type: 'validateDecimal2',
                        prompt: '请输入正确的计息固定费用（固定时间还款,最多两位小数）'
                    },{
                        type:'maxLength[16]',
                        prompt:'最长16位'
                    }
                ]
            },minInterestAmount: {
               identifier: 'product.minInterestAmount',
               depends    : 'interestType_1',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的计息固定费用最小值（固定时间还款,最多两位小数）'
                   },{
                       type: 'smaller[interestAmount]',
                       prompt: '请输入正确的计息固定费用最小值,必须小于等于固定费用（固定时间还款）'
                   },{
                       type:'maxLength[16]',
                       prompt:'最长16位'
                   }
               ]
           },interestRate: {
               identifier: 'product.interestRate',
               depends    : 'interestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率（固定时间还款,最多十位小数）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },minInterestRate: {
               identifier: 'product.minInterestRate',
               depends    : 'interestType_2',
               rules: [
                   {
                       type: 'canBeDecimal[10]',
                       prompt: '请输入正确的计息固定费率最小值（固定时间还款,最多十位小数）'
                   },{
                       type: 'smaller[interestRate]',
                       prompt: '请输入正确的计息固定费率最小值,必须小于等于固定费率（固定时间还款）'
                   },{
                       type:'maxLength[24]',
                       prompt:'最长24位'
                   }
               ]
           },repayMethod: {
               identifier: 'product.repayMethod',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择还款方式'
                   }
               ]
           },repayDateType: {
               identifier: 'product.repayDateType',
               depends: 'repayMethod1',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择还款时间'
                   }
               ]
           },repayDateType: {
               identifier: 'product.repayDateType',
               depends: 'repayMethod5',
               rules: [
                   {
                       type: 'empty',
                       prompt: '请选择还款时间'
                   }
               ]
           },overdueDays: {
               identifier: 'product.overdueDays',
               rules: [
                   {
                       type: 'integer',
                       prompt: '请输入正确的逾期宽限天数'
                   }
               ]
           },minAmount: {
               identifier: 'product.minAmount',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的最低额度（最多两位小数）'
                   }
               ]
           },maxAmount: {
               identifier: 'product.maxAmount',
               rules: [
                   {
                       type: 'validateDecimal2',
                       prompt: '请输入正确的最高额度（最多两位小数）'
                   },{
                       type : 'greater[minAmount]',
                       prompt: '最高额度需要大于最低额度'
                   }

               ]
           },infoTmpId: {
               identifier: 'product.infoTmpId',
               rules: [
                   {
                       type   : 'empty',
                       prompt: '请选择产品详情模板'
                   }
               ]
           },mediaTmpId: {
               identifier: 'product.mediaTmpId',
               rules: [
                   {
                       type   : 'empty',
                       prompt: '请选择影像资料模板'
                   }
               ]
           },repayNotifyEarlyDays: {
               identifier: 'product.repayNotifyEarlyDays',
               rules: [
                   {
                       type   : 'integer',
                       prompt: '还款提前通知天数'
                   }
               ]
           }

       }
   }).api({
        url: "/product/add",
        serializeForm: true,
        method: "post",
        beforeSend:function (settings) {
            for(i in settings.data){
                if(i !='user.roles'){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
            }
            return settings;
        },
        onSuccess: function (data) {
            $.uiAlert({
              type: "success",
              textHead: '添加成功',
              text: data.msg,
              time: 1
            });
            window.location.href = "/product/edit_init?id=" + data.id;
        }, onFailure: function (data) {
            $.uiAlert({
              type: "danger",
              textHead: '添加失败',
              text: data.msg,
              time: 1
          });
        }
  });
}

$(document).ready(function () {

    //初始化form
    initAddForm();

    //关闭
    $("#closeBnt").on("click",function(){
        window.location.href = "/product/list";
    });

    //保存
    $("#saveBnt").on("click",function () {
        if ($("#updateProductId").val()=="") {
            //根据id查找数据
            add();
        }
    });

    $(".loanTermType_item").change(function () {
        changeInterestTypeCheckboxStatus($(this));
        if($(this).attr("id")==="loanTermType_date") {
            return;
        }
        if ($(this).is(":checked")) {
            $(this).parent(".checkbox").parent().find("input[type=text]").attr("disabled", false);
        } else {
            $(this).parent(".checkbox").parent().find("input[type=text]").val("");
            $(this).parent(".checkbox").parent().find("input[type=text]").attr("disabled", true);
        }
    });

    $(".loanTermType_item").change(function () {
        var month = "0";
        var day = "0";
        var season = "0";
        var date = "0";

        if ($("#loanTermType_day").is(":checked")) {
            day = "1";
        }

        if ($("#loanTermType_month").is(":checked")) {
            month = "1";
        }

        if ($("#loanTermType_season").is(":checked")) {
            season = "1";
        }

        if ($("#loanTermType_date").is(":checked")) {
            date = "1";
        }


        if ((day+month+date+season)==="0000") {
          $("#loanTermType").val("");
        } else {
          $("#loanTermType").val(day + month + date + season);
        }
        if (month==="0") {
            $("#repayMethod_1").attr("disabled", true);
            $("#repayMethod_3").attr("disabled", true);
            $("#repayMethod_1").prop("checked", false).change();
            $("#repayMethod_3").prop("checked", false).change();
        }

        if (day==="0" && date==="0") {
            $("#repayMethod_2").attr("disabled", true);
            // $("#repayMethod_4").attr("disabled", true);
            $("#repayMethod_2").prop("checked", false).change();
            // $("#repayMethod_4").prop("checked", false).change();
        }

        if (month==="1") {
            $("#repayMethod_1").attr("disabled", false);
            $("#repayMethod_3").attr("disabled", false);
        }

        if (day==="1" || date==="1") {
            $("#repayMethod_2").attr("disabled", false);
        }

        if(season === '0'){
            $("#repayMethod_5").attr("disabled", true);
            $("#repayMethod_5").prop("checked", false).change();
        }
        if(season ==='1'){
            $("#repayMethod_4").attr("disabled", true);
            $("#repayMethod_5").attr("disabled", false);
            $("#repayMethod_4").prop("checked", false).change();
        }
        if((season == '1' && day == '1')||(season == '1' && date == '1') ||(season == '1' && month == '1')){
            $("#repayMethod_4").attr("disabled", false);
        }

    });

    $("#repayMethod_1,#repayMethod_5").change(function () {
        if ($("#repayMethod_1").is(":checked") || $("#repayMethod_5").is(":checked")) {
            $(".repayDateType_item").attr("disabled", false);
            $(".repayDateType_item").prop("checked", false);
            $("#repayDateType").val("");
            $("#repayDateType").attr("disabled", false);
        } else {
            $(".repayDateType_item").prop("checked",false);
            $(".repayDateType_item").attr("disabled",true);
            $("#repayDateType").attr("disabled",true);
        }
    });

    $(".repayMethod_item").change(function () {
        var r_1 = "0";
        var r_2 = "0";
        var r_3 = "0";
        var r_4 = "0";
        var r_5 = "0";

        if ($("#repayMethod_1").is(":checked")) {
            r_1 = "1";
        }
        if ($("#repayMethod_2").is(":checked")) {
            r_2 = "1";
        }
        if ($("#repayMethod_3").is(":checked")) {
            r_3 = "1";
        }
        if ($("#repayMethod_4").is(":checked")) {
            r_4 = "1";
        }
        if ($("#repayMethod_5").is(":checked")) {
            r_5 = "1";
        }

        if (r_1+r_2+r_3+r_4+r_5==="00000"){
          $("#repayMethod").val("");
        } else {
          $("#repayMethod").val(r_1 + r_2 + r_3 + r_4 + r_5);
        }

        
    });

    $(".repayDateType_item").change(function () {
        var t_1 = "0";
        var t_2 = "0";

        if ($("#repayDateType_1").is(":checked")) {
            t_1 = "1";
        }
        if ($("#repayDateType_2").is(":checked")) {
            t_2 = "1";
        }
        if ((t_1+t_2)==="00") {
          $("#repayDateType").val("");
        } else {
          $("#repayDateType").val(t_1 + t_2);
        }
    });

    $(".interestType_item").change(function () {
        if ($(this).is(":checked")) {
            $(this).parent(".checkbox").parent().find("input[type=text]").attr("disabled", false);
        } else {
            $(this).parent(".checkbox").parent().find("input[type=text]").val("");
            $(this).parent(".checkbox").parent().find("input[type=text]").attr("disabled", true);
        }
    });

    $(".interestTypeCheckbox").change(function () {
        var i_1 = "0";
        var i_2 = "0";

        if ($("#interestType_1").is(":checked")) {
            i_1 = "1";
        }
        if ($("#interestType_2").is(":checked")) {
            i_2 = "1";
        }
        if ((i_1+i_2)==="00") {
          $("#interestType").val("");
        } else {
          $("#interestType").val(i_1 + i_2);
        }
    });

   $(".monthInterestTypeCheckbox").change(function () {
      var i_1 = "0";
      var i_2 = "0";

      if ($("#monthInterestType_1").is(":checked")) {
          i_1 = "1";
      }
      if ($("#monthInterestType_2").is(":checked")) {
          i_2 = "1";
      }

      if ((i_1+i_2)==="00") {
        $("#monthInterestType").val("");
      } else {
        $("#monthInterestType").val(i_1 + i_2);
      }
  });

  $(".dayInterestTypeCheckbox").change(function () {
      var i_1 = "0";
      var i_2 = "0";

      if ($("#dayInterestType_1").is(":checked")) {
          i_1 = "1";
      }
      if ($("#dayInterestType_2").is(":checked")) {
          i_2 = "1";
      }
      if ((i_1+i_2)==="00") {
        $("#dayInterestType").val("");
      } else {
        $("#dayInterestType").val(i_1 + i_2);
      }
  });


  $(".seasonInterestTypeCheckbox").change(function () {
      var i_1 = "0";
      var i_2 = "0";

      if($("#seasonInterestType_1").is(":checked")) {
         i_1 = "1";
      }
      if($("#seasonInterestType_2").is(":checked")) {
         i_2 = "1";
      }
      if((i_1+i_2)==="00") {
         $("#seasonInterestType").val("");
      }else {
        $("#seasonInterestType").val(i_1 + i_2);
      }
  });




    //选择产品查询
    // $("#typeId").change(function () {
    //     $("#typeCode").text($(this).find("option:selected").attr("data-code"));
    // });

    function changeInterestTypeCheckboxStatus(loanTermTypeCheckbox) {
      if (undefined == loanTermTypeCheckbox) {
        return;
      }
      if (loanTermTypeCheckbox.is(":checked")) {
        changeInterestTypeCheckboxEnable(loanTermTypeCheckbox.attr("id"));
      } else {
        changeInterestTypeCheckboxDisable(loanTermTypeCheckbox.attr("id"));
      }

     
    }

    function changeInterestTypeCheckboxDisable(loanTermType) {
        if (loanTermType==="loanTermType_day") {
          $(".dayInterestTypeCheckbox").attr("disabled", true).prop("checked", false).change();
          $("#dayInterestType").val("");
        }
        if (loanTermType==="loanTermType_month") {
          $(".monthInterestTypeCheckbox").attr("disabled", true).prop("checked", false).change();
          $("#monthInterestType").val("");   
        }

        if(loanTermType ==="loanTermType_season"){
            $(".seasonInterestTypeCheckbox").attr("disabled", true).prop("checked", false).change();
            $("#seasonInterestType").val("");
        }
        if (loanTermType==="loanTermType_date") {
          $(".interestTypeCheckbox").attr("disabled", true).prop("checked", false).change();
          $("#interestType").val("");
        }
    }

    function changeInterestTypeCheckboxEnable(loanTermType) {
        if (loanTermType==="loanTermType_day") {
          $(".dayInterestTypeCheckbox").attr("disabled", false);
        }
        if (loanTermType==="loanTermType_month") {
          $(".monthInterestTypeCheckbox").attr("disabled", false);
        }
        if(loanTermType ==='loanTermType_season'){
            $(".seasonInterestTypeCheckbox").attr("disabled", false);
        }
        if (loanTermType==="loanTermType_date") {
          $(".interestTypeCheckbox").attr("disabled", false);
        }
    }


});


