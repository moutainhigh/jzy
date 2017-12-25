/**
 * Created by lw on 2017/4/12.
 */
var formQuery = {
    data: {
        s_name: '',
    },
    reset: function () {
        this.data = {
            s_name: '',
        }
    }
};
var dtTable;
function initPaymentAccountList() {
    dtTable = $("#paymentAccountTable").DataTable({
    serverSide: true,//服务端分页
    searching: false,//显示默认搜索框
    ordering: false,//开启排序
    autoWidth: true,//自适应宽度
    "ajax": {
        "url":'/account/account_list',
        "type":'POST',
        "data": function (d) {
            var _d = $.extend({},{searchKeys:formQuery.data},{start:d.start,length:d.length,draw:d.draw});
            return JSON.stringify(_d);
        },
        "dataType":'json',
    },
    columnDefs:[{
        targets: 0,
        render: function (data,type, row, meta) {
            return row.name == null ? '--' : row.name;
        }},{
        targets:1,
        render:function(data,type,row,meta){
            return row.bankName == null ? '--' : row.bankName;
        }
    },{
        targets:2,
        render:function(data,type,row,meta){
            return row.account == null ? '--' : row.account;
        }
    },{
        targets:3,
        render:function(data,type,row,meta){
            return row.updateBy == null ? '--' : row.updateBy;
        }
    },{
        targets:4,
        render:function(data,type,row,meta){
            return row.updateTime == null ? '--' : moment(row.updateTime).format('YYYY-MM-DD HH:mm:ss');
        }
    },{
        className:"center aligned",
        targets:5,
        render:function(data,type,row,meta){

             return '<a class="ui button basic mini" href="javascript:;" onclick="updatePaymentAccount(\'' + row.id + '\')"><i class="edit icon"></i>编辑</a>'
            + '<a class="ui button basic mini" href="javascript:;" onclick="deletePaymentAccount(\'' + row.id + '\')"><i class="delete icon"></i>删除</a>'

        }
    }
    ],
           "iDisplayLength": 10,
            initComplete: function () {
                $(".right.aligned.eight.wide.column").append("<div class='ui teal small button ' id='add'><i class='plus icon'></i>新增</div>");
            },
           "aLengthMenu": [
               [10],
               [10]
           ],
           "dom": "<'ui grid'<'row'<'eight wide column'><'right aligned eight wide column'>><'row dt-table'<'sixteen wide column'tr>><'row'<'seven wide column'i><'right aligned nine wide column'p>>>"
});

}

var searchForm = {
    init: function () {
        //查询
        $(".js-searchForm").form({
            onSuccess: function (e, fields) {
                e.preventDefault();
                formQuery.reset();
                $.extend(formQuery.data, fields);
                //console.log(formQuery.data)
                dtTable.ajax.reload();
            }
        })

    },

}

function initPage(){
    searchForm.init();
    initPaymentAccountList();

}


initPage();

function updatePaymentAccount(id) {

    var form = $("#edit_account_form");
    $(document).api({
            on: "now",
            url: "/account/edit_init",
            method: "post",
            data: {id: id},
            onSuccess: function (data) {
                var paymentAccount = data.paymentAccount;
                $("#id").val(paymentAccount.id);
                $("#edit_account_form input[name='name']").val(paymentAccount.name);
                $("#edit_account_form input[name='bankName']").val(paymentAccount.bankName);
                $("#edit_account_form input[name='account']").val(paymentAccount.account);
                $("#edit_account_modal").modal({
                    observeChanges: true,
                    blurring: true
                }).modal("show");
            },
            onFailure: function (data) {
                $.uiAlert(
                    {
                        type: "danger",
                        textHead: '获取数据错误',
                        text: data.msg,
                        time: 1
                    });
            }
        }
    );

}

function deletePaymentAccount(id) {

    $.uiDialog("您确认要删除此条回款账户记录吗？", {
        onApprove: function () {
            $(document).api({
                on: "now",
                method: 'post',
                url: "/account/delete_account",
                data: {
                    id: id,
                },
                onSuccess: function (data) {
                    $.uiAlert({
                        type: "success",
                        textHead: '删除成功',
                        text: '删除成功',
                        time: 1,
                        onClosed: function () {
                            dtTable.ajax.reload();
                        }
                    });
                },
                onFailure: function (data) {
                    $.uiAlert({
                        type: "danger",
                        textHead: '删除失败',
                        text: '删除失败',
                        time: 2
                    });
                }
            });
        }
    })

}

$(document).on('click', "#add",function () {
    $("#add_account_form")[0].reset();
    $("#add_account_modal").modal({
        observeChanges: true,
        blurring: true
    }).modal('show');
});

function add() {
    $('#add_account_form').form(validateOptions).api({
        action: 'add account',
        method: 'POST',
        serializeForm: true,
        beforeSend:function(settings){
            for(i in settings.data){
                var val = settings.data[i];
                settings.data[i] = $.trim(val);
            }
            return settings;
        },
        onSuccess: function (data) {
            $.uiAlert(
                {
                    type: "success",
                    textHead: '添加成功',
                    text: '成功添加回款账户',
                    time: 1,
                    onClosed: function () {
                        $('#add_account_modal')
                            .modal(
                                'hide');
                        dtTable.ajax.reload();
                    }
                });
        },
        onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '添加失败',
                    text: data.msg,
                    time: 1
                });
        }
    });
    $('#add_account_form').submit();
}

$("#editCancelBnt").on('click', function () {
    $('#edit_account_modal').modal("hide");
    $("#edit_account_form")[0].reset();

});
$("#addCancelBnt").on('click', function () {
    $('#add_account_modal').modal("hide");
    $("#add_account_form")[0].reset();

});
$(".close").on('click',function(){
    $("#add_account_form")[0].reset();
    $("#edit_account_form")[0].reset();
});

var validateOptions = {
    inline: true,
    fields: {
        name: {
            identifier: 'name',
            rules: [{
                type: 'empty',
                prompt: '回款账户户名不能为空'
            },{
                type: 'z_chRange[30]',
                prompt: '输入不超过30位中文'
            }]
        },
        bankName: {
            identifier: 'bankName',
            rules: [{
                type: 'empty',
                prompt: '回款账户开户行不能为空'
            }, {
                type: 'z_chRange[50]',
                prompt: '输入不超过50位的中文'
            }]
        },
        account: {
            identifier: 'account',
            rules: [{
                type: 'empty',
                prompt: '回款账户账号不能为空'
            }, {
                type: 'bankCard',
                prompt: '回款账户账号不正确（请输入6-30位数字）'
            }]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};


$("#editBnt").on('click', function () {
    $('#edit_account_form').form(validateOptions).api({
        method: 'post',
        action: 'update account',
        serializeForm: true,
        beforeSend:function(settings){
            for(i in settings.data){
                var val = settings.data[i];
                settings.data[i] = $.trim(val);
            }
            return settings;
        },
        onSuccess: function (data) {
            $.uiAlert(
                {
                    type: "success",
                    textHead: '修改成功',
                    text: data.msg,
                    time: 1,
                    onClosed: function () {
                        $('#edit_account_modal')
                            .modal('hide');
                        dtTable.ajax.reload();
                    }
                });
        },onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '修改失败',
                    text: data.msg,
                    time: 1
                });
        }
    });
    $('#edit_account_form').submit();
});

//银行卡号4位一空
$("input[name='account']").on("keyup",function(){
    var Val = $(this).val().replace(/\D/g, '').replace(/....(?!$)/g, '$& ');
    $(this).val(Val);
});