/**
 * Created by yangzb01 on 2017-06-15.
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

var Uploader_oneFile = {
    /**
     * 公用参数
     */
    common_options: {
        runtimes : 'html5,flash,silverlight,html4',
        browse_button : 'selectfiles',
        flash_swf_url : 'lib/plupload-2.1.2/js/Moxie.swf',
        silverlight_xap_url : 'lib/plupload-2.1.2/js/Moxie.xap',
        multi_selection:false, //不允许选择多个
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
                isMore = $('.js-uploadBtn').data('file');
                var lastStr = files[0].name.lastIndexOf('.');
                var t0 = files[0].name.substring(0,lastStr);
                var t1 = files[0].name.substring(lastStr+1,files[0].name.length);
                var filename = t0 + '_' + (new Date()).Format("yyyyMMddhhmmss") + '.' + t1;
                var fileCatalog = $('.js-uploadBtn').data('catalog');
                $('input[name="contractFileName"]').val(filename);
                var dir = $('.js-uploadBtn').data('code')+'/'+fileCatalog;
                set_upload_param(up, filename, dir)//暂时写死

                $('.js-uploadBtn').addClass('loading');
                $('.js-uploadBtn').siblings().find('input').attr('type','hidden');
                up.start();
            },
            UploadProgress: function(up, file) {
                if(isMore !='more') {
                    $('.js-fileName').html(file.percent + '%');
                }
            },

            FileUploaded: function(up, file, info) {
                var fileCatalog = $('.js-uploadBtn').data('catalog');
                var dir = $('.js-uploadBtn').data('code')+'/'+fileCatalog;
                var filename = $('input[name="contractFileName"]').val();
                var url = settings.accessHost + dir+'/'+filename;

                if(isMore =='more'){
                    var htm;
                    htm = '<div class="js_file mb_5">' +
                        '<a href="'+url+'" target="_blank" class="js-fileName">'+filename+'</a> &emsp;' +
                        '<span class="ui red button mini js_clear">删除</span>' +
                        '</div>';
                    $('.js_fileBox').append(htm)
                }else{
                    $('.js-fileName').attr({
                        'target':'_blank',
                        'href':url
                    }).html(filename);
                    $('input[name="contractFileUrl"]').val(url);
                }
                $('.js-uploadBtn').removeClass('loading');
                $('.js-uploadBtn').siblings().find('input').attr('type','file');
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