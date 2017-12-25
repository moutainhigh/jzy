/**
 * Created by yangzb01 on 2017-09-21.
 */
/**
 * Created by yangzb01 on 2017-09-15.
 */
var relationOderDetailInit = {
    /*
     * 未关联列表
     * */

    getData:function(callback){
        $(document).api({
            on:'now',
            action:'history loan relation',
            method:'post',
            data:{
                loanId : utils.getUrlParam('id')
            },
            onSuccess:function(data){
                callback(data);
            }
        })
    },


    unRelationList:function(data){
        dtTable1 = $('#detailUnRelationTable').DataTable({
            serverSide: false,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            destroy: true,//重新绘制dataTable
            data:data.loanList,
            columns: [
                {data: 'code'},
                {data: 'name'},
                {data: 'repayStatus'},
            ],
            columnDefs: [{
                className:"single line",
                targets: 0,
                render: function (data, type, row, meta) {
                    return  '<a class="ui mini" target="_blank" href="/business_apply/detail?id='+row.id+'&process=false&tab=business">'+data+'</a>';
                }
            }],
            "iDisplayLength": 5,
            "aLengthMenu": [
                [5],
                [5]
            ]
        })
    },


    /*
     * 已关联列表
     * */
    relationList:function(data){
        dtTable2 = $('#detailRelationTable').DataTable({
            serverSide: false,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            destroy: true,//重新绘制dataTable
            data:data.historyLoanRelationList,
            columns: [
                {data: 'code'},
                {data: 'name'},
                {data: 'repayStatus'},
            ],
            columnDefs: [{
                className:"single line",
                targets: 0,
                render: function (data, type, row, meta) {
                    return  '<a class="ui mini" target="_blank" href="/business_apply/detail?id='+row.id+'&process=false&tab=business">'+data+'</a>';
                }
            }],

            "iDisplayLength": 5,
            "aLengthMenu": [
                [5],
                [5]
            ],
        })
    },

    /*
     * 关联业务单
     * */
    relationCode:function(code){
        relationOrderInit.spStatus();
        $(document).api({
            on:'now',
            method:'post',
            action:'relation loan code',
            data:{
                code:code,
                loanId:utils.getUrlParam('id'),
                approveStatus:relationOrderInit.spStatus()
            },
            onSuccess:function(data){
                initAjaxReload()
                $.uiAlert({
                    type: "success",
                    textHead: '关联成功',
                    text: data.data,
                    time: 2
                });
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '关联失败',
                    text: data.message,
                    time: 2
                });
            }
        })
    }
};

function initAjaxReload(){
    relationOderDetailInit.getData(function(data){
        relationOderDetailInit.unRelationList(data);
        relationOderDetailInit.relationList(data);
    });
};
initAjaxReload();