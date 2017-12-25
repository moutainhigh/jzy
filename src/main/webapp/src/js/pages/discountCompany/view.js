/**
 * Created by zhangyy04 on 2017/6/15.
 */

var id = utils.getUrlParam('id');
$(document).api({
    on: "now",
    method: 'get',
    action: "view borrower",
    data: {
        id:id
    },
    onSuccess:function (data) {
        var _data = data.data;
        /*多文件*/
        if(_data.contractFileUrls){
            var files=JSON.parse(_data.contractFileUrls)
            var filesArr_a=files.a;
            var filesArr_b=files.b;
            if(filesArr_a){
                var filesLen_a=filesArr_a.length;
                if(filesLen_a>0){
                    for(var i=0;i<filesLen_a;i++){
                        $("#fileBox0").append('<div class="fbox"><a target="_blank" href="'+filesArr_a[i].dataValue+'">'+filesArr_a[i].keyName+'</a>&nbsp;</div>')
                    }
                }else{
                    $("#fileBox0").append('无数据')
                }
            }else{
                $("#fileBox0").append('无数据')
            }
            if(filesArr_b){
                var filesLen_b=filesArr_b.length;
                if(filesLen_b>0){
                    for(var i=0;i<filesLen_b;i++){
                        $("#fileBox1").append('<div class="fbox"><a target="_blank" href="'+filesArr_b[i].dataValue+'">'+filesArr_b[i].keyName+'</a>&nbsp;</div>')
                    }
                }else{
                    $("#fileBox1").append('无数据')
                }
            }else{
                $("#fileBox1").append('无数据')
            }
        }

        $("#form-borrower input").each(function(n,ele){
            var name = $(ele).attr('name');

            if(_data.status == 'ABLE'){
                $('input[name="status"]:eq(0)').attr('checked',true);
            }else{
                $('input[name="status"]:eq(1)').attr('checked',true);
            }
            $(ele).val(_data[name]);
        });
    }
});

