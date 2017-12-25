/**
 * Created by yangzb01 on 2017-09-16.
 */

var validate = {
    inline:true,
    fields:{
        'loanId':{
            identifier:'loanId',
            rules:[{
                type:'empty',
                prompt:'项目说明不为空'
            }]
        }
    }
}

var init = {
    list: function () {
        dtTable = $("#listTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['noMortgage apply list'],
                "type": 'post',
                data: function (d) {
                    var data = {};
                    data.applyCode = $('#applyCode').val();
                    data.borrower = $('#borrower').val();
                    data.addressType = $('#addressType').val();
                    data.loanStatus = $('#loanStatus').val();
                    var _d = $.extend({}, {searchKeys: data}, {start: d.start, length: d.length, draw: d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'applyCode'},
                {data: 'addressType'},
                {data: 'businessSource'},
                {data: 'borrower'},
                {data: 'loanStatus'},
                {data: null},
                {data: null}
            ],
            columnDefs: [{
                targets:[3],
                render:function (data){
                    return data ? data:'--';
                }
            },{
                targets: 1,
                render: function (data, type, row, meta) {
                    return enums.addressType[data];
                }
            }, {
                targets: 4,
                className: 'right aligned',
                render: function (data, type, row, meta) {
                    return enums.approvalStatusTypeList[data];
                }
            },{
                targets: 5,
                render: function (data, type, row, meta) {
                    if("IN_EDIT"==data.loanStatus){
                       return "待提交";
                    }else {
                        return null != row.approveStatusDesc ? row.approveStatusDesc:'--';
                    }
                }
            }, {
                //   指定第最后一列
                targets: 6,
                className: "single line",
                render: function (data, type, row, meta) {
                    var generalBtn = '<a class="ui button mini basic" href="javascript:;" onclick="init.generalApprovalSheetById(\''+row.id+'\')"><i class="file pdf outline icon"></i>生成业务审批单</a>';
                    if (row.loanStatus == 'IN_EDIT') {
                        return  generalBtn+'<div class="ui button mini basic" onclick="init.edit_noMortgage(\'' + row.loanId + '\',\'' + row.id + '\')"><i class="edit icon"></i>编辑</div>' +
                                '<a class="ui basic mini button" href="javascript:;" data-id="'+row.id+'" onclick="init.apply_cancle($(this))"><i class="cancel icon"></i>取消</a>';
                    } else if (row.loanStatus == 'IN_APPROVAL' || row.loanStatus == 'CANCEL' || row.loanStatus == 'REJECT') {
                        return  generalBtn+'<a target="_blank" href="/house_noMortgage_apply/detail?id=' + row.loanId + '&applyId='+row.id+'&type=noMortgage&comeFrom=' + window.location.pathname + '" class="ui button mini basic"><i class="info circle icon"></i>查看</a>'
                    } else if (row.loanStatus == 'APPROVED') {
                        return  generalBtn+ '<a target="_blank" href="/house_noMortgage_apply/detail?id=' + row.loanId + '&applyId='+row.id+'&type=noMortgage&comeFrom=' + window.location.pathname+'" class="ui button mini basic"><i class="info circle icon"></i>查看</a>'
                                //'<a target="_blank" href="" class="ui button mini basic">单证下载</a>'

                    }
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });

    },

    search: function () {
        dtTable.ajax.reload();
    },

    reset:function(){
        $('#applyCode').val('');
        $('#borrower').val('');
        $('#addressType').val('');
        $('#loanStatus').val('');
    },


    add_noMortgage:function(){
        $('#add_noMortgage').modal('show');
        $('#titleName').html('新增解押');
        var $template = utils.render("#templateInfo",'');
        $('#templateInfoBox').html($template);
    },
    //取消
    apply_cancle:function (obj) {
    var id = obj.data("id");
    $.uiDialog("您确认要取消这笔业务吗？", {
        onApprove: function () {
            var $modal = $(this);
            $(document).api({
                on: "now",
                method: 'post',
                action: "cancle house_noMortgage",
                data: {
                    id: id,
                },
                onSuccess: function (data) {
                    $.uiAlert({
                        type: "success",
                        textHead: '取消成功',
                        text: '取消成功',
                        time: 1,
                        onClosed: function () {
                            dtTable.ajax.reload();
                        }
                    });
                },
                onFailure: function (data) {
                    $.uiAlert({
                        type: "danger",
                        textHead: '取消失败',
                        text: '取消失败',
                        time: 2
                    });
                }
            });
        }
    })
    },

    edit_noMortgage:function(loanId,id){
        $('#add_noMortgage').modal('show');
        $('#titleName').html('编辑解押');
        $(document).api({
            on:'now',
            method:'post',
            action:'query noMortgage detail',
            data:{
                loanId:loanId,
                applyId:id
            },
            onSuccess:function(data){
                var obj = $('#add_form select[name="addressType"]');
                var addressType = data.houseNoMortgageApply.addressType;
                if(data.houseNoMortgageApply.mortgageDate) $('#mortgageDate').val(moment(data.houseNoMortgageApply.mortgageDate).format('YYYY-MM-DD'));
                obj.val(addressType);
                for(i in data.houseInfoList){
                    if(data.houseInfoList[i].owerStr){
                        var ower = data.houseInfoList[i].owerStr;
                        data.houseInfoList[i].userList = JSON.parse(ower)
                    }
                }
                data.loan_amount = function(){
                    return accounting.formatNumber(this.loanAmount,2,",");
                };
                data.clear_date = function(){
                    if(this.clearDate){
                        return moment(this.clearDate).format('YYYY-MM-DD');
                    }else{
                        return '';
                    }
                };
                var $template = utils.render("#templateInfo",data);
                $('#templateInfoBox').html($template);

                init.changeName(addressType);
            },
            onFailure:function(data){
                var $template = utils.render("#templateInfo",'');
                $('#templateInfoBox').html($template);
                $.uiAlert({
                    type: "danger",
                    textHead: '数据获取失败',
                    text: data.msg,
                    time: 2
                });
            }
        })
    },

    apply_code:function(){
        var code = $('#businessCode').val();
        var applyId = $("input[name='applyId']").val();
        $(document).api({
            on:'now',
            method:'post',
            action:'noMortgage apply init',
            data:{
                businessCode:code,
                applyId:applyId
            },
            onSuccess:function(data){
                var _data = data;

                for(i in data.houseInfoList){
                    if(data.houseInfoList[i].owerStr){
                        var ower = data.houseInfoList[i].owerStr
                        data.houseInfoList[i].userList = JSON.parse(ower)
                    }
                }
                data.loan_amount = function(){
                    return accounting.formatNumber(this.loanAmount,2,",");
                };
                data.clear_date = function(){
                    if(this.clearDate){
                        return moment(this.clearDate).format('YYYY-MM-DD');
                    }else{
                        return '';
                    }
                };

                var $template = utils.render("#templateInfo",_data);
                $('#templateInfoBox').html($template);


                var value = $('#add_form select[name="addressType"]').val();
                init.changeName(value);
            },
            onFailure:function(data){
                var $template = utils.render("#templateInfo",'');
                $('#templateInfoBox').html($template);
                $.uiAlert({
                    type: "danger",
                    textHead: '申请失败',
                    text: data.msg,
                    time: 2
                });
            }

        })
    },


    /*
    * 提交或保存
    * */

    save:function(type){
        var txt = type == 'add'?'保存':'提交';
        var addressType = $('#add_form select[name="addressType"]').val();
        var mortgageDate = $('#add_form input[name="mortgageDate"]').val();
        var texts = addressType =='GZMORTGAGE'?'他项权证号':'抵押登记号';
        var flag = true;
        $('.js_warrantNumber').each(function(n,ele){
            if($(this).val() == ''){
                $.uiAlert({
                    type: "danger",
                    textHead: '保存失败',
                    text: texts+'不为空',
                    time: 2
                });
                flag = false;
                return
            }
        });

        if($('#add_form .js_house').length < 1){
            $.uiAlert({
                type: "danger",
                textHead: '保存失败',
                text: '房产信息不为空',
                time: 2
            });
            return false;
        }
        if(flag){
            $('#add_form').form(validate).api({
                action: 'save house_noMortgage',
                method: 'POST',
                serializeForm: true,
                data: {
                    "addressType":addressType,
                    "type":type,
                    "mortgageDate":mortgageDate
                },
                beforeSend:function(settings){
                    var houseListStr=[];
                    $('#add_form .js_house').each(function(n,ele){
                        houseListStr.push({
                            id:$(this).find('.js_id').val(),
                            warrantNumber:$(this).find('.js_warrantNumber').val()
                        })
                    });
                    settings.data['houseListStr'] = JSON.stringify(houseListStr)

                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess:function(data){
                    $('#add_noMortgage').modal('hide');
                    dtTable.ajax.reload();
                    $.uiAlert({
                        type: "success",
                        textHead: txt+'成功',
                        text: data.msg,
                        time: 2
                    });
                },
                onFailure:function(data){
                    $.uiAlert({
                        type: "danger",
                        textHead: txt+'失败',
                        text: data.msg,
                        time: 2
                    });
                }
            });
            $('#add_form').submit()
        }
    },

    changeName:function(value){
        var txt = value =='GZMORTGAGE'?'他项权证号：':'抵押登记号：';
        $('.js_numName').html(txt);
    },

    generalApprovalSheetById:function(id){
        window.open("/house_noMortgage_apply/approval_download?id="+id,'_blank');
    }


};

$(document).on('change','#add_form select[name="addressType"]',function(){
    var value = $(this).val();
    init.changeName(value);
});

init.list();