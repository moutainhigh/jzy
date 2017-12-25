/**
 * Created by yangzb01 on 2017-09-06.
 */
var templateType  = utils.getUrlParam("type");
var flowConfigureType = utils.getUrlParam('flowConfigureType');
var isApproval = utils.getUrlParam('isApproval');
var path = window.location.pathname;
var DATA = {orderId:'', taskId:'', type:'', code:''};
var enumsTitle = {
    EXTENSION:{'Y':'业务展期', 'F':'风控展期', 'C':'财务展期', 'G':'高管展期',},
    NORMAL:{'Y':'业务', 'F':'风控', 'C':'财务', 'G':'高管',}
};

function getBasicInfo(callBack){
    $(document).api({
        on: "now",
        method: 'get',
        action: "get base",
        data: {
            id: utils.getUrlParam("loanId")
        },
        onSuccess:function(data){
            var _data = data.data;
            DATA.code = _data.code;
            callBack();
        }
    })
}

/*
*
* */
var init = {
    /**
     * 获取审批节点信息
     * url:请求地址
     * data:请求参数
    * */
    getApprovalInfo:function(url,data,callback){
        $(document).api({
            on: "now",
            method: 'get',
            action: url,
            data:data,
            onSuccess: function (data) {
                var _data = data.data;
                var pointType = utils.getUrlParam("pointType");
                if(_data){
                    if(flowConfigureType =='EXTENSION' && pointType != _data.type){
                        getRiskMedia(pointType);
                    }
                }else{
                    getRiskMedia(pointType);
                }


                if(_data && pointType == _data.type){
                    DATA.taskId= _data.workItem.taskId;
                    DATA.orderId= _data.workItem.orderId;
                    DATA.type= _data.type;

                    if(flowConfigureType =='EXTENSION'){
                        getBasicInfo(function(){
                            getRiskMedia(pointType,_data)
                        });
                    }

                    if(flowConfigureType == 'EXTENSION'){
                        _data.type_val = function(){
                            return enumsTitle.EXTENSION[_data.type];
                        };
                    }else{
                        _data.type_val = function(){
                            return enumsTitle.NORMAL[_data.type]
                        };
                    }

                    _data.resultEnums = function(){
                        var r=[];
                        for(var x in enums.approval_code_desc){
                            r.push({
                                value:x,
                                name:enums.approval_code_desc[x]
                            })
                        }
                        return r;
                    };

                    if(isApproval =='true'){
                        var $applyTemplate = utils.render("#businessFormTemplate", _data);
                        $("#approvalTemplate").append($applyTemplate);
                    }

                    callback();
                }else{
                    $("#approvalTemplate").addClass('ks-hidden');
                }
            }
        });
    },

    clickAgree:function(obj){
        var obj = obj ? '#'+obj : '';
        $(''+obj+' input[name="approvalCode"]').click(function(){
            var val =  $('input[name="approvalCode"]:checked').val();
            DATA.approvalCode = val;
            var htm = val == 'AGREE' ? '同意':'';
            $(''+obj+' textarea[name="content"]').val(htm);
        });
    },

    /**
     * 提交审批
     * */
    submitFlow:function(dataCallBack){
        $("#approvalForm").form({
            fields: {
                content: {
                    identifier: 'content',
                    rules: [
                        {
                            type:'maxLength[500]',
                            prompt:'审批意见不超过500个字符'
                        }
                    ]
                },
                approvalCode: {
                    identifier: 'approvalCode',
                    rules: [
                        {
                            type: 'checked',
                            prompt: '请选择一个状态'
                        }
                    ]
                }
            },
            onSuccess: function (e, fidlds) {
                e.preventDefault();
            }
        }).api({
            action: "flow node approval",
            method: 'POST',
            serializeForm: true,
            beforeSend:function (settings) {
                if(dataCallBack(settings) == false){
                    return false;
                }else{
                    dataCallBack(settings);
                }
            },
            onSuccess: function (response) {
                $.uiAlert({
                    type: "success",
                    textHead:  '成功',
                    text: '成功',
                    time: 1,
                    onClosed: function () {
                        window.location.reload()
                    }
                });
            },
            onFailure: function (response, element) {
                $.uiAlert({
                    type: "danger",
                    textHead:  '失败',
                    text: response.msg,
                    time: 3,
                });
            }
        });
    },

    /**
    *  判断退回时，是否重走流程
    * */
    submitFlowApprove:function(obj){
        var  type = obj.find('input[name="approvalCode"]:checked').val();
        if ("BACKBEGIN" === type) {
            $("input[name='needRepeatFlowRadio'][value='true']").prop("checked", "checked");
            $('#needRepeatFlowModal').modal({
                closable  : true,
                onDeny    : function(){ return true;},
                onApprove : function() {
                    var radioValue = $("input[name='needRepeatFlowRadio']:checked").val();
                    obj.children("input[name='needRepeatFlow']").val(radioValue);
                    obj.submit();
                }
            }).modal('show');
        }else {
            obj.submit();
        }
    }
};

