/**
 * Created by yangzb01 on 2017/4/13.
 */
/**
 * Created by yangzb01 on 2017/4/13.
 */


function send_request(dir) {
    /* 获取上传Token */
    var xmlhttp = null;
    if (window.XMLHttpRequest) {
        xmlhttp=new XMLHttpRequest();
    } else if (window.ActiveXObject){
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (xmlhttp!=null) {
        var tokenUrl = '/media_upload/fetchAliOssToken?dir=' + dir
        xmlhttp.open( "GET", tokenUrl, false );
        xmlhttp.send( null );
        return xmlhttp.responseText
    } else{
        alert("Your browser does not support XMLHTTP.");
    }
}

function set_upload_param(up, filename, dir) {
    var obj = JSON.parse(send_request(dir));
    var new_multipart_params = {
        'key' : obj['dir'] + filename,  // 文件重命名
        'policy': obj['policy'],
        'OSSAccessKeyId': obj['accessid'],
        'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
        'signature': obj['signature']
    }
    up.setOption({
        'url': obj['host'],
        'multipart_params': new_multipart_params
    });
}

var fileTempName='';
var Uploader = {
    /**
     * 公用参数
     */
    common_options: {
        runtimes : 'html5,flash,silverlight,html4',
        browse_button : 'selectfiles',
        flash_swf_url : 'lib/plupload-2.1.2/js/Moxie.swf',
        silverlight_xap_url : 'lib/plupload-2.1.2/js/Moxie.xap',
        url : 'http://oss.aliyuncs.com',
        filters: {
            max_file_size : '20480kb', //最大只能上传20m的文件
        }, resize:{
            quality: 50,
            preserve_headers: false
        },
        init: {
            PostInit: function() {
            },
            FilesAdded: function(up, files) {
                var lastStr = files[0].name.lastIndexOf('.');
                var t0 = files[0].name.substring(0,lastStr);
                var t1 = files[0].name.substring(lastStr+1,files[0].name.length);
                var filename = t0 + '_' + (new Date()).Format("yyyyMMddhhmmss") + '.' + t1;
                //$('input[name="contractFileName"]').val(filename);
                fileTempName=filename;
                var dir = $('.js-uploadBtn').data('code')+'/channel';
                set_upload_param(up, filename, dir)//暂时写死
                up.start();
            },
            UploadProgress: function(up, file) {
                $("#percent").html(file.percent+'%');
                //$('.js-fileName').html(file.percent+'%');
            },
            FileUploaded: function(up, file, info) {
                $("#percent").html('');
                var dir = $('.js-uploadBtn').data('code')+'/channel';
                //var filename = $('input[name="contractFileName"]').val();
                var url = settings.accessHost + dir+'/'+fileTempName;

                //添加文件显示
                $("#fileBox").append('<div class="fbox"><a target="_blank" href="'+url+'">'+fileTempName+'</a>&nbsp;<i class="ui red button mini clearFile" onclick="clearFile(this)">删除</i></div>')
                $('input[name="contract"]').val(fileTempName)
                /*$('.js-fileName').attr({
                    'target':'_blank',
                    'href':url
                }).html(filename);
                $('input[name="contractFileUrl"]').val(url);*/
            },
            Error: function(up, err) {
                $.uiAlert({textHead:'',type:'danger',text:'上传失败，'+err.message});
            }
        }
    },
    set: function(option) {
        var othis = this;
        return new plupload.Uploader($.extend({}, othis.common_options, option))
    }
};
function clearFile(obj) {
    if($("#fileBox").children().length==1){
        $('input[name="contract"]').val('');
    }
    $(obj).parent('.fbox').remove();
}