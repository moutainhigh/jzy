/**
 * Created by zhangyy04 on 2017/6/15.
 */

/*编辑获取默认值*/
var id = utils.getUrlParam('id');
function initData() {
    $(document).api({
        on: "now",
        method: 'post',
        action: "get channel detail",
        data: {
            id: id,
            channelType:'0'
        },
        onSuccess:function(data){
            var _data = data.data;
            if(_data["effectiveDate"]){
                $('input[name="effectiveDate"]').val(moment(_data["effectiveDate"]).format("YYYY-MM-DD"));//生效时间
            }
            /*多文件*/
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

            $("#form-channel input").each(function(n,ele){
                var name = $(ele).attr('name');
                if(_data.status == 'ABLE'){
                    $('input[name="status"]:eq(0)').attr('checked',true);
                }else{
                    $('input[name="status"]:eq(1)').attr('checked',true);
                }
                if(name!='effectiveDate'){
                    for(i in _data){
                        if(name == i){
                            $(ele).val(_data[i]);
                        }
                    }
                }
            });
            //bindUpload();
        }
    })
}
initData();