/*
* 审批 提交表单
* */
if(flowConfigureType == 'DECOMPRESSION'){
    //解压流程获取审批节点信息

    init.getApprovalInfo('query user approval',{
        'loanId':utils.getUrlParam('applyId'),
        'flowConfigureType':utils.getUrlParam('flowConfigureType')
    },function(){
        init.clickAgree('approvalForm');

        //解压审批
        init.submitFlow(function(settings){
            settings.data["loanId"] = utils.getUrlParam("applyId");
            settings.data["orderId"] = DATA.orderId;
            settings.data["taskId"] = DATA.taskId;
            settings.data["approvalType"] =  DATA.type;
            settings.data["flowConfigureType"] =  utils.getUrlParam("flowConfigureType");
            return settings;
        });
    });
}else {
    init.getApprovalInfo('query user approval',{
        'loanId':utils.getUrlParam('id'),
        'flowConfigureType':utils.getUrlParam('flowConfigureType')
    },function(){
        init.clickAgree('approvalForm');
        if(flowConfigureType == 'EXTENSION' && DATA.type =='F'){
            init.submitFlow(function(settings){
                settings.data["loanId"] =  utils.getUrlParam("id");
                settings.data["orderId"] =  DATA.orderId;
                settings.data["taskId"] =  DATA.taskId;
                settings.data["approvalType"] =  DATA.type;
                settings.data["flowConfigureType"] = utils.getUrlParam("flowConfigureType");
                if(DATA.riskMediaIsComplate || DATA.approvalCode !='AGREE'){
                    return settings
                }else{
                    $.uiAlert({
                        type: "danger",
                        textHead:  '失败',
                        text:"请确认资料全部已提交",
                        time: 3,
                    });
                    return false;
                }
            });
        }else{
            init.submitFlow(function(settings){
                settings.data["loanId"] =  utils.getUrlParam("id");
                settings.data["orderId"] =  DATA.orderId;
                settings.data["taskId"] =  DATA.taskId;
                settings.data["approvalType"] =  DATA.type;
                settings.data["flowConfigureType"] =  utils.getUrlParam("flowConfigureType");
                return settings;
            });
        }

    });
}


$(document).on('click','#businessFormBtn',function(){
    init.submitFlowApprove($('#approvalForm'));
});


