/**
 * Created by zhangyy04 on 2017/6/15.
 */

// 绑定上传事件
function bindUpload(){
    $(".js-uploadBtn").each(function(index) {;
        var uploader = Object.create(Uploader).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
            file_box:"fileBox"+index
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

$.fn.form.settings.rules.matchedCharacter = function(value){
    var reg = /^[\u4e00-\u9fa5a-zA-Z0-9\(\)\（\）]+$/g;
    return (reg.test(value) && value.length <= 50) || value == ''
}

$.fn.form.settings.rules.link_phone = function(value){
    var mobile = /^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$/;
    var fix_phone = /^[0-9]{3,5}\-[0-9]{3,8}$/;
    return mobile.test(value) || fix_phone.test(value) || value ==''
}


var validateForm = {
    inline: true,
    on: 'blur',
    fields :{
        name:{
            identifier: 'name',
            rules:[{
                type:'empty',
                prompt:'企业名称不能为空'
            },{
                type:'maxLength[50]',
                prompt:'输入不能超过50位字符'
            }]
        },
        certifNumber:{
            identifier:'certifNumber',
            rules:[{
                type:'empty',
                prompt:'证件号码不能为空'
            },{
                type: 'validateCharNum',
                prompt: '证件号码需为数字和英文'
            },{
                type:'validateLen[20]',
                prompt:'输入不能超过20位'
            }]
        },
        accountName:{
            identifier: 'accountName',
            rules:[{
                type: 'maxLength[50]',
                prompt: '输入不超过50位的字符'
            }]
        },
        account:{
            identifier: 'account',
            rules:[{
                type: 'bankCard',
                prompt: '账号格式不正确'
            }]
        },
        bankName:{
            identifier: 'bankName',
            rules:[{
                type: 'z_chRange[50]',
                prompt: '输入不超过50位的中文'
            }]
        },
        legalRepresentative: {
            identifier: 'legalRepresentative',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'matchedCharacter',
                prompt:'50位字符（只包含数字、中文、字母和括号）'
            }]
        },
        legalRepresentativePhone: {
            identifier: 'legalRepresentativePhone',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'link_phone',
                prompt:'请输入正确的联系方式'
            }]
        },
        linkman: {
            identifier: 'linkman',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'matchedCharacter',
                prompt:'50位字符（只包含数字、中文、字母和括号）'
            }]
        },
        linkmanPhone: {
            identifier: 'linkmanPhone',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'link_phone',
                prompt:'请输入正确的联系方式'
            }]
        },
        residence: {
            identifier: 'residence',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'maxLength[200]',
                prompt:'长度为200个字符'
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
function getFileUrls(id,flag) {
    var fileArr=[]
    $("#"+id).find('a').each(function(n,ele){
        var fileObj={
            keyName:$(ele).html(),
            dataValue:$(ele).attr('href')
        }
        fileArr.push(fileObj)
    })
    return '"'+flag+'":'+JSON.stringify(fileArr);
}
$('#btn-test').on('click',function(){
    var file0=getFileUrls('fileBox0',"a")
    var file1=getFileUrls('fileBox1',"b")
    var files ="{"+file0+","+file1+"}";//多文件上传
    console.log(files)
})

$('#btn-submit').on('click',function(){
    if($(this).html() == '保存'){
        $("#form-borrower").form(validateForm).api({
            method: 'post',
            action: "save borrower",
            beforeSend:function(settings){
                $('#btn-submit').html('保存中...');
                var $this = $("#form-borrower");
                settings.data["discountType"] =$this.find('select[name="discountType"]').val();
                settings.data["name"] =$this.find('input[name="name"]').val();
                settings.data["certifType"] =$this.find('select[name="certifType"]').val();
                settings.data["certifNumber"] =$this.find('input[name="certifNumber"]').val();
                //多文件上传
                var file0=getFileUrls('fileBox0',"a")
                var file1=getFileUrls('fileBox1',"b")
                settings.data["contractFileUrls"] ="{"+file0+","+file1+"}";

                settings.data["accountName"] =$this.find('input[name="accountName"]').val();
                settings.data["account"] =$this.find('input[name="account"]').val();
                settings.data["bankName"] =$this.find('input[name="bankName"]').val();

                settings.data["residence"] =$this.find('input[name="residence"]').val();
                settings.data["legalRepresentative"] =$this.find('input[name="legalRepresentative"]').val();
                settings.data["legalRepresentativePhone"] =$this.find('input[name="legalRepresentativePhone"]').val();
                settings.data["linkman"] =$this.find('input[name="linkman"]').val();
                settings.data["linkmanPhone"] =$this.find('input[name="linkmanPhone"]').val();
                if($('input[name="status"]:eq(0)').prop('checked')){
                    settings.data["status"] = 'ABLE'
                }else{
                    settings.data["status"] = 'DISABLED'
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
                        window.location.href = '/borrower/discountCompanyList';
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
        $("#form-borrower").submit();
    }else{
        return false;
    }
});