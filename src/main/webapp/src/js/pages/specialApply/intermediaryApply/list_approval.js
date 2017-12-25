/**
 * Created by yangzb01 on 2017-09-06.
 */

var init = {
    list:function(type){
        var pointType = type
        dtTable1 =  $("#approvalTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['approval list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    data.type = pointType;
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
                    return  '<a target="_blank" class="ui button mini basic" href="/bill_intermediary_apply/detail?id='+row.loanId+'&intermediaryApplyId='+row.id+'&type=intermediary&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'+
                            '<a target="_blank" class="ui button mini basic" href="/bill_intermediary_apply/approval?id='+row.loanId+'&intermediaryApplyId='+row.id+'&flowConfigureType=BROKERAGE_FEE&isApproval=true&pointType='+pointType+'&type=intermediary&comeFrom='+window.location.pathname+'">审批</a>'

                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });


        dtTable2 = $('#approvedTable').DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['approved list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.businessCode =$('#businessCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.loanTime =$('#loanTime').val();
                    data.type = type;
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

    search:function(){
        var status = $('#tab_menu .item.active').attr('data-status');
        if(status == 'approval'){
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

}

var path = window.location.pathname;
$("#submitDate").dateRangePicker({separator: '~'});
$("#loanTime").dateRangePicker({separator: '~'});


$('.ui.menu a.item').on('click', function() {
    init.reset();
    $(this).addClass('active').siblings().removeClass('active');
    var status = $(this).attr('data-status');
    if(status == 'approval'){
        $('#part_one').removeClass('ks-hidden');
        $('#part_two').addClass('ks-hidden');
        $("#disableLoanTime").addClass('disabled');
    }else{
        $("#disableLoanTime").removeClass('disabled');
        $('#part_one').addClass('ks-hidden');
        $('#part_two').removeClass('ks-hidden');
    }
});

if(path.indexOf('process_business_list') > -1){
    init.list('Y')
}else if(path.indexOf('process_risk_list') > -1){
    init.list('F')
}else if(path.indexOf('process_finance_list') > -1){
    init.list('C')
}
