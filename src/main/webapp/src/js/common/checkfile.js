/**
 * Created by yangzb01 on 2017/3/28.
 */

//赎楼&&红本&&个人&&车贷
$(document).on('click','#divStep4 .js-temp',function(){
    data = [];
    renderTmp('step4Form');
    var curIndex = $('#divStep4 .js-temp').index($(this));
    showFileTmp(curIndex);
    $('#source_file').attr('data-form','step4Form');
});

//票据银票增加
$(document).on('click','#billMedia .js-temp',function(){
    data = [];
    renderTmp('billMedia');
    var curIndex = $('#billMedia .js-temp').index($(this));
    showFileTmp(curIndex);
    $('#source_file').attr('data-form','billMedia');
});

//业务票据-审批查看
$(document).on('click','#businessApply .js-temp',function(){
    data = [];
    renderTmp('businessApply');
    var curIndex = $('#businessApply .js-temp').index($(this));
    showFileTmp(curIndex);
    $('#source_file').attr('data-form','businessApply');
});

//风控-审批查看
$(document).on('click','#riskControl .js-temp',function(){
    data = [];
    renderTmp('riskControl');
    var curIndex = $('#riskControl .js-temp').index($(this));
    showFileTmp(curIndex);
    $('#source_file').attr('data-form','riskControl');
});

//风控-审批查看
$(document).on('click','#riskMediaTmp .js-temp',function(){
    data = [];
    renderTmp('riskMediaTmp');
    var curIndex = $('#riskMediaTmp .js-temp').index($(this));
    showFileTmp(curIndex);
    $('#source_file').attr('data-form','riskMediaTmp');
});


function renderTmp(obj){
    $('#'+obj+' .js-temp').each(function(i,ele){
        var href = $(ele).attr('href');
        var fileType = href.split('.')[href.split('.').length-1].toLowerCase();
        if(fileType =='png' || fileType =='jpg' || fileType =='gif' || fileType =='jpeg' || fileType =='bmp' || fileType == 'svg'){
            var isImage = 'true';
        }else{
            var isImage = 'false';
        }
        data.push({
            href:$(ele).attr('href'),
            name:$(ele).data('name'),
            fileType:fileType,
            isImage:isImage
        })
    });
}

function showFileTmp(index){
    data.data_List = data;
    data.is_Image = function(){
        if(this.isImage =='true'){
            return true;
        }
    };
    var $applyFileTmp = utils.render("#applyFileTmp",data);
    $('#checkFile').html($applyFileTmp);

    $('.js-side:eq('+index+')').addClass('active');
    $('#checkFile').modal({
        blurring: true,
        onShow:function(){
            calculate();
        }
    }).modal('show');
}

$(document).on('click','#prev',function(){
    var curIndex = $('.js-side.active').index('.sides .js-side');
    var lastIndex = $('.sides .js-side').length-1 ;
    if(curIndex == 0){
        $('.sides .js-side.active').removeClass('active');
        $('.sides .js-side:eq('+lastIndex+')').addClass('active');
    }else{
        $('.sides .js-side.active').removeClass('active').prev().addClass('active');
    }
    calculate()
});
$(document).on('click','#next',function(){
    var curIndex = $('.js-side.active').index('.sides .js-side');
    if(curIndex == $('.sides .js-side').length-1){
        $('.sides .js-side.active').removeClass('active');
        $('.sides .js-side:eq(0)').addClass('active');
    }else{
        $('.sides .js-side.active').removeClass('active').next().addClass('active');
    }
    calculate()
});

function calculate(){
    $('body,#checkFile').addClass('scrolling');
}





