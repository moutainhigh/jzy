/**
 * Created by yangzb01 on 2017/4/14.
 */
// 业务员查询（渠道经理）
$("#managerSearch").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['search b_user'] + '?search={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (data) {
        $('#form-addChannel').find('input[name="managerId"]').val(data.id);
    }
});


// 绑定上传事件
function bindUpload(){
    $(".js-uploadBtn").each(function(index) {;
        var uploader = Object.create(Uploader).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
        });
        uploader.init();
    });
}
bindUpload();

//清除文件
$('#clearFile').on('click',function(){
    var $this = $('#form-addChannel');
    $('.js-fileName').html("");
    $this.find('input[name="contractFileName"]').val("");
    $this.find('input[name="contractFileUrl"]').val("");
});


//授信额度关联可用额度
$('input[name="depositLimit"]').on('input propertychange', function () {
    var tVal=Number($(this).val());
    if(tVal || tVal==0){
        $('input[name="usedAmount"]').val(tVal);
    }else{
        $('input[name="usedAmount"]').val('');
        return false;
    }
})

$("#effectiveDate").val(moment().format("YYYY-MM-DD"));


//获取渠道代码code
$(document).api({
    on: "now",
    method: 'post',
    action: "get channel code",
    onSuccess: function (data) {
        $('#channelCode').html(data.data);
        $('#form-addChannel').find('input[name="code"]').val(data.data);
        $('.js-uploadBtn').attr('data-code',data.data);
    },
    onFailure:function(data){
        $.uiAlert({
            type: "danger",
            textHead: '后台处理错误',
            text: data.msg,
            time: 1
        });
    }
});

//渲染产品类型
initData('productTree');


//校验
$.fn.form.settings.rules.isNull = function(value){
    if(value == '[]' || value ==''){
        return false;
    }else{
        return true;
    }
}

/*$("#effectiveDate").dateRangePicker({separator: '~'});*/

var channelForm = {
    inline: true,
    on: 'blur',
    fields :{
        fullName:{
            identifier: 'fullName',
            rules:[{
                type:'empty',
                prompt:'渠道全称不为空'
            },{
                type:'maxLength[30]',
                prompt:'不超过30位字符'
            }]
        },
        name:{
            identifier:'name',
            rules:[{
                type:'empty',
                prompt:'渠道简称不为空'
            },{
                type:'maxLength[10]',
                prompt:'不超过10位字符'
            }]
        },
        managerId:{
            identifier:'managerId',
            rules:[{
                type:'empty',
                prompt:'请选择渠道经理'
            }]
        },
        cooperationAmount:{
            identifier:'cooperationAmount',
            rules:[{
                type:'empty',
                prompt:'授信额度不为空'
            },{
                type:'canBeDecimal[2]',
                prompt:'金额为2位小数'
            },{
                type:'validateNumFloat[1-999999999]',
                prompt:'金额范围1-999999999'
            }]
        },
        depositLimit:{
            identifier:'depositLimit',
            rules:[{
                type:'empty',
                prompt:'保证金杠杆额度不为空'
            },{
                type:'canBeDecimal[2]',
                prompt:'金额为2位小数'
            },{
                type:'validateNumFloat[1-999999999]',
                prompt:'金额范围1-999999999'
            }]
        },
        residualAmount:{
            identifier:'residualAmount',
            rules:[{
                type:'empty',
                prompt:'可用额度不为空'
            },{
                type:'canBeDecimal[2]',
                prompt:'金额为2位小数'
            },{
                type:'validateNumFloat[0-999999999]',
                prompt:'金额范围0-999999999'
            }]
        },
        interestRate:{
            identifier:'interestRate',
            rules:[{
                type:'canBeDecimal[9]',
                prompt:'利率为9位小数'
            }]
        },
        effectiveDate:{
            identifier:'effectiveDate',
            rules:[{
                type:'empty',
                prompt:'生效日期不为空'
            }]
        },
        cooperationProductType:{
            identifier:'cooperationProductType',
            rules:[{
                type:'isNull',
                prompt:'合作产品类型未关联'
            }]
        },
        contract:{
            identifier:'contract',
            rules:[{
                type:'empty',
                prompt:'合作协议不为空'
            }]
        },
        contactWay:{
            identifier: 'contactWay',
            rules:[{
                type: 'mobile',
                prompt: '联系方式格式不正确'
            }]
        },
        riskControlMeasure:{
            identifier:'riskControlMeasure',
            rules:[{
                type:'empty',
                prompt:'风控措施不为空'
            },{
                type:'maxLength[300]',
                prompt:'风控措施300个字符'
            }]
        },
        status:{
            identifier:'status',
            rules:[{
                type:'checked',
                prompt:'请选择一个状态'
            }]
        }
    }
}

