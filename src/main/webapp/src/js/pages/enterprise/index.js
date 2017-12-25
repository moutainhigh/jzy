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
function initEnterpriseList() {
    dtTable = $("#enterpriseTable").DataTable({
    serverSide: true,//服务端分页
    searching: false,//显示默认搜索框
    ordering: false,//开启排序
    autoWidth: true,//自适应宽度
    "ajax": {
        "url":'/enterprise/enterprise_list',
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
            return row.level == null ? '--' : getLevel(row.level);
        }
    },{
        className:'right aligned',
        targets:2,
        render:function(data,type,row,meta){
            return row.price == null ? '--' : accounting.formatNumber(row.price,2);
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

             return '<a class="ui button basic mini" href="javascript:;" onclick="updateEnterprise(\'' + row.id + '\')"><i class="edit icon"></i>编辑</a>'

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
    initEnterpriseList();

}


initPage();

//更新企业
function updateEnterprise(id) {

    var form = $("#edit_enterprise_form");
    $(document).api({
            on: "now",
            url: "/enterprise/edit_init",
            method: "post",
            data: {id: id},
            onSuccess: function (data) {
                var enterprise = data.enterprise;

                $("#edit_enterprise_form .item").each(function(n,ele){
                    var name = $(ele).attr('name');
                    $(ele).val(enterprise[name]);
                });
                if(enterprise["establishDate"]){
                    $('input[name="establishDate"]').val(moment(enterprise["establishDate"]).format("YYYY-MM-DD"));//生效时间
                }
                if(enterprise["level"]){
                    form.find(("select[name='level']")).val(
                        enterprise.level
                    )
                }
                form.find('#companies').html(enterprise.companies);

                $("#edit_enterprise_modal").modal({
                    observeChanges: true,
                    blurring: true
                }).modal("show");

                changeCompanyType('edit_enterprise_form');
                if(enterprise.type =='CREDITCOMPANY'){
                    $('#edit_enterprise_form').on('input propertychange','input[name="creditQuota"]',function () {
                        var $thisVal = $(this).val() =='' ? 0:parseFloat($(this).val());
                        if( enterprise.libraryAmount!= '0' && enterprise.libraryAmount){
                            var creditQuotaVal = sub($thisVal,enterprise.libraryAmount);
                            $('#edit_enterprise_form input[name="remainderAmount"]').val(creditQuotaVal);
                        }else{
                            $('#edit_enterprise_form input[name="remainderAmount"]').val($thisVal)
                        }
                    });
                }
            },
            onFailure: function (data) {
                $.uiAlert({
                    type: "danger",
                    textHead: '获取数据错误',
                    text: data.msg,
                    time: 1
                });
            }
        }
    );

}

function getLevel(level) {
    if(null != level){
        if('A'==level){
            return 'AAA';
        }
        if('B'==level){
            return 'AA';
        }
        if('C'==level){
            return 'A';
        }
    }
}

$(document).on('click', "#add",function () {
    $("#add_enterprise_form")[0].reset();
    $("#add_enterprise_modal").modal({
        observeChanges: true,
        blurring: true
    }).modal('show');
});

//新增企业
function addCompany() {
    $('#add_enterprise_form').form(validateOptions).api({
        action: 'add enterprise',
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
            $.uiAlert({
                type: "success",
                textHead: '添加成功',
                text: '成功添加企业',
                time: 1,
                onClosed: function () {
                    $('#add_enterprise_modal').modal('hide');
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
    $('#add_enterprise_form').submit();
}


//改变公司类型级联
function changeCompanyType(obj){
    var slVal = $('#'+obj).find('select[name="type"]').val();
    change(slVal);
    $('#'+obj).on('change','select[name="type"]',function(){
        var slVal = $(this).val();
        change(slVal);
    });

    function change(slVal){
        if(slVal == 'CREDITCOMPANY'){
            $('#'+obj).find('input[name="libraryAmount"],input[name="companyId"]').attr('disabled',true);
            $('#'+obj).find('input[name="creditQuota"]').attr('disabled',false);
            $('#'+obj).find('.js_libraryAmount').addClass('disabled ks-hidden');
            $('#'+obj).find('.js_companyId').addClass('ks-hidden disabled');
            $('#'+obj).find('.js_remainderAmount').removeClass('ks-hidden disabled');
            $('#'+obj).find('.js_creditQuota').removeClass('ks-hidden disabled');
            $('#'+obj).find('.js_companies').removeClass('ks-hidden');
            $('#'+obj).find('input[name="remainderAmount"]').attr('disabled',false);
        }else{
            $('#'+obj).find('input[name="remainderAmount"]').attr('disabled',true);
            $('#'+obj).find('input[name="creditQuota"]').attr('disabled',true);
            $('#'+obj).find('input[name="libraryAmount"],input[name="companyId"]').attr('disabled',false);
            $('#'+obj).find('.js_libraryAmount').removeClass('disabled ks-hidden');
            $('#'+obj).find('.js_companyId').removeClass('ks-hidden disabled');
            $('#'+obj).find('.js_remainderAmount').addClass('ks-hidden disabled');
            $('#'+obj).find('.js_creditQuota').addClass('ks-hidden disabled');
            $('#'+obj).find('.js_companies').addClass('ks-hidden');
        }
    }
}
changeCompanyType('add_enterprise_form');


$(".js_companyId").search({
    apiSettings: {
        method: "post",
        url: $(document).api.settings.api['query credit company'] + '?name={query}'
    },
    fields: {
        results: 'data',
        title: 'name',
        price:'',
        // description: ''
    },
    onSelect: function (data) {
        $(this).find('input[name="companyId"]').val(data.id);
    }
});

$('.js_companyId input[name="companyName"]').on('input propertychange', function () {
    $(this).prev('input').val("");
});

$('#add_enterprise_form input[name="creditQuota"]').on('input propertychange',function () {
    var creditQuotaVal = $(this).val();
    $('#add_enterprise_form input[name="remainderAmount"]').val(creditQuotaVal);
});


//初始化表单
$("#editCancelBnt").on('click', function () {
    $('#edit_enterprise_modal').modal("hide");
    $("#edit_enterprise_form")[0].reset();

});

//初始化表单
$("#addCancelBnt").on('click', function () {
    $('#add_enterprise_modal').modal("hide");
    $("#add_enterprise_form")[0].reset();
});

//关闭dialog初始化表单
$(".close").on('click',function(){
    $("#add_enterprise_form")[0].reset();
    $("#edit_enterprise_form")[0].reset();
});

/*
* 2位小数或整数，可为负
* */
$.fn.form.settings.rules.floatNum = function(value){
    var reg = /^-?[0-9]+(\.[0-9]{1,2})?$/;
    return reg.test(value) || value ==''
};

var validateOptions = {
    inline: true,
    fields: {
        name: {
            identifier: 'name',
            rules: [
                {
                    type: 'empty',
                    prompt: '企业名称不能为空'
                },
                {
                    type: 'maxLength[50]',
                    prompt: '企业名称不得超过50个汉字'
                }
            ]
        },
        level: {
            identifier: 'level',
            rules: [
                {
                    type: 'empty',
                    prompt: '资信等级不能为空'
                }
            ]
        },
        price: {
            identifier: 'price',
            rules: [
                {
                    type: 'empty',
                    prompt: '低价不能为空'
                },
                {
                    type:'canBeDecimal[2]',
                    prompt:'金额为2位小数'
                },{
                    type:'validateNumFloat[0-999999999]',
                    prompt:'金额范围0-999999999'
                }
            ]
        },
        companyId: {
            identifier: 'companyId',
            rules: [
                {
                    type: 'empty',
                    prompt: '请选择数据中的授信公司关联'
                }
            ]
        },
        establishDate: {
            identifier: 'establishDate',
            rules: [
                {
                    type: 'empty',
                    prompt: '成立时间不能为空'
                }
            ]
        },
        nature: {
            identifier: 'nature',
            rules: [
                {
                    type: 'empty',
                    prompt: '所有制性质不能为空'
                }
            ]
        },
        computerNature: {
            identifier: 'computerNature',
            rules: [
                {
                    type: 'empty',
                    prompt: '公司性质不能为空'
                }
            ]
        },
        creditQuota: {
            identifier: 'creditQuota',
            rules: [
                {
                    type: 'empty',
                    prompt: '授信额度不能为空'
                },
                {
                    type:'canBeDecimal[2]',
                    prompt:'金额为2位小数'
                },{
                    type:'validateNumFloat[0-999999999.99]',
                    prompt:'金额范围0-999999999.99'
                }
            ]
        },
        remainderAmount: {
            identifier: 'remainderAmount',
            rules: [
                {
                    type: 'empty',
                    prompt: '剩余额度不能为空'
                }, {
                    type:'validateNumFloat[0-999999999.99]',
                    prompt:'剩余额度不能小于0'
                }
            ]
        },
        year: {
            identifier: 'year',
            rules: [
                {
                    type: 'empty',
                    prompt: '年度不能为空'
                }
            ]
        },
        businessIncome: {
            identifier: 'businessIncome',
            rules: [
                {
                    type:'canBeDecimal[2]',
                    prompt:'金额为2位小数'
                },{
                    type:'validateNumFloat[0-999999999]',
                    prompt:'金额范围0-999999999'
                }
            ]
        },
        notNetProfit: {
            identifier: 'notNetProfit',
            rules: [{
                    type:'floatNum',
                    prompt:'金额为2位小数'
                },{
                    type:'maxLength[13]',
                    prompt:'字符长度为13位'
                }
            ]
        },
        cashFlowNet: {
            identifier: 'cashFlowNet',
            rules: [
                {
                    type:'floatNum',
                    prompt:'金额为2位小数'
                },{
                    type:'maxLength[13]',
                    prompt:'字符长度为13位'
                }
            ]
        },
        assetsDebtRatio: {
            identifier: 'assetsDebtRatio',
            rules: [
                {
                    type:'canBeDecimal[2]',
                    prompt:'输入为2位小数'
                },{
                    type:'validateNumFloat[0-100]',
                    prompt:'输入范围0-100'
                }
            ]
        },
        computerBasic: {
            identifier: 'computerBasic',
            rules: [
                {
                    type: 'empty',
                    prompt: '公司基本情况不能为空'
                }
            ]
        },
        executorSituation: {
            identifier: 'executorSituation',
            rules: [
                {
                    type: 'empty',
                    prompt: '失信执行人情况不能为空'
                }
            ]
        },
        dispute: {
            identifier: 'dispute',
            rules: [
                {
                    type: 'empty',
                    prompt: '纠纷不能为空'
                }
            ]
        },
        managerOpinion: {
            identifier: 'managerOpinion',
            rules: [
                {
                    type: 'empty',
                    prompt: '经办人意见不能为空'
                }
            ]
        },
        otherSituation: {
            identifier: 'otherSituation',
            rules: [
                {
                    type: 'empty',
                    prompt: '其他情况不能为空'
                }
            ]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};

//修改
$("#editBnt").on('click', function () {
    $('#edit_enterprise_form').form(validateOptions).api({
        method: 'post',
        action: 'update enterprise',
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
                        $('#edit_enterprise_modal')
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
    $('#edit_enterprise_form').submit();
});