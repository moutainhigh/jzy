/**
 * Created by yangzb01 on 2017-11-28.
 */
var init = {
    approvalList:function(){
        dtTable1 =  $("#approval").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['order approval list'],
                "type": 'post',
                data: function (d) {
                    var data = {};
                    var _d = $.extend({}, {searchKeys: data}, {start: d.start, length: d.length, draw: d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'pushTarget'},
                {data: 'code'},
                {data: 'itemType'},
                {data: 'amount'},
                {data: 'termStr'},
                {data: 'repayMethod'},
                {data: 'channelRate'},
                {data: 'pushDateTime'},
                {data: null}
            ],
            columnDefs:[{
                targets:0,
                render: function (data, type, row, meta) {
                    return data == 'KAISAFAX'? '佳兆业金服':'--'
                }
            },{
                targets:1,
                render: function (data, type, row, meta) {
                    return '<a href="/loan_push_order/detail?id='+row.id+'&comeFrom=/loan_push_order/approval_list">'+data+'</a>';
                }
            },{
                targets:2,
                render: function (data, type, row, meta) {
                    return enums.itemType[data];
                }
            },{
                targets:3,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return accounting.formatMoney(data,'',2,',','.');
                }
            },{
                targets:5,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return enums.orderRepayMethod[data];
                }
            },{
                targets:7,
                render: function (data, type, row, meta) {
                    return data? moment(data).format('YYYY-MM-DD'):'--';
                }
            },{
                targets:8,
                className:'single line',
                render:function(data, type, row, meta){
                    return '<a class="ui button basic mini" href="/loan_push_order/approval_detail?taskId='+row.taskId+'&orderId='+row.id+'">审批</a>';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });
    },
    approvedList:function(){
        dtTable2 = $("#approved").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['order approved list'],
                "type": 'post',
                data: function (d) {
                    var data = {};
                    var _d = $.extend({}, {searchKeys: data}, {start: d.start, length: d.length, draw: d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'pushTarget'},
                {data: 'code'},
                {data: 'itemType'},
                {data: 'platformLoanCode'},
                {data: 'amount'},
                {data: 'termStr'},
                {data: 'repayMethod'},
                {data: 'channelRate'},
                {data: 'pushDateTime'},
                {data: 'platformLoanTime'},
                {data: 'platformLoanRate'},
                {data: null},
                {data: 'platformLoanNextDueDate'},
                {data: 'status'},
                {data: 'platformLoanDueDate'},
                {data: 'platformLoanClearedDate'},
                {data: null}
            ],
            columnDefs:[{
                targets:0,
                render: function (data, type, row, meta) {
                    return data == 'KAISAFAX'? '佳兆业金服':'--'
                }
            },{
                targets:1,
                render: function (data, type, row, meta) {
                    return '<a href="/loan_push_order/detail?id='+row.id+'&comeFrom=/loan_push_order/approval_list">'+data+'</a>'
                }
            },{
                targets:2,
                render: function (data, type, row, meta) {
                    return enums.itemType[data];
                }
            },{
                targets:[3,10],
                render: function (data, type, row, meta) {
                    return data? data:'--';
                }
            },{
                targets:4,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return accounting.formatMoney(data,'',2,',','.');
                }
            },{
                targets:6,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return enums.orderRepayMethod[data];
                }
            },{
                targets:11,
                render: function (data, type, row, meta) {
                    return row.platformLoanTotalPeriod ? row.platformLoanCurrentPeriod+'/'+row.platformLoanTotalPeriod : '--'
                }
            },{
                targets:[8,9,12,14,15],
                render: function (data, type, row, meta) {
                    return data? moment(data).format('YYYY-MM-DD'):'--';
                }
            },{
                targets:13,
                render:function(data){
                    return enums.psuh_order_status[data];
                }
            },{
                targets:16,
                className:'single line',
                render:function(data, type, row, meta){
                    return  row.status == ('LOANED'||'OVERDUE'||'CLEARED') ? '<a class="ui button mini" onclick="init.repayPlan('+row.repayId+')">还款计划</a>':'--';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]

        });
    },

    /*
    * 获取还款计划
    * */
    repayPlan:function(id){

    }
};

$('.ui.menu a.item').on('click', function() {
    $(this).addClass('active').siblings().removeClass('active');
    var status = $(this).attr('data-status');
    if(status == 'one'){
        $('#part_one').removeClass('ks-hidden');
        $('#part_two').addClass('ks-hidden');
    }else{
        $('#part_one').addClass('ks-hidden');
        $('#part_two').removeClass('ks-hidden');
    }
});

init.approvalList();
init.approvedList();