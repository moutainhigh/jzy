/**
 * Created by yangzb01 on 2017-11-28.
 */


$(document).api({
    on:'now',
    method:'post',
    action:'order file list',
    data:{
        loanId:utils.getUrlParam('loanId')
    },
    onSuccess:function(data){
        for(i in data.attachList){
            var files =[];
            var _data = data.attachList[i];
            if(_data.attachNames && _data.attachNames.indexOf(',') > -1){
                var file = _data.attachNames.split(',');
                for(k in file){
                    files.push({
                        fileUrl:_data.urls.split(',')[k],
                        fileName:file[k]
                    })
                }
            }
            _data.files = files;
        }
        console.log(data);

        data.hasFile = function(){
            return this.urls ? true:false;
        };

        var $htm = utils.render('#fileTmp',data);
        $('#fileList').html($htm);
    }
});
