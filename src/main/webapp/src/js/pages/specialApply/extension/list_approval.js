/**
 * Created by yangzb01 on 2017-11-01.
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
                "url":$(document).api.settings.api['extension approval list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.extensionCode =$('#extensionCode').val();
                    data.submitDate =$('#submitDate').val();
                    data.code = $('#code').val();
                    data.type = pointType;
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'code'},
                {data: 'extensionCode'},
                {data: 'productTypeName'},
                {data: 'businessSource'},
                {data: 'borrowerName'},
                {data: 'amount'},
                {data: null},
                {data: null},
                {data: 'approvalStatusType'},
                {data: 'approveStatusDesc'},
                {data: null}
            ],
            columnDefs: [{
                targets:5,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    if(data){
                        return accounting.formatNumber(data,2,",");
                    }else {
                        return '--';
                    }
                }
            },{
                targets:6,
                render: function (data, type, row, meta) {
                    if(null != row.termType && row.termType == 'FIXED_DATE'){
                        return  '至'+row.term;
                    }else {
                        return row.term == null ? '--' : row.term + "" + enums.loanTermType[row.termType];
                    }
                }
            },{
                targets:7,
                render: function (data, type, row, meta) {
                    return enums.loan_status[data.loanStatus];
                }
            },{
                targets:8,
                render: function (data, type, row, meta) {
                    return enums.approvalStatusTypeList[data];
                }
            },{targets:9,
                render: function (data, type, row, meta) {
                    return data?data:'--';
                }
            },{
                //   指定第最后一列
                targets: 10,
                className:"single line",
                render: function (data, type, row, meta) {
                    return  '<a target="_blank" class="ui button mini basic" href="/extension/view?id='+row.id+'&loanId='+row.loanId+'&type=extension&pointType='+pointType+'&flowConfigureType=EXTENSION&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'+
                            '<a target="_blank" class="ui button mini basic" href="/extension/approval_page?id='+row.id+'&loanId='+row.loanId+'&flowConfigureType=EXTENSION&isApproval=true&pointType='+pointType+'&type=extension&comeFrom='+window.location.pathname+'">审批</a>'

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
                "url":$(document).api.settings.api['extension complete list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.extensionCode =$('#extensionCode').val();
                    data.code = $('#code').val();
                    data.type = type;
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'code'},
                {data: 'extensionCode'},
                {data: 'productTypeName'},
                {data: 'businessSource'},
                {data: 'borrowerName'},
                {data: 'amount'},
                {data: null},
                {data: null},
                {data: 'approvalStatusType'},
                {data: 'approveStatusDesc'},
                {data: null}
            ],
            columnDefs: [{
                targets:5,
                className:'right aligned',
                render: function (data, type, row, meta) {
                    if(data){
                        return accounting.formatNumber(data,2,",");
                    }else {
                        return '--';
                    }
                }
            },{
                targets:6,
                render: function (data, type, row, meta) {
                    if(null != row.termType && row.termType == 'FIXED_DATE'){
                        return  '至'+row.term;
                    }else {
                        return row.term == null ? '--' : row.term + "" + enums.loanTermType[row.termType];
                    }
                }
            },{
                targets:7,
                render: function (data, type, row, meta) {
                    return enums.loan_status[data.loanStatus];
                }
            },{
                targets:8,
                render: function (data, type, row, meta) {
                    return enums.approvalStatusTypeList[data];
                }
            },{targets:9,
                render: function (data, type, row, meta) {
                    return data?data:'--';
                }
            },{
                //   指定第最后一列
                className:"single line",
                targets: 10,
                render: function (data, type, row, meta) {
                    return  '<a target="_blank" class="ui button mini basic" href="/extension/view?id='+row.id+'&loanId='+row.loanId+'&pointType='+pointType+'&flowConfigureType=EXTENSION&type=extension&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'+
                            '<a target="_blank" class="ui button mini basic" href="/flow/to_approval_list?id='+row.id+'&flowConfigureType=EXTENSION&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>流程查看</a>';
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
        $('#extensionCode').val('');
        $('#code').val('');
    },

}

var path = window.location.pathname;

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

if(path.indexOf('list_approval_business') > -1){
    init.list('Y')
}else if(path.indexOf('list_approval_risk') > -1){
    init.list('F')
}else if(path.indexOf('list_approval_finance') > -1) {
    init.list('C')
}else if(path.indexOf('list_approval_senior') > -1){
    init.list('G')
}
