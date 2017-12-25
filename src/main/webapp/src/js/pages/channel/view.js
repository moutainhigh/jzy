/**
 * Created by yangzb01 on 2017/4/17.
 */

var id = utils.getUrlParam('id');
//初始化数据
var $depositLimitAmount,$residualAmount
$(document).api({
    on: "now",
    method: 'post',
    action: "get channel detail",
    data: {
        id:id
    },
    onSuccess:function (data) {
        var _data = data.data;
        /*已用额度计算*/
        $depositLimitAmount=Number(_data["depositLimit"]);
        $residualAmount=Number(_data["residualAmount"]);
        if(($depositLimitAmount || $depositLimitAmount==0) && ($residualAmount || $residualAmount==0)){
            var tVal=sub($depositLimitAmount,$residualAmount);
            $('input[name="usedAmount"]').val(accounting.formatMoney(tVal,'',2,'','.'));
        }

        if(_data["effectiveDate"]){
            $('input[name="effectiveDate"]').val(moment(_data["effectiveDate"]).format("YYYY-MM-DD"));//生效时间
        }

        /*多文件*/
        $('#channelCode').html(_data.code);
        $('.js-uploadBtn').attr('data-code',_data.code);
        if(_data.contractFileUrls){
            var filesArr=JSON.parse(_data.contractFileUrls)
            var filesLen=filesArr.length;
            if(filesLen>0){
                for(var i=0;i<filesLen;i++){
                    $("#fileBox").append('<a target="_blank" href="'+filesArr[i].dataValue+'">'+filesArr[i].keyName+'</a>&nbsp;')
                }
            }
        }else if(_data.contractFileName){
            $("#fileBox").append('<a target="_blank" href="'+_data.contractFileUrl+'">'+_data.contractFileName+'</a>&nbsp;')
        }

        $("#form-addChannel .field .item").each(function(n,ele){
            var name = $(ele).attr('name');
           /* $('#channelCode').html(_data.code);
            $('.js-uploadBtn').attr('data-code',_data.code);
            $('.js-fileName').attr('href',_data.contractFileUrl).html(_data.contractFileName);*/
            if(_data.status == 'ABLE'){
                $('input[name="status"]:eq(0)').attr('checked',true);
            }else{
                $('input[name="status"]:eq(1)').attr('checked',true);
            }
            if(name =='cooperationAmount' || name == 'guaranteeAmount' || name == 'residualAmount'){
                $(ele).val(accounting.formatMoney(_data[name],'',2,',','.'))
            }else if(name == 'interestRate'){
                $(ele).val(accounting.formatMoney(_data[name],'',9,',','.'))
            }else{
                if(name=='usedAmount' || name=='effectiveDate') {

                }else{
                    $(ele).val(_data[name]);
                }
            }

        });
        initData('productTreeView',id);
    }
});

function subtr(arg1,arg2){
    var r1,r2,m,n;
    try{r1=arg1.toString().split(".")[1].length}catch(e){r1=0}
    try{r2=arg2.toString().split(".")[1].length}catch(e){r2=0}
    m=Math.pow(10,Math.max(r1,r2));
    //last modify by deeka
    //动态控制精度长度
    n=(r1>=r2)?r1:r2;
    return ((arg1*m-arg2*m)/m).toFixed(n);
}
