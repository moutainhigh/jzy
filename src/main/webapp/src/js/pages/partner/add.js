/**
 * Created by zhangyy04 on 2017/6/15.
 */

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

//获取渠道代码code
function getCode() {
    var code = (new Date()).Format("yyyyMMddhhmmss");
    $('.js-uploadBtn').attr('data-code',code);
}
getCode();

//校验
$.fn.form.settings.rules.isNull = function(value){
    if(value == '[]' || value ==''){
        return false;
    }else{
        return true;
    }
}

var validateForm = {
    inline:true,
    on:'blur',
    fields:{
        fullName:{
            identifier: 'fullName',
            rules:[{
                type:'empty',
                prompt:'全称不为空'
            },{
                type:'maxLength[30]',
                prompt:'不超过30位字符'
            }]
        },
        name:{
            identifier:'name',
            rules:[{
                type:'empty',
                prompt:'简称不为空'
            },{
                type:'maxLength[10]',
                prompt:'不超过10位字符'
            }]
        },
        manager:{
            identifier:'manager',
            rules:[{
                type:'empty',
                prompt:'业务员姓名不为空'
            },{
                type:'maxLength[30]',
                prompt:'不超过30位字符'
            }]
        },
        contactWay:{
            identifier:'contactWay',
            rules:[{
                type:'mobile',
                prompt:'电话格式不正确'
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

//对接人
$(".managerSearch").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['search b_user'] + '?search={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        description: 'code'
    },
    onSelect: function (result, response) {
        $(this).find('input[name="managerId"]').val(result.id)
    }
});

$('#btn-submit').on('click',function(){
    $("#form-channel").form(validateForm).api({
        method: 'post',
        action: "save channel",
        beforeSend:function(settings){
            $('#btn-submit').html('保存中...');
            var $this = $("#form-channel");
            settings.data["channel.channelType"] = 0;
            settings.data["channel.id"] = $this.find('input[name="id"]').val();
            settings.data["channel.fullName"] = $this.find('input[name="fullName"]').val();
            settings.data["channel.name"] = $this.find('input[name="name"]').val();
            settings.data["channel.manager"] = $this.find('input[name="manager"]').val();
            settings.data["channel.managerId"] = $this.find('input[name="managerId"]').val();
            //新增
            settings.data["channel.contactWay"] = $this.find('input[name="contactWay"]').val();//联系电话、联系方式
            var effectiveDate=$this.find('input[name="effectiveDate"]').val();
            settings.data["channel.effectiveDate"] =effectiveDate?moment(effectiveDate).format("YYYY-MM-DD"):'';//生效日期
            //多文件上传
            settings.data["channel.contractFileUrls"] =getFileUrls();
            //settings.data["channel.contractFileName"] = $this.find('input[name="contractFileName"]').val();
            //settings.data["channel.contractFileUrl"] = $this.find('input[name="contractFileUrl"]').val();
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
                    $('#btn-submit').html('保存');
                    window.location.href = '/channel/partnerList';
                }
            });
        },
        onFailure:function(data){
            $('#btn-submit').html('保存');
            $.uiAlert({
                type: "danger",
                textHead: '后台处理错误',
                text: data.msg,
                time: 1
            });
        }
    });
    $("#form-channel").submit();
});