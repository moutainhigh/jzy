/**
 * Created by yangzb01 on 2017/4/11.
 */

/**
* 列表初始化渲染
*/

function initTable(){
    dataTable = $("#slBusinessList").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax":{
            "url": $(document).api.settings.api['query extension loan list'],
            "data": function (d) {
                var data = {};
                data.dueDate = $("#dueDate").val();
                data.borrserName = $("#borrserName").val();
                data.isExtension = $('#isExtension').val();
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            },
            type:'POST'
        },
        columns: [
            {data: 'code'},
            {data: 'productTypeName'},
            // {data: 'organizeName'},
            {data: 'saleName'},
            {data: 'borrserName'},
            {data: null},
            {data: null},
            {data: null},
            {data: null},
            {data: null},
            {data: null}
        ],
        columnDefs:[{
            targets: 4,
            className:'right aligned',
            render: function (data, type, row, meta) {
                return accounting.formatMoney(data.amount,'',2,',','.');
            }
        },{
            targets: 5,
            render: function (data, type, row, meta) {
                return moment(row.loanTime).format('YYYY-MM-DD');
            }
        },{
            targets:6,
            render:function(data, type, row, meta){
                if(data.dueDate !='--'){
                    return moment(data.dueDate).format('YYYY-MM-DD');
                }else{
                    return '--';
                }
            }
        },{
            targets: 7,
            render : function(data, type, row, meta){
                return enums.loan_status[data.loanStatus];
            }
        },{
            targets: 8,
            render : function(data, type, row, meta){
                return enums.approvalStatusTypeList[data.approvalStatusType]||data.approvalStatusType;
            }
        },{
            targets:9,
            className:"single line",
            render : function(data, type, row, meta){
                var flag = data.extensionLoanId == '--' ? 'true':'false';
                var status = '',
                    htm = '';
                if(data.approvalStatusType=='IN_APPROVAL'){
                    status  = 'disabled';
                    htm = '<a class="ui basic mini button" target="_blank" href="/extension/view?id='+row.extensionId+'&loanId='+row.id+'&pointType=G&flowConfigureType=EXTENSION&type=extension&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>';
                }
                return  '<a target="_blank" href="/business_apply/detail?id='+data.id+'&tab=extension&isExtension=true&comeFrom=/business_apply/extension&isDays='+data.termType+'" class="ui basic mini button '+status+'"><i class="info circle icon"></i>展期</a>' + htm;

            }
        }],
        "iDisplayLength": 20,
        "aLengthMenu": [
            [20],
            [20]
        ],
    });
}


/*
 * 点击查询
 * */
$('#search').on('click',function(){
    dataTable.ajax.reload();
});

/*
 * 点击重置
 * */
$('#reset').on('click',function(){
    $('#isExtension option[value=""]').prop('selected','selected');
    $('#borrserName').val("");
    $('#dueDate').val("");
});

function init(){
    $("#dueDate").dateRangePicker({});
    // var startDate =moment(new Date()).format('YYYY-MM-DD');
    // var endDate = moment(new Date()).add('days',7).format('YYYY-MM-DD');
    // $("#dueDate").val(startDate+" to "+endDate);
    initTable();
}
init()

