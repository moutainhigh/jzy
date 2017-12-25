/**
 * Created by sunwanchao on 2017/3/3.
 */
var ProductTempType = {
    productTempType : $("#productTempType").val(),
    piaoju : "PIAOJU",
    yinpiao:"YINPIAO",
    isBill : function(){
        return ProductTempType.productTempType==ProductTempType.piaoju || ProductTempType.productTempType==ProductTempType.yinpiao;
    }
}