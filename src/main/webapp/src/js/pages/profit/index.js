/**
 * Created by yangzb01 on 2017-07-31.
 */

/**
* 列表table_init
* */

var table_init = {

    tmpList:{
        'PIAOJU':'detail_modal_bill',
        'YINPIAO':'detail_modal_bill',
        'GERENDAI':'detail_modal_personal'
    },

    search:function(){
        dtTable.ajax.reload();
    },

    search_reset:function(){
        $('#code').val('');
        $('#name').val('');
    },

    list:function(){
        dtTable = $("#profit_table").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['profit data list'],
                "data": function (d) {
                    var data = {}
                    data.code = $('#code').val();
                    data.name = $('#name').val();
                    var _d = $.extend({}, {searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                },
                "type": "POST",
            },
            columns: [
                {data: 'code'},
                {data: 'name'},
                {data: null},
                {data: null},
                {data: null}
            ],
            columnDefs: [{
                targets:2,
                render: function (data, type, row, meta) {
                    return enums.guaranty_type[data.guarantyType]
                }
            },{
                targets:3,
                render: function (data, type, row, meta) {
                    return enums.business_type[data.businessType]
                }
            },{
                className:"single line",
                targets: 4,
                render: function (data, type, row, meta) {
                    var txt = data.operation == 1 ? '添加':'编辑';
                    var modalType;
                    if(table_init.tmpList[data.type]){
                        modalType = table_init.tmpList[data.type];
                    }else{
                        modalType = 'detail_modal_else';
                    }

                    if(data.operation == 0){
                        return '<a class="ui mini basic button" data-productId="'+data.productTypeId+'" data-type="'+data.type+'" data-operation="'+data.operation+'" onclick="detail.show(\''+modalType+'\',this)">新增</a>';
                    }else{
                        return '<a class="ui mini basic button" data-productId="'+data.productTypeId+'" data-type="'+data.type+'" data-operation="'+data.operation+'" data-id="'+data.id+'" onclick="detail.show(\''+modalType+'\',this)">编辑</a>';
                    }

                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        })
    }
};

table_init.list();

/**
* detail
* */

$.fn.form.settings.rules.is_empty = function(value){
    var tmp_type = $('#position').val();
    if(tmp_type == 'PIAOJU'){
        return value == ''
    }else{
        return !(value == '');
    }
};


/*
* flag, true表示可以为0，false不可为0
* */
$.fn.form.settings.rules.decimal = function(value,flag){
    var f;
    var reg = /^[0-9]+(\.[0-9]+)$|^[0-9]+$/;
    var val = parseFloat(value);
    if(val == 0 && flag =='false'){
        f = false;
    }else{
        f = true;
    }
    return (reg.test(value) || value == '') && f;
}

var validateForm = {
    inline: true,
    on: 'blur',
    fields:{
        capitalCostDay:{
            identifier  : 'productProfit.capitalCostDay',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        capitalCostMonth:{
            identifier  : 'productProfit.capitalCostMonth',
            rules: [{
                type : 'is_empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        totalTax:{
            identifier  : 'productProfit.totalTax',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        valueAddedTax:{
            identifier  : 'productProfit.valueAddedTax',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        surtax:{
            identifier  : 'productProfit.surtax',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        laborCostSelf:{
            identifier  : 'productProfit.laborCostSelf',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[true]',
                prompt:'输入整数或小数（可为0）'
            },{
                type:'newCanBeDecimal[10,2]',
                prompt:'整数部分最多10位，小数点后最多2位'
            }]
        },
        laborCostChannel:{
            identifier  : 'productProfit.laborCostChannel',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[true]',
                prompt:'输入整数或小数（可为0）'
            },{
                type:'newCanBeDecimal[10,2]',
                prompt:'整数部分最多10位，小数点后最多2位'
            }]
        },
        administrativeExpenses:{
            identifier  : 'productProfit.administrativeExpenses',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[true]',
                prompt:'输入整数或小数（可为0）'
            },{
                type:'newCanBeDecimal[10,2]',
                prompt:'整数部分最多10位，小数点后最多2位'
            }]
        },
        brokerageFee:{
            identifier  : 'productProfit.brokerageFee',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        badAssetsReserve:{
            identifier  : 'productProfit.badAssetsReserve',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        operatingCostDay:{
            identifier  : 'productProfit.operatingCostDay',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        },
        operatingCostMonth:{
            identifier  : 'productProfit.operatingCostMonth',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'maxLength[16]',
                prompt:'含小数点在内最多16位'
            },{
                type:'decimal[false]',
                prompt:'输入整数或小数（且不为0）'
            },{
                type:'newCanBeDecimal[5,10]',
                prompt:'整数部分最多5位，小数点后最多10位'
            }]
        }
    }
}

var detail = {

    show:function(id,ele){
        var $this = $('#'+id)
        $this.modal('show');
        $this.find('.ui.form')[0].reset(); //初始化
        var operation = $(ele).attr('data-operation');
        var productId = $(ele).attr('data-productId');
        if(operation == 1){
            //为编辑时不传productTypeId
            var d_id = $(ele).attr('data-id');
            $this.find('.js_save').attr('id','js_edit_save');
            $('#js_edit_save').attr('data-id',d_id);
            detail.get_info(d_id,id);
            $this.find('input[name="productProfit.productTypeId"]').attr('disabled',true);
        }else{
            $this.find('.js_save').attr('id','js_add_save');
            $this.find('input[name="productProfit.productTypeId"]').attr('disabled',false);
            $this.find('input[name="productProfit.productTypeId"]').val(productId);
        }
        $('#position').val($(ele).attr('data-type'));
    },

    hide:function(id){
        var $this = $('#'+id);
        $this.modal('hide');
        $this.find('.js_save').attr('id','');
    },

    add:function(){
        $(document).on('click','#js_add_save',function(){
            var $this = $(this);
            var $form = $this.parents('.ui.form');
            $form.form(validateForm).api({
                action: "add profit",
                method: 'POST',
                serializeForm: true,
                beforeSend:function (settings) {
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess:function(data){
                    $this.parents('.ui.modal').modal('hide');
                    $.uiAlert({type: "success", textHead: '新增利润数据', text: '保存成功', time: 1,
                        onClosed:function(){
                            dtTable.ajax.reload();
                        }
                    });
                },
                onFailure:function(data){
                    $.uiAlert({type: "danger", textHead: '保存失败', text: data.msg, time: 3});
                },
            });
            $form.submit();
        })
    },


    get_info:function(id,obj){
        $(document).api({
            on: "now",
            method: 'post',
            action: "get profit by id",
            data: {
                'id':id
            },
            onSuccess:function(data){
                var $this = $('#'+obj).find('.ui.form');
                $this.find('input').each(function(n,ele){
                    var name = $(this).attr('name').split('.')[1];
                    $(ele).val(data[name]);
                })
            },
            onFailure:function(){
                $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 3});
            }
        })
    },

    edit:function(){
        $(document).on('click','#js_edit_save',function(){
            var id = $(this).attr('data-id');
            var $this = $(this);
            var $form = $this.parents('.ui.form');
            $form.form(validateForm).api({
                action: "edit profit",
                method: 'POST',
                serializeForm: true,
                beforeSend:function (settings) {
                    settings.data['productProfit.id'] = id;
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess:function(data){
                    $this.parents('.ui.modal').modal('hide');
                    $this.attr('id','');
                    $.uiAlert({type: "success", textHead: '利润数据配置', text: '保存成功', time: 1,
                        onClosed:function(){
                            dtTable.ajax.reload();
                        }
                    });
                },
                onFailure:function(data){
                    $.uiAlert({type: "danger", textHead: '保存失败', text: data.msg, time: 3});
                },
            });
            $form.submit();
        })
    }
};
detail.add();
detail.edit();



