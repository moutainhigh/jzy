/**
 * Created by yangzb01 on 2017-08-15.
 */

// 绑定上传事件
function bindUpload(){
    $(".js-uploadBtn").each(function(index) {;
        var uploader = Object.create(Uploader_oneFile).set({
            //自己的单独参数
            browse_button: $('.js-uploadBtn')[index],
        });
        uploader.init();
    });
}
bindUpload();

$(document).on('click','.js_clear',function(){
    $(this).parents('.js_file').remove();
});


var search= {
    reset:function(){
        $('#houseForm')[0].reset();
        $('#channelId').val('');
        $('input[name="saleName"]').val('');
    },

    time:function(){
        $('.js_dateRange').each(function(n,ele){
            $(ele).dateRangePicker({
                separator : '~',
            });
        })
    },

    reSearch:function(){
        dtTable.ajax.reload();
    },

    initSaleSearch: function () {
        $('.js-saleSearch').search({
            apiSettings: {
                method: "post",
                url: $(document).api.settings.api['search b_user'] + '?search={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            // minCharacters : 3
            onSelect: function (result, response) {
                $(this).find('input[name="saleName"]').val(result.name)
            },
        })

        $('.js-saleSearch .js-input').on('input propertychange', function () {
            $(this).prev('input').val($(this).val());
        })
    },

    exports:function(id){
        //todo
        var str = '';
        var nameList = [];
        var $this= $('#'+id)
        $this.find('.js_item').each(function(n,ele){
            nameList.push($(ele).attr('name'))
        })

        var d_val = [];
        for(i in nameList){
            d_val.push($('#'+id+' [name="'+nameList[i]+'"]').val())
        }
        for(var i =0;i<nameList.length;i++){
            if(i!=nameList.length-1){
                str += nameList[i]+'='+d_val[i]+'&';
            }else{
                str += nameList[i]+'='+d_val[i];
            }
        }
        window.open('/house_manage/houseManage_export?'+str,'_blank');
    },

    initChannel:function(){
        $('#managerChannelSearch').search({
            apiSettings: {
                method: "post",
                url: '/channel/list_channel_name' + '?channelName={query}&channelType=1'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            onSelect: function (data) {
                $("#channelId").val(data.id);
            }
        });
        $('#managerChannelSearch .js-input').on('input propertychange', function () {
            $(this).prev('input').val($(this).val());
        })
    },

    tableList:function(){
        dtTable = $("#cdTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['manage house list'],
                "data": function (d) {
                    var data ={};
                    $('#houseForm .js_item').each(function(n,ele){
                        data[''+$(ele).attr('name')+''] = $(ele).val();
                    })
                    var _d = $.extend({}, {searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                },
                "type": "POST",
            },
            columns: [
                {data: 'businessCode'},
                {data: 'productName'},
                {data: 'loanSubject'},
                {data: 'borrower'},
                {data: 'saleName'},
                {data: 'loanPrincipal'},
                {data: 'loanTerm'},
                {data: 'price'},
                {data: 'area'},
                {data: 'address'},

                {data: 'ower'},
                {data: 'code'},
                {data: 'mortgageType'},
                {data: 'guaranteeResponsibility'},
                {data: 'mortgageDate'},
                {data: 'noMortgageDate'},
                {data: 'propertyRightStatus'},
                {data: 'storageStatus'},
                {data: 'inDate'},
                {data: 'outDate'},
                {data: 'loanTime'},
                {data: 'dueDate'},
                {data: 'extensionDueDate'},

                {data: null},
                {data: null},
                {data: null},
            ],
            columnDefs: [{
                targets:[0,1,2,3,4,6,7,8,9,10,11],
                className:"single line",
                render: function (data, type, row, meta) {
                    return (data=='' | !data) ?'--' : data;
                }
            },{
                targets:[5],
                className:"single line right aligned ",
                render: function (data, type, row, meta) {
                    return (data == '' | !data) ? '0.00' : accounting.formatMoney(data,'',2,',','.');
                }
            },{
                targets:[12],
                className:"single line ",
                render: function (data, type, row, meta) {
                    return (data == '--' || !data || data =='') ? '--' : enums.MortgageType[data];
                }
            },{
                targets:[13],
                className:"single line right aligned ",
                render: function (data, type, row, meta) {
                    return (data == '--' || !data || data =='') ? '--' : accounting.formatMoney(data,'',2,',','.');
                }
            },{
                targets:[16],
                className:"single line",
                render: function (data, type, row, meta) {
                    return enums.PropertyRightStatus[data]
                }
            },{
                targets:[17],
                className:"single line",
                render: function (data, type, row, meta) {
                    return (data == '--' || !data || data =='') ? '--' : enums.StorageStatus[data]
                }
            },{
                targets:[14,15,18,19,20,21,22],
                className:"single line",
                render: function (data, type, row, meta) {
                    return  (data == '' | !data) ? '--' : moment(data).format('YYYY-MM-DD');
                }
            },{
                targets:[23],
                className:"single line",
                render: function (data, type, row, meta) {
                    if(row.propertyRightStatus =='WAITSECURED'){
                        return  '<a class="ui mini teal button" data-id="'+data.id+'" data-houseId="'+data.houseId+'" onclick="handle.show(\'WAITSECURED\',this)">抵押</a>'+
                                '<a class="ui mini button disabled">解押</a>'
                    }else if(row.propertyRightStatus =='SECURED'){
                        return  '<a class="ui mini button disabled">抵押</a>'+
                                '<a class="ui mini teal button" data-id="'+data.id+'" data-houseId="'+data.houseId+'" onclick="handle.show(\'SECURED\',this)">解押</a>'
                    }else{
                        return  '<a class="ui mini button disabled">抵押</a>'+
                                '<a class="ui mini button disabled">解押</a>'
                    }

                }
            },{
                targets:[24],
                className:"single line",
                render: function (data, type, row, meta) {
                    if(row.storageStatus == 'OUT'){
                        return  '<a class="ui mini disabled button">入库</a>'+
                                '<a class="ui mini disabled button">出库</a>'
                    }else if(row.storageStatus == 'IN'){
                        return  '<a class="ui mini disabled button">入库</a>'+
                                '<a class="ui mini teal button" data-id="'+data.id+'" data-houseId="'+data.houseId+'" onclick="handle.show(\'OUT\',this)">出库</a>'
                    }else{
                        return  '<a class="ui mini teal button" data-id="'+data.id+'" data-houseId="'+data.houseId+'" onclick="handle.show(\'IN\',this)">入库</a>'+
                                '<a class="ui mini disabled button">出库</a>'
                    }
                }
            },{
                targets:[25],
                className:"single line",
                render: function (data, type, row, meta) {
                    return '<a class="ui mini basic button" data-id="'+data.id+'" data-houseId="'+data.houseId+'" data-loanId="'+data.loanId+'"  onclick="handle.show(\'DETAIL\',this)"><i class="info circle icon"></i>查看</a>'
                }
            }],

            "iDisplayLength": 10,
            "aLengthMenu": [
                [10],
                [10]
            ]
        })
    },
}
search.time();
search.initChannel();
search.initSaleSearch();
search.tableList();


var validateForm = {
    inline: true,
    on: 'blur',
    fields:{
        guaranteeResponsibility:{
            identifier  : 'guaranteeResponsibility',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            },{
                type:'validateNumFloat[0.00-999999999.99]',
                prompt:'金额范围0.0-999999999.99'
            }]
        },
        mortgageType:{
            identifier  : 'mortgageType',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            }]
        },
        mortgageDate:{
            identifier  : 'mortgageDate',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            }]
        },
        noMortgageDate:{
            identifier:'noMortgageDate',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            }]
        },
        inDate:{
            identifier:'inDate',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            }]
        },
        outDate:{
            identifier:'outDate',
            rules: [{
                type : 'empty',
                prompt : "不为空"
            }]
        }

    }
}


