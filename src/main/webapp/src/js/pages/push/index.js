
var  productTypeCascade = TwoCascade({parentSelect:$("#productType"),subSelect:$("#product")})
productTypeCascade.create();
$('#loanMaxDueDate').dateRangePicker({separator: '~'});
var init = {
    littlePush:'',
    allPush:''
};

var initList = {
    noPushList:function(){
        dtTable1 = $("#noPush").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['get loan push list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.loanCode = $('#loanCode').val();
                    data.saleName = $('#saleName').val();
                    data.masterBorrowerName = $('#masterBorrowerName').val();
                    data.channelName = $('#channelName').val();
                    data.productTypeId = $('#productType').val();
                    data.productId = $('#product').val();
                    data.loanSubjectId = $('#loanSubjectId').val();
                    data.loanPushStatus = 'STAY_PUSH';
                    var _d = $.extend({},data,{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'loanCode'},
                {data: 'productTypeName'},
                {data: 'productName'},
                {data: 'loanSubjectName'},
                {data: 'source'},
                {data: 'masterBorrowerName'},
                {data: 'termStr'},
                {data: 'amount'},
                {data: 'leaderApprovedTime'},
                {data: 'loanTime'},
                {data: 'actualAmount'},
                {data: 'dueDate'},
                {data: 'loanStatus'},

            ],
            columnDefs: [{
                targets:[7,10],
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return data ? accounting.formatMoney(data,'',2,',','.'):'--'
                }
            },{
                targets:[8,9,11],
                render: function (data, type, row, meta) {
                    return data ? moment(data).format("YYYY-MM-DD"):'--';
                }
            },{
                targets:[12],
                className:'single line',
                render: function (data, type, row, meta) {
                    return enums.loan_status[data];
                }
            },{
                targets:13,
                className:"single line",
                render: function (data, type, row, meta) {
                    return  '<a target="_blank" class="ui button basic mini" href="/business_apply/detail?id='+row.loanId+'&tab=business&process=false">业务详情</a>' +
                            '<a class="ui button basic mini" href="/loan_push_order/index?loanPushId='+row.id+'&comeFrom=/loan_push/index">推单</a>';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        })
    },

    littlePushList:function(){
        dtTable2 = $("#littlePush").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['get loan push list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.loanCode = $('#loanCode').val();
                    data.saleName = $('#saleName').val();
                    data.masterBorrowerName = $('#masterBorrowerName').val();
                    data.channelName = $('#channelName').val();
                    data.productTypeId = $('#productType').val();
                    data.productId = $('#product').val();
                    data.loanSubjectId = $('#loanSubjectId').val();
                    data.loanPushStatus = 'PART_PUSHED';
                    var _d = $.extend({},data,{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'loanCode'},
                {data: 'productTypeName'},
                {data: 'productName'},
                {data: 'loanSubjectName'},
                {data: 'source'},
                {data: 'masterBorrowerName'},
                {data: 'termStr'},
                {data: 'amount'},
                {data: 'leaderApprovedTime'},
                {data: 'loanTime'},
                {data: 'actualAmount'},
                {data: 'loanStatus'},
                {data: 'dueDate'},
                {data: 'loanMaxDueDate'},
                {data: 'clearDate'},
                {data: null}
            ],
            columnDefs: [{
                targets:[7,10],
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return data ? accounting.formatMoney(data,'',2,',','.'):'--'
                }
            },{
                targets:[8,9,12,13,14],
                render: function (data, type, row, meta) {
                    return data ? moment(data).format("YYYY-MM-DD"):'--';
                }
            },{
                targets:[11],
                className:'single line',
                render: function (data, type, row, meta) {
                    return enums.loan_status[data];
                }
            },{
                targets:15,
                className:'single line',
                render: function (data, type, row, meta) {
                    return  '<a class="ui button basic mini" href="/business_apply/detail?id='+row.loanId+'&tab=business&process=false">业务详情</a>' +
                            '<a class="ui button basic mini" href="/loan_push_order/index?loanPushId='+row.id+'&comeFrom=/loan_push/index"">推单</a>';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        })
    },

    allPushList:function(){
        dtTable3 = $("#allPush").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax":{
                "url":$(document).api.settings.api['get loan push list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.loanCode = $('#loanCode').val();
                    data.saleName = $('#saleName').val();
                    data.masterBorrowerName = $('#masterBorrowerName').val();
                    data.channelName = $('#channelName').val();
                    data.productTypeId = $('#productType').val();
                    data.productId = $('#product').val();
                    data.loanSubjectId = $('#loanSubjectId').val();
                    data.loanPushStatus = 'PUSHED';
                    data.loanMaxDueDate = $('#loanMaxDueDate').val();
                    var _d = $.extend({},data,{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'loanCode'},
                {data: 'productTypeName'},
                {data: 'productName'},
                {data: 'loanSubjectName'},
                {data: 'source'},
                {data: 'masterBorrowerName'},
                {data: 'termStr'},
                {data: 'amount'},
                {data: 'leaderApprovedTime'},
                {data: 'loanTime'},
                {data: 'actualAmount'},
                {data: 'loanStatus'},
                {data: 'dueDate'},
                {data: 'loanMaxDueDate'},
                {data: 'clearDate'},
                {data: null}
            ],
            columnDefs: [{
                targets:[7,10],
                className:'right aligned',
                render: function (data, type, row, meta) {
                    return data ? accounting.formatMoney(data,'',2,',','.'):'--'
                }
            },{
                targets:[8,9,12,13,14],
                render: function (data, type, row, meta) {
                    return data ? moment(data).format("YYYY-MM-DD"):'--';
                }
            },{
                targets:[11],
                className:'single line',
                render: function (data, type, row, meta) {
                    return enums.loan_status[data];
                }
            },{
                targets:15,
                className:'single line',
                render: function (data, type, row, meta) {
                    return  '<a class="ui button basic mini" href="/business_apply/detail?id='+row.loanId+'&tab=business&process=false">业务详情</a>' +
                            '<a class="ui button basic mini" href="/loan_push_order/index?loanPushId='+row.id+'&comeFrom=/loan_push/index">推单详情</a>';
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
        document.getElementById('searchForm').reset()
    },
    search:function(){
        var dataStatus = $('#tab_menu a.item.active.teal').attr('data-status');
        if(dataStatus == 'one'){
            dtTable1.ajax.reload();
        }else if(dataStatus == 'two'){
            dtTable2.ajax.reload();
        }else if(dataStatus == 'three'){
            dtTable3.ajax.reload();
        }
    }
};
initList.noPushList();

$('#tab_menu a.item.teal').on('click',function(){
    $(this).addClass('active').siblings().removeClass('active');
    var tip = $(this).attr('data-status');
    $('#part_'+tip).removeClass('ks-hidden').siblings('.js_part').addClass('ks-hidden');
    initList.reset();
    if(tip !='one'){
        $('#js_dueDate').removeClass('disabled ks-hidden');
        var htm = tip == 'two'? '标的最小应结清日期':'标的最大应结清日期'
        $('#js_dueDate label').html(htm);
    }else{
        $('#js_dueDate').addClass('disabled ks-hidden');
    }

    if(tip == 'two' && init.littlePush ==''){
        initList.littlePushList();
        init.littlePush = 'false';
    }else if(tip == 'three' && init.allPush ==''){
        initList.allPushList();
        init.allPush = 'false';
    }
});




