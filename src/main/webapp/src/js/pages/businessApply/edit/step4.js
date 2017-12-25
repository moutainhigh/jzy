function clearMedia($item){
    $item.find("input[name='url']").val("");
    $item.find("input[name='attachName']").val("");
}

var userType = $('#stepData').data('userType');
var comeFrom = utils.getUrlParam('comeFrom');

// 绑定上传事件
$(".js-uploadBtn").each(function(index) {;
    var uploader = Object.create(Uploader).set({
        //自己的单独参数
        browse_button: $('.js-uploadBtn')[index],
    });
    uploader.init();
});
// 点击上传按钮
$(document).on("click",".js-uploadBtn",function() {
    var $item = $(this).parents("tr");
    // clearMedia($item);
});


//点击按钮删除对应文件
$(document).on('click','.js-remove',function(){
    var parentsDel = $(this).parents('.js-canBeDel');
    var k = $(this).parents('tr').index();
    var n = parentsDel.index();
    $.each(mediaDetail[k],function (i,item) {
        if(n == i){
            mediaDetail[k].splice(n,1);
        }
    });
    var detail = mediaDetail[k] == '' ? '' : JSON.stringify(mediaDetail[k]);
    $(this).parents("tr").find("input[name='mediaDetail']").val(detail);
    parentsDel.remove();
});

// 请求更新Loan附件
function ajaxUpdateLoanBusinessMedia(callback) {
    $(document).api({
        on:"now",
        action: "update loan business media",
        method: "post",
        beforeSend: function(settings) {
            var _step = "";
            if($("#linkStep1").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            if($("#linkStep2").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            if($("#linkStep3").hasClass("completed")) {
                _step += "1"
            }else {
                _step += "0"
            }
            _step += "1"
            settings.data["step"] = _step;

            var loanId = $("#stepData").data("loan_id");
            var loanSubjectId = $('select[name="loanSubjectId"]').val();
            settings.data["id"] = loanId;
            settings.data["loanSubjectId"] = loanSubjectId;
            var medias = [];
            var errors = [];
            $("tr.mediaItem").each(function() {
                var $item = $(this);
                var required = $item.find("input[name='required']").val();
                var itemName = $item.find("input[name='itemName']").val();
                var mediaInfo = $item.find("input[name='mediaDetail']").val();
                var productMediaAttachId = $item.find("input[name='productMediaAttachId']").val();
                if(required == "true" && (mediaInfo == "" || mediaInfo == "[]") ) {
                    $item.addClass("error");
                    errors.push('<li>必填项必须上传</li>');
                } else {
                    $item.removeClass("error");
                }
                medias.push({
                    'loanId': loanId,
                    'itemName': $item.find("input[name='itemName']").val(),
                    'required': required,
                    'mediaItemType': $item.find("input[name='mediaItemType']").val(),
                    'productMediaAttachDetails':mediaInfo,
                    'id':productMediaAttachId
                })
            });
            settings.data["medias"] = JSON.stringify(medias);
            if(errors.length == 0) {
                return settings;
            } else {
                $(".ui.error.message ul").html(errors.join(""));
                $("#step4Form").addClass("error");
                return false;
            }
        },
        onSuccess: function (response) {
            callback();
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
        }
    });
}

//赋值mediaDetail
function renderMediaInfo() {
    upArryList();
    var n = $('.js-uploadBtn').length;
    for(var i=0;i<n;i++){
        $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
            mediaDetail[i].push({
                url:$(ele).attr("href"),
                attachName:$(ele).text()
            })
        });
        var detail = JSON.stringify(mediaDetail[i]);
        $('.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail)
    }
};
renderMediaInfo();

// 提交表单
function ajaxSubmitLoan() {
    var loanSubjectId = $('select[name="loanSubjectId"]').val();
    if(loanSubjectId == ""){
        $.uiAlert({type: "danger", textHead: '保存失败', text: "未选择放款主体", time: 3});
        return false;
    }
    $(document).api({
        on:"now",
        action: "submit loan",
        method: "post",
        beforeSend: function(settings) {
            settings.data["id"] = $("#stepData").data("loan_id");
            settings.data["loanSubjectId"] = $('select[name="loanSubjectId"]').val();
            return settings;
        },
        onSuccess: function (response) {
            window.location.href = comeFrom;
        },
        onFailure: function (response) {
            $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
        }
    });
}

// 点击保存按钮
$("#btn-submit-step4").click(function() {
    var loanSubjectId = $('select[name="loanSubjectId"]').val();
    if(loanSubjectId == ""){
        $.uiAlert({type: "danger", textHead: '保存失败', text: "未选择放款主体", time: 3});
        return false;
    }
    ajaxUpdateLoanBusinessMedia(function() {
        window.location.href = comeFrom;
    });
});

//点击提交按钮
$("#btn-submit-step4-commit").click(function() {
    if(!$("#linkStep2").hasClass("completed") || !$("#linkStep3").hasClass("completed")) {
        $.uiAlert({type: "danger", textHead: '提交失败', text: "资料未填写完全", time: 3});
        return false;
    }
    var loanSubjectId = $('select[name="loanSubjectId"]').val();
    if(loanSubjectId == ""){
        $.uiAlert({type: "danger", textHead: '保存失败', text: "未选择放款主体", time: 3});
        return false;
    }
    ajaxUpdateLoanBusinessMedia(function() {
        ajaxSubmitLoan();
    });
});