function getRiskMedia(ponitType,_data){
    if((_data && _data.type =='Y') || ponitType =='Y') return false;

    var Data = _data;
    $(document).api({
        on: "now",
        method: 'get',
        action: "get extension risk media manifest",
        data: {
            loanId: utils.getUrlParam('loanId'),
            extensionId:utils.getUrlParam('id')
        },
        onSuccess: function (data) {
            if(Data){
                var _data = Data
                _data.media = data.data;
                _data.code =function(){
                    return DATA.code;
                };
            }else{
                var _data ={};
                _data.media = data.data;
            }
            _data.url = function(){
                if(this.productMediaAttachDetails && this.productMediaAttachDetails.length>0){
                    return true;
                }else{
                    return false;
                }
            };
            _data.isExtension = function(){
                return this.extensionId ? true:false;
            };

            var loanInfoList = _data.media.loanRiskInfo;
            if(_data.media.flag == true){
                _data.infoList = loanInfoList.splice(0,loanInfoList.length-1);
                _data.lastList = loanInfoList;
            }else{
                _data.infoList = loanInfoList;
            }

            DATA.riskMediaIsComplate = _data.media.flag;
            _data.id = utils.getUrlParam('id');
            if(Data && isApproval == 'true' && Data.type == 'F'){
                var $riskFormTemplate = utils.render("#riskFormTemplate", _data);
                $("#riskMediaTmp").prepend($riskFormTemplate);
                var n = $("#risk_info").find('.js-uploadBtn').length;
                upArryList();//创建数组
                for(var i=0;i < n ;i++){
                    $('tr.mediaItem:eq('+i+') .js-temp').each(function(k,ele){
                        mediaDetail[i].push({
                            url:$(ele).attr("href"),
                            attachName:$(ele).text()
                        })
                    });
                    var detail = JSON.stringify(mediaDetail[i]);
                    $('.mediaItem:eq('+i+')').find('input[name="mediaDetail"]').val(detail)
                }
            }else{
                if(DATA.riskMediaIsComplate == true){
                    var $riskFormTemplate = utils.render("#riskTemplate", _data);
                    $("#riskMediaTmp").prepend($riskFormTemplate);
                }
            }

            initRiskMediaForm();
        }
    });
}

/**
 * 展期风控资料
 * */
function initRiskMediaForm(){
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
    });

//点击按钮删除对应文件
    $(document).on('click','.js-remove',function(){
        var parentsDel = $(this).parents('.js-canBeDel');
        var k = $('tr.mediaItem').index($(this).parents('tr.mediaItem'));
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

    $("#riskMediaForm").form({
        fields: {
            content: {
                identifier: 'content',
                rules: [
                    {
                        type: 'empty',
                        prompt: '{name}不能为空'
                    },{
                        type:'maxLength[500]',
                        prompt:'授信方案不超过500个字符'
                    }
                ]
            }
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }).api({
        action: "update risk  media",
        method: 'POST',
        serializeForm: true,
        beforeSend: function (settings) {
            var loanId = utils.getUrlParam("loanId")
            settings.data["loanId"] = loanId;
            var medias = [];
            var errors = [];
            $("tr.mediaItem").each(function() {
                var $item = $(this);
                var required = $item.find("input[name='required']").val();
                var mediaInfo = $item.find("input[name='mediaDetail']").val();
                var productMediaAttachId = $item.find("input[name='productMediaAttachId']").val();
                if(required == "true" && (mediaInfo == "" || mediaInfo == "[]")) {
                    $item.addClass("error");
                    errors.push('<li>必填项必须上传</li>');
                } else {
                    $item.removeClass("error");
                }

                medias.push({
                    'loanId': loanId,
                    'itemName': $item.find("input[name='itemName']").val(),
                    'required': required,
                    'productMediaAttachDetails':mediaInfo,
                    'mediaItemType': $item.find("input[name='mediaItemType']").val(),
                    'id':productMediaAttachId
                })
            });
            settings.data["medias"] = JSON.stringify(medias);

            if(errors.length == 0) {
                return settings;
            } else {
                $("#riskMediaForm .ui.error.message ul").html(errors.join(""));
                $("#riskMediaForm").addClass("error");
                return false;
            }
        },
        onSuccess: function (response) {
            $.uiAlert({
                type: "success",
                textHead:  '成功',
                text: '成功' ,
                time: 1,
                onClosed: function () {
                    window.location.reload()
                }
            });

        },
        onFailure: function (response, element) {
            $.uiAlert({
                type: "danger",
                textHead:  '失败',
                text: response.msg,
                time: 3,
            });
        }
    })
}