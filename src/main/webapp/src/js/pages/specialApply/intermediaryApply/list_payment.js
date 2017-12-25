/**
 * Created by yangzb01 on 2017-09-07.
 */

var init = {
    list:function(type){
        dtTable1 =  $("#loanTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['intermediary loan list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    data.loanStatus = 'APPROVEEND';
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
                    return  '<div class="ui button mini basic" onclick="init.showModal(\''+row.loanId+'\')">放款</div>'+
                            '<a target="_blank" class="ui button mini basic" href="/bill_intermediary_apply/detail?id='+row.loanId+'&intermediaryApplyId='+row.id+'&type=intermediary&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>';


                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });


        dtTable2 = $('#loanedTable').DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['intermediary loan list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    data.loanStatus = 'LOANED';
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
                //   指定第最后一列
                className:"single line",
                targets: 8,
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
    reset:function(){
        $('#businessCode').val('');
        $('#submitDate').val('');
        $('#loanTime').val('');
    },

    search:function(){
        var status = $('#tab_menu .item.active').attr('data-status');
        if(status == 'loan'){
            dtTable1.ajax.reload();
        }else{
            dtTable2.ajax.reload();
        }
    },

    showModal:function(loanId){
        $('#loanAmountInfo').modal('show');
        $(document).api({
            on:'now',
            method:'post',
            action:'confirm loan info',
            data:{
                loanId:loanId
            },
            onSuccess:function(data){
                $('#loanInfo .item,#js-urlBox').html('');
                $('#loanInfo input[name="loanId"]').val(loanId)
                if(data.intermediaryApply.serviceContractFileUrls){
                    var urlList =JSON.parse(data.intermediaryApply.serviceContractFileUrls);
                    for(i in urlList){
                        var _data =  urlList[i];
                        $('#js-urlBox').append('<a class="ui button mini margin5t" href="'+_data.url+'" download>'+_data.name+'</a><br/>');
                    }
                }
                $('#loanInfo .item').each(function(n,ele){
                    var name = $(ele).attr('data-name');
                    $(ele).html(data.intermediaryApply[name]);
                    if(name == 'intermediaryFee'){
                        $(ele).html(accounting.formatMoney(data.intermediaryFee,'',2,',','.'));
                    }else{
                        $(ele).html(data[name]);
                    }
                })
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '获取放款信息失败',
                    text: data.msg,
                    time: 2,
                })
            }
        })
    },

    loanAmount:function(){
        var loanId =  $('#loanInfo input[name="loanId"]').val();
        $.uiDialog('确认放款？',{
            onApprove:function(){
                $(document).api({
                    on:'now',
                    method:'post',
                    action:'confirm loan',
                    data:{
                        loanId:loanId
                    },
                    onSuccess:function(data){
                        $('#loanAmountInfo').modal('hide');
                        $.uiAlert({
                            type: "success",
                            textHead: '放款成功',
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
                            textHead: '获取放款信息失败',
                            text: data.msg,
                            time: 2,
                        })
                    }
                })
            }
        })

    },
    printApply:function(){
        var loanId =  $('#loanInfo input[name="loanId"]').val();
        window.open("/bill_intermediary_apply/document_download?loanId="+loanId,'_blank');

    },
    down:function(){

    }

}


$("#submitDate").dateRangePicker({separator: '~'});
$("#loanTime").dateRangePicker({separator: '~'});

$('.ui.menu a.item').on('click', function() {
    init.reset();
    $(this).addClass('active').siblings().removeClass('active');
    var status = $(this).attr('data-status');
    if(status == 'loan'){
        $('#part_one').removeClass('ks-hidden');
        $('#part_two').addClass('ks-hidden');
        $("#disableLoanTime").addClass('disabled');
    }else{
        $("#disableLoanTime").removeClass('disabled');
        $('#part_one').addClass('ks-hidden');
        $('#part_two').removeClass('ks-hidden');
    }
});

init.list();