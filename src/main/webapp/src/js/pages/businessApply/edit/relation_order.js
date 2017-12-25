/**
 * Created by yangzb01 on 2017-09-15.
 */
var relationOrderInit = {
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
        dtTable1 = $('#unRelationTable').DataTable({
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
                {data: null}
            ],
            columnDefs: [{
                className:"single line",
                targets: 0,
                render: function (data, type, row, meta) {
                    return  '<a class="ui mini" target="_blank" href="/business_apply/detail?id='+row.id+'&process=false&tab=business">'+data+'</a>';
                }
            },{
                className:"single line",
                targets: 3,
                render: function (data, type, row, meta) {
                    return  '<div class="ui button mini basic" onclick="relationOrderInit.relationCode(\''+row.code+'\')"><i class="edit icon"></i>关联</div>'
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
        dtTable2 = $('#relationTable').DataTable({
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
                {data: null}
            ],
            columnDefs: [{
                className:"single line",
                targets: 0,
                render: function (data, type, row, meta) {
                    return  '<a class="ui mini" target="_blank" href="/business_apply/detail?id='+row.id+'&process=false&tab=business">'+data+'</a>';
                }
            },{
                //   指定第最后一列
                className:"single line",
                targets: 3,
                render: function (data, type, row, meta) {
                    return  '<div class="ui button mini basic" onclick="relationOrderInit.delRelationCode(\''+row.id+'\')">删除</div>'
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
    },

    spStatus:function(){
        var approval = utils.getUrlParam('process');
        //审批时传状态
        if(approval =='true'){
            var taskKey = $('#taskKey').val()
        }else{
            var taskKey =''
        }

        return taskKey;
    },

    delRelationCode:function(id){
        $(document).api({
            on:'now',
            method:'post',
            action:'del relation code',
            data:{
                relationLoanId:id,
                loanId:utils.getUrlParam('id'),
                approveStatus:relationOrderInit.spStatus()
            },
            onSuccess:function(data){
                initAjaxReload();
                $.uiAlert({
                    type: "success",
                    textHead: '删除成功',
                    text: data.message,
                    time: 2
                });
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '删除失败',
                    text: data.message,
                    time: 2
                });
            }
        })
    },

    btnRelationCode:function(){
        $('#relationBtn').click(function(){
            var code = $('#businessCode').val();
            relationOrderInit.spStatus();

            if(code == ''){
                $.uiAlert({
                    type: "danger",
                    textHead: '请输入需要关联的业务单号',
                    text: '',
                    time: 2
                });
                return false
            }

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
                    initAjaxReload();
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
        })
    }

};

function initAjaxReload(){
    relationOrderInit.getData(function(data){
        relationOrderInit.unRelationList(data);
        relationOrderInit.relationList(data);
    });
};
initAjaxReload();


relationOrderInit.btnRelationCode();
