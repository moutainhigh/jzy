/**
 * Created by yangzb01 on 2017-11-30.
 */
/**
 * Created by yangzb01 on 2017-11-29.
 */
var id = utils.getUrlParam('id');
var order_type= {
    CAR_LOAN:'CHEDAI_template',
    HOUSE_MORTGAGE_LOAN:'HONGBEN_template',
    BANK_HOUSE_LOAN:'SHULOU_template',
    PERSONAL_LOAN:'GERENDAI_template',
    FACTORING:'BAOLI_template',
    BILL:'PIAOJU_template',
    BANK_BILL:'YINPIAO_template'
};
var DATA ={
    itemType:'',
    loanPushId:'',
};

//期限渲染
function changeTermType(){
    var termType = $('select[name="termType"]');
    termType.val() =='FIXED_DATE'?$('input[name="term"]').attr('type','date'):$('input[name="term"]').attr('type','text')
    termType.on('change',function(){
        $(this).val() == 'FIXED_DATE'? $('input[name="term"]').attr('type','date'):$('input[name="term"]').attr('type','text')
    });
}

// list为content json数据 填充模板数据
function setProjectInfo(list){
    for(i in list){
        var _data = list[i];
        if(_data.type != 'list'){
            $('#loanTmpInfo').find('[name='+i+']').val(_data.value).attr('readonly',true);
        }else{
            for(K in _data.value){
                $('#loanTmpInfo').find('[name='+i+']').each(function(n,ele){
                    var ele = $(ele);
                    ele.attr('disabled',true);
                    if(ele.attr('data-key') == K){
                        ele.prop('checked',true)
                    }
                })
            }
        }
    }
}

//组装项目信息
function jsonContentData(){
    var data={
        audittype:{
            title:'',
            type:'list',
            value:{}
        }
    };
    $('#loanTmpInfo').find('.item').each(function(n,ele){
        var key = $(this).attr('name'),
            title = $(this).prev('label').html(),
            val = $(this).val();
        data[key] = {
            title:title,
            type:'text',
            value:val
        }
    });
    $('#loanTmpInfo').find("input[name='audittype']").each(function(n,ele){
        if($(this).prop('checked')){
            var key = $(this).attr('data-key');
            var val = $(this).next('label').html();
            data.audittype.value[key] = val
        }
    });
    return JSON.stringify(data);
}

var init = {
    /**
     * 获取票据列表 & 填充
     * */
    getBillList:function(billList){
        for(i in billList){
            var _data = billList[i];
            $('#billList').append('<option value='+_data.id+'>'+_data.billNo+'')
        }

        $('#billList').dropdown({
            allowAdditions: true,
        });

        var values = [];
        for(i in billList){
            var _data = billList[i];
            if(_data.pushOrderId){
                values.push(_data.id)
            }
        }

        $('#billList').dropdown('set selected',values)
        $('#billList').attr('disabled',false);
        $('#billRelation').removeClass('disabled ks-hidden');
    },
    getDetail:function () {
        $(document).api({
            on:'now',
            method:'post',
            action:'get order detail',
            data:{
                'id':id
            },
            onSuccess:function(data){
                DATA.itemType = data.order.itemType;
                var repayMethod = [];
                for(i in data.pushRepayMethods){
                    var flag = false;
                    if(data.order.repayMethod){
                        flag = i== data.order.repayMethod ? true:false
                    };
                    repayMethod.push({
                        key:i,
                        name:data.pushRepayMethods[i],
                        selected:flag
                    })
                }
                data.repayMethod = repayMethod;
                var $basicTmpInfo = utils.render('#basicTmpInfo',data);
                $('#info').html($basicTmpInfo);

                //初始化产品模板
                var $tmpInfo = utils.render('#'+order_type[DATA.itemType]+'','');
                $('#loanTmpInfo').html($tmpInfo);

                var list = JSON.parse(data.order.content);
                setProjectInfo(list);//填充产品模板信息

                if(data.billLoanPushList) init.getBillList(data.billLoanPushList);

                $('select[name="termType"]').val(data.order.termType);
                changeTermType();
                $('input[name="term"]').val(data.order.term);
                $('select[name="repayMethod"]').val(data.order.repayMethod);
            }

        })
    },
};



init.getDetail();

