/**
 * Created by yangzb01 on 2017-11-08.
 */


var init = {
    list:function() {
        dtTable = $("#applyTale").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            'ajax':{
                "url":$(document).api.settings.api['get cost apply list'],
                "type":'post',
                data:function(d){
                    var data = {};
                    data.code =$('#code').val();
                    data.businessName =$('#businessName').val();
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'costExemptionCode'},
                {data: 'code'},
                {data: 'businessName'},
                {data: 'createTime'},
                {data: null},
                {data: 'approveStatusDesc'},
                {data: null}
            ],
            columnDefs:[{
                targets:1,
                render: function (data, type, row, meta) {
                    return '<a target="_blank" href="/business_apply/detail?id='+row.loanId+'&tab=business&process=false">'+data+'</a>'
                }
            },{
                targets:2,
                render: function (data, type, row, meta) {
                    return data?data:'--';
                }
            },{
                targets:3,
                render: function (data, type, row, meta) {
                    return moment(data).format('YYYY-MM-DD')
                }
            },{
                targets:4,
                render:function(data, type, row, meta){
                    return enums.approvalStatusTypeList[row.approvalStatusType]
                }
            },{
                targets:5,
                render:function(data, type, row, meta){
                    return data? data:'--'
                }
            },{
                targets:6,
                className:"single line",
                render: function (data, type, row, meta) {
                    var flowBnt =  '<a target="_blank" class="ui button mini basic" href="/flow/to_approval_list?id='+row.id+'&flowConfigureType=COST_WAIVER&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>流程</a>';
                    if(row.approvalStatusType == 'IN_EDIT'){
                        return  '<a target="_blank" class="ui button mini basic" href="/cost_exemption/edit?loanId='+row.loanId+'&id='+row.id+'&comeFrom='+window.location.pathname+'"><i class="edit icon"></i>编辑</a>' +
                                '<div class="ui button mini basic" onclick="cancelCost(\''+row.id+'\')"><i class="cancel icon"></i>取消</div>'+flowBnt;
                    }else{
                        return '<a target="_blank" class="ui button mini basic" href="/cost_exemption/edit?loanId='+row.loanId+'&id='+row.id+'&view=true&comeFrom='+window.location.pathname+'"><i class="info circle icon"></i>查看</a>'+flowBnt;
                    }

                }
            }],
            "iDisplayLength": 10,
            "aLengthMenu": [
                [10],
                [10]
            ],
            initComplete: function () {
                $(".right.aligned.eight.wide.column").append("<div class='ui teal small button ' id='add'><i class='plus icon'></i>新增</div>");
            },
        })
    },

    search:function(){
        dtTable.ajax.reload();
    },
    reset:function(){
        $('#code').val('');
        $('#businessName').val('');
    },
    addApply:function(){
        $(document).on('click','#add',function(){
            $('#applyCost').modal('show');
        })
    }

};

init.list();
init.addApply();

//获取详情
$('#js_applyCostBtn').click(function(){
     var code = $('#businessCode').val();
    getDetail(code);
});

//reset
$('#js_applyReset').click(function(){
    $('#businessCode').val('');
    $('#applyInfo').html('');
});

//关闭modal
$('.js_close').click(function(){
    $('#applyCost').modal('hide');
    $('#businessCode').val('');
    $('#applyInfo').html('');
});

$('#js_applySubmit').click(function(){
    var id = $('#loanId').val();
    if(id){
        submitCostApply(id);
    }else{
        $.uiAlert({
            type: "danger",
            textHead: '申请失败',
            text: '请关联业务单',
            time: 3
        });
    }

});

/**
 * 查询详情
 * */
function getDetail(code){
    $(document).api({
        on:'now',
        method:'post',
        action:'get cost apply detail',
        data:{
            code:code
        },
        onSuccess:function(data){
            data.f_amount = function(){
                return accounting.formatNumber(this.amount,2,",");
            };
            data.f_loanDate = function(){
                if(this.loanDate){
                    return moment(this.loanDate).format('YYYY-MM-DD');
                }else{
                    return '--';
                }
            };
            var $busniessInfoTmp = utils.render('#busniessInfoTmp',data);
            $('#applyInfo').html($busniessInfoTmp);
        },
        onFailure: function (data) {
            $.uiAlert({type: "danger", textHead: '关联失败', text: data.msg, time: 3});
        }
    })
}


/**
 * 申请减免
 * */
function submitCostApply(id){
    $(document).api({
        on:'now',
        method:'post',
        action:'submit cost apply',
        data:JSON.stringify({loanId:id}),
        onSuccess:function(data){
            window.location.href = '/cost_exemption/edit?loanId='+id+'&id='+data.id+'&&comeFrom=/cost_exemption/index'
        },
        onFailure: function (data) {
            $.uiAlert({type: "danger", textHead: '申请失败', text: data.msg, time: 3});
        }
    })
}

function cancelCost(id){
    $.uiDialog('确定取消这笔减免吗？',{
        onApprove:function(){
            $(document).api({
                on:'now',
                action:'cancel cost approval',
                method:'post',
                data:{
                    'id':id,
                },
                onSuccess:function(data){
                    $.uiAlert({type: "success", textHead: '取消成功', text:'', time: 2});
                    dtTable.ajax.reload();
                },
                onFailure: function (data) {
                    $.uiAlert({type: "danger", textHead: '取消失败', text: data.msg, time: 3});
                }
            })
        }
    })
}