/*多文件上传*/
function getFileUrls() {
    var fileArr=[]
    $("#fileBox").find('a').each(function(n,ele){
        var fileObj={
            keyName:$(ele).html(),
            dataValue:$(ele).attr('href')
        }
        fileArr.push(fileObj)
    })
    return JSON.stringify(fileArr);
}

$('#btn-addChannel').on('click',function(){
    if($(this).html() == '保存'){
        $("#form-addChannel").form(channelForm).api({
            method: 'post',
            action: "save channel",
            beforeSend:function(settings){
                $('#btn-addChannel').html('保存中...');
                var $this = $("#form-addChannel");
                settings.data["channel.code"] =$this.find('input[name="code"]').val();
                settings.data["channel.channelType"] = '1';
                settings.data["channel.fullName"] =$this.find('input[name="fullName"]').val();
                settings.data["channel.name"] =$this.find('input[name="name"]').val();
                settings.data["channel.manager"] =$this.find('input[name="manager"]').val();
                settings.data["channel.managerId"] =$this.find('input[name="managerId"]').val();
                settings.data["channel.cooperationAmount"] =$this.find('input[name="cooperationAmount"]').val();
                settings.data["channel.guaranteeAmount"] =$this.find('input[name="guaranteeAmount"]').val();
                settings.data["channel.cooperationProductType"] =$this.find('input[name="cooperationProductType"]').val();
                settings.data["channel.buttMan"] =$this.find('input[name="buttMan"]').val();
                settings.data["channel.contactWay"] =$this.find('input[name="contactWay"]').val();
                settings.data["channel.riskControlMeasure"] =$this.find('textarea[name="riskControlMeasure"]').val();
                settings.data["channel.depositLimit"] = $this.find('input[name="depositLimit"]').val();
                //多文件上传
                settings.data["channel.contractFileUrls"] =getFileUrls();

                /*settings.data["channel.contractFileName"] =$this.find('input[name="contractFileName"]').val();
                settings.data["channel.contractFileUrl"] =$this.find('input[name="contractFileUrl"]').val();*/
                /*新加2017.06.13*/
                settings.data["channel.residualAmount"] =$this.find('input[name="residualAmount"]').val();//可用额度
                settings.data["channel.interestRate"] =$this.find('input[name="interestRate"]').val();//利息
                var effectiveDate=$this.find('input[name="effectiveDate"]').val();
                settings.data["channel.effectiveDate"] =effectiveDate?moment(effectiveDate).format("YYYY-MM-DD"):'';//生效日期

                if($('input[name="status"]:eq(0)').prop('checked')){
                    settings.data["channel.status"] = 'ABLE'
                }else{
                    settings.data["channel.status"] = 'DISABLED'
                }

                for(i in settings.data){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
                return settings;
            },
            onSuccess:function(data){
                $.uiAlert({
                    type: "success",
                    textHead: '添加成功',
                    text: data.msg,
                    time: 1,
                    onClosed:function(){
                        $('#btn-addChannel').html('保存');
                        window.location.href = '/channel/list';
                    }
                });
            },
            onFailure:function(data){
                $('#btn-addChannel').html('保存');
                $.uiAlert({
                    type: "danger",
                    textHead: '后台处理错误',
                    text: data.msg,
                    time: 1
                });
            }
        });
        $("#form-addChannel").submit();
    }else{
        return false;
    }
});