var handle = {



    show:function(obj,ele){
        var $this = $('#'+obj+'_modal');
        var $form = $this.find('.ui.form');
        $form[0].reset();
        $this.find('.js_fileBox').html('');
        $this.modal('show');
        var houseId = $(ele).attr('data-houseId');
        var id = $(ele).attr('data-id');
        var loanId = $(ele).attr('data-loanId');

        handle.get_info(obj,id,houseId,loanId);

    },

    hide:function(id){
        var $this = $('#'+id+'_modal');
        $this.modal('hide');
    },


    submit_op:function(){
        $(document).on('click','.js_submit',function(){
            var id = $(this).attr('data-id');
            var $this = $('#'+id+'_modal')
            var $form = $this.find('.ui.form');

            $form.form(validateForm).api({
                action: "update house manage",
                method: 'POST',
                serializeForm: true,
                beforeSend:function (settings) {
                    if(id =='WAITSECURED'){
                        var fileUrls =[];
                        $('#'+id+'_modal .js_filebox .js_file a').each(function () {
                            fileUrls.push({
                                'fileName':$(this).html(),
                                'fileUrl':$(this).attr('href')
                            })
                        });
                        settings.data['fileUrls'] = JSON.stringify(fileUrls);
                        console.log(JSON.stringify(fileUrls));
                    }
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess:function(data){
                    $this.modal('hide');
                    $.uiAlert({type: "success", textHead: '操作成功', text: '保存成功', time: 1,
                        onClosed:function(){
                            dtTable.ajax.reload();
                        }
                    });
                },
                onFailure:function(data){
                    $.uiAlert({type: "danger", textHead: '操作失败', text: data.msg, time: 3});
                },
            })
            $form.submit();
        })
    },


    get_info:function(obj,id,houseId,loanId){
        if(loanId && loanId != undefined){
            $(document).api({
                on: "now",
                method: 'post',
                action: "view house manage",
                data: {
                    'id':id,
                    'loanId':loanId,
                    'houseId':houseId
                },
                onSuccess:function(data){
                   initCheckDetail(data);
                },
                onFailure:function(){
                    $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 3});
                }
            })
        }else{
            $(document).api({
                on: "now",
                method: 'post',
                action: "init house manage",
                data: {
                    'id':id,
                    'houseId':houseId
                },
                onSuccess:function(data){
                    initDetail(obj,data,id);
                },
                onFailure:function(){
                    $.uiAlert({type: "danger", textHead: '获取数据失败', text: data.msg, time: 3});
                }
            })
        }

        var initCheckDetail = function(data){
            $('#DETAIL_modal .js_info').html('');
            $('#DETAIL_modal .js_info').each(function(n,ele){
                var name = $(ele).attr('data-name')
                if(name in data.houseManage){
                    var val = data.houseManage[name];
                    if(name == 'propertyRightStatus'){
                        $(ele).html(enums.PropertyRightStatus[val]);
                    }else if(name =='loanPrincipal'){
                        $(ele).html(accounting.formatMoney(data.houseManage[name],'',2,',','.'));
                    }else if(name == 'storageStatus'){
                        $(ele).html(enums.StorageStatus[val]);
                    }else if(name == 'mortgageType'){
                        $(ele).html(enums.MortgageType[val]);
                    }else if(name =='guaranteeResponsibility'){
                        $(ele).html(accounting.formatMoney(val,'',2,',','.'));
                    }else if(name =='mortgageDate' || name =='noMortgageDate' || name =='outDate' || name =='inDate'){
                        if(val){
                            val = moment(val).format('YYYY-MM-DD');
                        } else{
                            val ='--'
                        }
                        $(ele).html(val);
                    }else{
                        $(ele).html(val);
                    }
                }

                if(data.houseManage.fileUrls){
                    $('#DETAIL_modal .js_fileBox').html('');
                    var fileDatas = data.houseManage.fileUrls;
                    for(i in JSON.parse(fileDatas)){
                        var fileData = JSON.parse(fileDatas)[i];
                        $('#DETAIL_modal .js_fileBox').append(
                            '<div class="js_file mb_5">' +
                            '<a target="_blank" href="'+fileData.fileUrl+'">'+fileData.fileName+'</a>' +
                            '</div>')
                    }
                }

            })
        }

        var initDetail = function(obj,data,id){
            var $this = $('#'+obj+'_modal').find('.ui.form');
            $this.find('input[name="id"]').val(id);
            var _data = data.houseManage;
            if(obj =='WAITSECURED'){
                $this.find('select[name="mortgageType"]').html('');
                $this.find("input[name='code']").val(_data.code);
                for(i in data.mortgageTypeList){
                    var val = data.mortgageTypeList[i]
                    $this.find('select[name="mortgageType"]').append('<option value="'+val+'">'+enums.MortgageType[val]+'</option>')
                }
            }else if(obj =='SECURED'){
                $this.find("input[name='guaranteeResponsibility']").val(data.houseManage.guaranteeResponsibility)
                $this.find("input[name='mortgageType']").val(enums.MortgageType[data.houseManage.mortgageType])
                $this.find("input[name='code']").val(_data.code);
                $this.find("input[name='fileUrls']").val(_data.fileUrls);
                if(_data.fileUrls){
                    $('#'+obj+'_modal .js_fileBox').html('');
                    for(i in JSON.parse(_data.fileUrls)){
                        var fileData = JSON.parse(_data.fileUrls)[i];
                        $('#'+obj+'_modal .js_fileBox').append(
                            '<div class="js_file mb_5">' +
                                '<a target="_blank" href="'+fileData.fileUrl+'">'+fileData.fileName+'</a>' +
                            '</div>')
                    }
                }
            }else if(obj =='IN' || obj == 'OUT'){
                $this.find("input[name='code']").val(_data.code);
            }
        }

    }


}
handle.submit_op()































