/**
 * Created by yangzb01 on 2017-09-05.
 */


$.fn.form.settings.rules.phoneNum = function(value){
    var reg = /^[0-9]+([0-9]|\-)[0-9]+$/;
    return reg.test(value) || value == '';
};

$.fn.form.settings.rules.isNull = function(value){
    var x_len = $('.js_fileBox .js_file').length;
    return x_len > 0;
};

var init = {
    list:function(){
        dtTable1 =  $("#applyTale").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['intermediary apply list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.loanStatus = 'SAVE';
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'businessCode'},
                {data: 'applyCode'},
                {data: 'productName'},
                {data: 'borrower'},
                {data: 'submitDate'},
                // {data: 'loanTime'},
                {data: 'name'},
                {data: 'intermediaryFee'},
                {data: null}
            ],
            columnDefs: [{
                targets:4,
                render: function (data, type, row, meta) {
                    if(data){
                        return moment(data).format("YYYY-MM-DD");
                    }else {
                        return '--';
                    }

                }
            },{
                targets:6,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return accounting.formatMoney(data,'',2,',','.');
                }
            },{
                //   指定第最后一列
                targets: 7,
                className:"single line",
                render: function (data, type, row, meta) {
                    return  '<div class="ui button mini basic" onclick="init.showModal(\''+data.loanId+'\',\''+data.id+'\')">补录</div>' +
                            '<a target="_blank" class="ui button mini basic" href="/bill_intermediary_apply/detail?id='+row.loanId+'&intermediaryApplyId='+row.id+'&type=intermediary&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });


        dtTable2 = $('#appliedTale').DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['intermediary apply list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.loanStatus = 'SUBMIT';
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'businessCode'},
                {data: 'applyCode'},
                {data: 'productName'},
                {data: 'borrower'},
                {data: 'submitDate'},
                {data: 'loanTime'},
                {data: 'name'},
                {data: 'intermediaryFee'},
                {data: 'approveStatusDesc'},
                {data: null}
            ],
            columnDefs: [{
                targets:[4,5],
                render: function (data, type, row, meta) {
                    if(data){
                        return moment(data).format("YYYY-MM-DD");
                    }else {
                        return '--';
                    }
                }
            },{
                targets:7,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return accounting.formatMoney(data,'',2,',','.');
                }
            },{
                targets:8,
                render: function (data, type, row, meta) {
                    if(data){
                        return row.approveStatusDesc;
                    }
                    return '等待审批';
                }
            },{
                //   指定第最后一列
                className:"single line",
                targets: 9,
                render: function (data, type, row, meta) {
                    return  '<a target="_blank" class="ui button mini basic" href="/bill_intermediary_apply/detail?id='+row.loanId+'&intermediaryApplyId='+row.id+'&type=intermediary&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'+
                            '<a target="_blank" class="ui button mini basic" href="/flow/to_approval_list?id='+row.loanId+'&flowConfigureType=BROKERAGE_FEE&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>流程查看</a>';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        })

    },

    search:function(){
        var status = $('#tab_menu .item.active').attr('data-status');
        if(status == 'apply'){
            dtTable1.ajax.reload();
        }else{
            dtTable2.ajax.reload();
        }
    },

    reset:function(){
        $('#businessCode').val('');
        $('#submitDate').val('');
        $('#loanTime').val('');
    },
    showModal:function(loanId,id){
        $('#intermediaryInfo').modal('show');
        $('#intermediaryForm')[0].reset();
        $(document).api({
            on:'now',
            method:'post',
            action:'get intermediary info',
            data:{
                loanId:loanId
            },
            onSuccess:function(data){
                $('.js_fileBox').html('');
                $('#intermediaryForm .item').each(function(){
                    $(this).attr('readonly',false);
                });
                $('#intermediaryForm .item').each(function(i,ele){
                    var name = $(ele).attr('name');
                    if(data.intermediary[name]){
                        $(ele).attr('readonly',true);
                    }
                    $(ele).val(data.intermediaryApply[name]);
                });
                var withHoldingTaxFee = $('#intermediaryForm').find('input[name="withHoldingTaxFee"]');
                var afterTaxIntermediaryFee = $('#intermediaryForm').find('input[name="afterTaxIntermediaryFee"]');
                withHoldingTaxFee.val(data.billLoan['withHoldingTaxFee']);
                withHoldingTaxFee.attr('readonly',true);
                afterTaxIntermediaryFee.val(data.billLoan['afterTaxIntermediaryFee']);
                afterTaxIntermediaryFee.attr('readonly',true);
                $('#intermediaryForm').find('input[name="id"]').val(data.intermediary['id']);
                $('#intermediaryForm').find('input[name="intermediaryApplyId"]').val(id);
                $('#intermediaryForm input[name="loanId"]').val(data.intermediaryApply.loanId);
                $('#intermediaryFee').val(data.intermediaryApply.intermediaryFee);
                $('#intermediaryFee').attr('readonly',true);
                if(data.intermediaryApply.serviceContractFileUrls){
                    var list = JSON.parse(data.intermediaryApply.serviceContractFileUrls);
                    for(i in list){
                        $('.js_fileBox').append('<div class="js_file mb_5">' +
                            '<a href="'+list[i].url+'" target="_blank" class="js-fileName">'+list[i].name+'</a>  ' +
                            '<span class="ui red button mini js_clear">删除</span>' +
                            '</div>')
                    }
                }

            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '获取补录信息失败',
                    text: data.msg,
                    time: 2,
                })
            }

        })

    },

    validataForm:{
        inline: true,
        fields:{
            phone:{
                identifier:'phone',
                rules:[{
                    type: 'empty',
                    prompt: '电话不为空'
                },{
                    type:'phoneNum',
                    prompt:'电话号格式不对'
                },{
                    type:'maxLength[20]',
                    prompt:'字符长度20'
                }]
            },
            bank:{
                identifier:'bank',
                rules:[{
                    type: 'empty',
                    prompt: '开户行不能为空'
                },{
                    type:'maxLength[50]',
                    prompt:'字符长度50'
                }]
            },
            account:{
                identifier:'account',
                rules:[{
                    type: 'empty',
                    prompt: '账号不能为空'
                },{
                    type: 'bankCard',
                    prompt: '6-30个数字'
                }]
            },
            address:{
                identifier:'address',
                rules:[{
                    type: 'maxLength[100]',
                    prompt: '字符长度100'
                }]
            },
            serviceContractFileUrls:{
                indextifier:'serviceContractFileUrls',
                rules:[{
                    type:'isNull',
                    prompt:'请上传居间服务协议'
                }]
            }


        }
    },

    save:function(){
        $('#intermediaryForm').form(init.validataForm).api({
            action:'save intermediary info',
            method:'post',
            serializeForm: true,
            beforeSend:function(settings){
                var serviceContractFileUrls = [];
                $('#intermediaryForm .js_fileBox .js_file a').each(function(n,ele){
                    serviceContractFileUrls.push({
                        name:$(ele).html(),
                        url:$(ele).attr('href')
                    })
                })
                settings.data['serviceContractFileUrls'] = JSON.stringify(serviceContractFileUrls)


                for(i in settings.data){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
                return settings;
            },
            onSuccess:function(data){
                $('#intermediaryInfo').modal('hide');
                $.uiAlert({
                    type: "success",
                    textHead: '保存成功',
                    text: data.msg,
                    time: 1,
                    onClosed:function(){
                        dtTable1.ajax.reload();
                        dtTable2.ajax.reload();
                    }
                })
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '保存失败',
                    text: data.msg,
                    time: 2,
                })
            }

        });
        $('#intermediaryForm').submit();
    },

    saveAndSubmit:function(){
        $('#intermediaryForm').form(init.validataForm).api({
            action:'submit intermediary info',
            method:'post',
            serializeForm: true,
            beforeSend:function(settings){
                var serviceContractFileUrls = [];
                $('#intermediaryForm .js_fileBox .js_file a').each(function(n,ele){
                    serviceContractFileUrls.push({
                        name:$(ele).html(),
                        url:$(ele).attr('href')
                    })
                })
                settings.data['serviceContractFileUrls'] = JSON.stringify(serviceContractFileUrls)

                for(i in settings.data){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
                return settings;
            },
            onSuccess:function(data){
                $('#intermediaryInfo').modal('hide');
                $.uiAlert({
                    type: "success",
                    textHead: '提交成功',
                    text: data.msg,
                    time: 1,
                    onClosed:function(){
                        dtTable1.ajax.reload();
                        dtTable2.ajax.reload();
                    }
                })
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '提交失败',
                    text: data.msg,
                    time: 2,
                })
            }
        });
        $('#intermediaryForm').submit();
    },

    bindUpload:function(){
        $(".js-uploadBtn").each(function(index) {
            var uploader = Object.create(Uploader_oneFile).set({
                //自己的单独参数
                browse_button: $('.js-uploadBtn')[index],
            });
            uploader.init();
        });
        var code = (new Date()).Format("yyyyMMddhhmmss");
        $('#intermediaryForm .js-uploadBtn').attr('data-code',code);
    },

    clearFile:function(){
        $(document).on('click','.js_clear',function(){
            $(this).parents('.js_file').remove();
        })
    }

};

$("#submitDate").dateRangePicker({separator: '~'});
$("#loanTime").dateRangePicker({separator: '~'});


$('.ui.menu a.item').on('click', function() {
    init.reset();
    $(this).addClass('active').siblings().removeClass('active');
    var status = $(this).attr('data-status');
    if(status == 'apply'){
        $('#part_one').removeClass('ks-hidden');
        $('#part_two').addClass('ks-hidden');
        $("#disableLoanTime").addClass('disabled');
    }else{
        $("#disableLoanTime").removeClass('disabled');
        $('#part_one').addClass('ks-hidden');
        $('#part_two').removeClass('ks-hidden');
    }
});

init.bindUpload();
init.clearFile();
init.list();