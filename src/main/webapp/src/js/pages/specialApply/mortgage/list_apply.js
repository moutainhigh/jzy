/**
 * Created by yangzb01 on 2017-09-18.
 */

var certifType;
//港澳台通行证校验
$.fn.form.settings.rules.passId = function(value){
    if(certifType == 'PASS_ID'){
        var str_l = value.length;
        switch (str_l){
            case 11:
                var reg = new RegExp("^(H|M)[0-9]{10}$");
                return reg.test(value)
            case 8 :
                var reg = new RegExp("^[0-9]{8}$");
                return reg.test(value);
            case 10:
                //香港身份证
                var hk_reg = /^[A-Z]{1}[0-9]{6}(\(|\（)[0-9A-Z]{1}(\)|\）)$/;
                //澳门身份证
                var am_reg = /^(1|5|7){1}[0-9]{6}(\(|\（)[0-9]{1}(\)|\）)$/;
                //台湾身份证
                var tw_reg = /^[A-Z]{1}[0-9]{9}$/;
                return am_reg.test(value) || tw_reg.test(value) || hk_reg.test(value);
            default:
                return false;
        }
    }else{
        return true;
    }
};

//身份证
$.fn.form.settings.rules.identityCodeValids = function (value) {
    if(certifType == 'ID') {
        var city = {
            11: "北京", 12: "天津", 13: "河北", 14: "山西", 15: "内蒙古",
            21: "辽宁", 22: "吉林", 23: "黑龙江 ",
            31: "上海", 32: "江苏", 33: "浙江", 34: "安徽", 35: "福建", 36: "江西", 37: "山东",
            41: "河南", 42: "湖北 ", 43: "湖南", 44: "广东", 45: "广西", 46: "海南",
            50: "重庆", 51: "四川", 52: "贵州", 53: "云南", 54: "西藏 ",
            61: "陕西", 62: "甘肃", 63: "青海", 64: "宁夏", 65: "新疆",
            71: "台湾",
            81: "香港",
            82: "澳门",
            91: "国外 "
        };
        var tip = "";
        var pass = true;

        if (!value || !/^\d{6}(18|19|20)?\d{2}(0[1-9]|1[012])(0[1-9]|[12]\d|3[01])\d{3}(\d|X)$/i.test(value)) {
            tip = "身份证号格式错误";
            pass = false;
        } else if (!city[value.substr(0, 2)]) {
            tip = "地址编码错误";
            pass = false;
        } else {
            //18位身份证需要验证最后一位校验位
            if (value.length == 18) {
                value = value.split('');
                //∑(ai×Wi)(mod 11)
                //加权因子
                var factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
                //校验位
                var parity = [1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2];
                var sum = 0;
                var ai = 0;
                var wi = 0;
                for (var i = 0; i < 17; i++) {
                    ai = value[i];
                    wi = factor[i];
                    sum += ai * wi;
                }
                var last = parity[sum % 11];
                if (parity[sum % 11] != value[17]) {
                    tip = "校验位错误";
                    pass = false;
                }
            }
        }
        return pass;
    }else{
        return true;
    }
};

var validate1 = {
    inline: true,
    fields:{
        saleId: {
            identifier: 'saleId',
            rules: [{
                type: 'empty',
                prompt: '请选择业务员有效数据'
            }]
        },
        channelId: {
            identifier: 'channelId',
            rules: [{
                type: 'empty',
                prompt: '请选择业务来源有效数据'
            }]
        }
    }

};

var validate2 = {
    inline:true,
    fields:{
        housePropertyNumber:{
            identifier:'housePropertyNumber',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'maxLength[60]',
                prompt:'房产证号不超过60个字符'
            }]
        },
        address:{
            identifier:'address',
            rules:[{
                type: 'empty',
                prompt: '不为空'
            }, {
                type:'maxLength[60]',
                prompt:'房产地址不超过60个字符'
            }]
        },
        area: {
            identifier: 'area',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'validateNumFloat',
                prompt:'输入正整数或2位小数且在0.01-99999999.99内'
            }]
        },
        maximumLoanAmount:{
            identifier:'maximumLoanAmount',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'validateNumFloat[0-99999999.99]',
                prompt:'输入正整数或2位小数且在0-99999999.99内'
            }]
        },
        internalEvaluationValue:{
            identifier:'internalEvaluationValue',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'validateNumFloat[0-99999999.99]',
                prompt:'输入正整数或2位小数且在0-99999999.99内'
            }]
        },

        loanInterestRate:{
            identifier: 'loanInterestRate',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'canBeDecimal[10]',
                prompt:'最多10为小数'
            },{
                type:'maxLength[30]',
                prompt:'不超过30个字符'
            }]
        },
        startBorrowingTime:{
            identifier: 'startBorrowingTime',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            }]
        }
    }
};

var validate3 = {
    inline:true,
    fields:{
        name:{
            identifier: 'name',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'maxLength[30]',
                prompt:'不超过30位字符'
            }]
        },
        certifType:{
            identifier: 'certifType',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            }]
        },
        'certificateNo':{
            identifier: 'certificateNo',
            rules: [{
                type: 'empty',
                prompt: '不为空'
            },{
                type:'passId',
                prompt:'证件号格式不对'
            },{
                type:'identityCodeValids',
                prompt:'身份证格式不对'
            },{
                type:'maxLength[30]',
                prompt:'不超过30位字符'
            }]
        },
        homeAddress:{
            identifier: 'homeAddress',
            rules: [{
                type:'maxLength[200]',
                prompt:'不超过200位字符'
            }]
        }
    }
};


var init = {
    list: function () {
        dtTable = $("#listTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['mortgage apply list'],
                "type": 'post',
                data: function (d) {
                    var data = {};
                    data.mortgageCode = $('#applyCode').val();
                    data.businessName = $('#borrower').val();
                    data.houseMortgageType = $('#addressType').val();
                    data.approvalStatusType = $('#loanStatus').val();
                    var _d = $.extend({}, {searchKeys: data}, {start: d.start, length: d.length, draw: d.draw});
                    return JSON.stringify(_d);
                }
            },
            columns: [
                {data: 'mortgageCode'},
                {data: 'houseMortgageType'},
                {data: 'businessSource'},
                {data: 'equityHolder'},
                {data: 'approvalStatusType'},
                {data: 'approveStatusDesc'},
                {data: null}
            ],
            columnDefs: [{
                targets: [3, 5],
                render: function (data) {
                    return data ? data : '--';
                }
            }, {
                targets: 1,
                render: function (data, type, row, meta) {
                    return enums.houseMortgageType[data];
                }
            }, {
                targets: 4,
                className: 'right aligned',
                render: function (data, type, row, meta) {
                    return enums.approvalStatusTypeList[data];
                }
            }, {
                //   指定第最后一列
                targets: 6,
                className: "single line",
                render: function (data, type, row, meta) {
                    var generalBtn = '<a class="ui button mini basic" href="javascript:;" onclick="init.generalApprovalSheetById(\''+row.id+'\')"><i class="file pdf outline icon"></i>生成业务审批单</a>';
                    if (row.approvalStatusType == 'IN_EDIT') {
                        return  generalBtn+'<a class="ui button mini basic" href="/mortgage/edit?type=edit&id='+row.id+'&comeFrom=' + window.location.pathname + '"><i class="edit icon"></i>编辑</a>' +
                                '<div class="ui button mini teal" onclick="init.submit(\''+row.id+'\')">提交审批</div>'+
                                '<div class="ui button mini basic" onclick="init.cancel(\''+row.id+'\')">取消</div>'
                    } else{
                        return  generalBtn+'<a target="_blank" href="/mortgage/view?id=' + row.id +'&type=mortgage&comeFrom=' + window.location.pathname + '" class="ui button mini basic"><i class="info circle icon"></i>查看</a>'
                    }
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ]
        });
    },

    search:function(){
        dtTable.ajax.reload();
    },

    reset:function(){
        $('#applyCode').val('');
        $('#borrower').val('');
        $('#addressType').val('');
        $('#loanStatus').val('');
    },


    submit:function(id){
        $.uiDialog("你确定要提交审批？",{
            onApprove:function(){
                $(document).api({
                    on:'now',
                    action:'submit mortgage approval',
                    method:'post',
                    data:{
                        id:id
                    },
                    onSuccess:function(data){
                        $.uiAlert({type: "success", textHead: '提交审批成功', text: '', time: 3});
                        dtTable.ajax.reload();
                    },
                    onFailure: function (response) {
                        $.uiAlert({type: "danger", textHead: '提交审批失败', text: response.msg, time: 3});
                    }
                })
            }
        })
    },
    cancel:function(id){
        $.uiDialog("你确定要取消？",{
            onApprove:function(){
                $(document).api({
                    on:'now',
                    action:'cancel mortgage approval',
                    method:'post',
                    data:{
                        id:id
                    },
                    onSuccess:function(data){
                        $.uiAlert({type: "success", textHead: '取消成功', text:'', time: 3});
                        dtTable.ajax.reload();
                    },
                    onFailure: function (response) {
                        $.uiAlert({type: "danger", textHead: '取消失败', text: response.msg, time: 3});
                    }
                })
            }
        })
    },

    add_house: function () {
        $(document).on('click', '.js_addHouse', function () {
            var htm = $('.js_houseList:eq(0)').clone();
            htm.find('input,select').val('');
            htm.find('.js_removeHouse').removeClass('ks-hidden');
            htm.find('input[name="startBorrowingTime"]').dateRangePicker({separator: '~'});
            $('#editForm').append(htm);
        });
    },
    add_user: function () {
        $(document).on('click', '.js_addUser', function () {
            var htm = $('.js_userList:eq(0)').clone().removeClass('margin50t');
            htm.find('input,select').val('');
            htm.find('.js_userRemove').removeClass('ks-hidden');
            $(this).parents('.js_houseList').append(htm);
        })
    },
    del_house: function () {
        $(document).on('click','.js_removeHouse',function(){
            $(this).parents('.js_houseList').remove();
        })
    },
    del_user: function () {
        $(document).on('click','.js_userRemove',function(){
            $(this).parents('.js_userList').remove();
        })
    },

    channelSearch:function(){
        $("#businessSearch").search({
            apiSettings: {
                method: "post",
                url: '/product/get_user' + '?keyWord={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            onSelect: function (data) {
                $("#businessSearch input[name='businessId']").val(data.id);
            }
        });

        $("#editForm input[name='businessName']").on('input propertychange',function(){
            $("#editForm input[name='businessId']").val('')
        });
    },

    getDetail:function(callback){
        $(document).api({
            on:'now',
            method:'post',
            action:'get mortgage detail',
            data:{
                id:utils.getUrlParam('id')
            },
            onSuccess:function(data){
                var data = data;
                data.startTime = function(){
                    return moment(this.startBorrowingTime).format('YYYY-MM-DD')
                };
                data.endTime = function(){
                    return moment(this.endBorrowingTime).format('YYYY-MM-DD')
                };

                if (data.channel=='ZY') {
                    data.saleId = function(){
                        return this.businessId
                    };
                    data.saleName = function(){
                        return this.businessName
                    };
                }
                if (data.channel=='QD') {
                    data.channelId = function(){
                        return this.businessId
                    };
                    data.channelName = function(){
                        return this.businessName
                    };
                }

                var $editTemplate = utils.render('#editTemplate',data);
                $('#editForm').html($editTemplate)

                $('select').each(function(){
                    $(this).val($(this).attr('value'))
                })

                if ($('select[name=channel]').val()=='QD') {
                    $('#businessSearch input').attr('disabled', true);
                    $('#businessSearchDiv').addClass('ks-hidden');
                } else {
                    $('#channelSearch input').attr('disabled', true);
                    $('#channelSearchDiv').addClass('ks-hidden');
                }
                callback();

            },
            onFailure: function (response) {
                $.uiAlert({type: "danger", textHead: '获取数据失败', text: response.msg, time: 3});
            }
        })
    },

    add_channel_change_event: function() {
        $('select[name=channel]').change(function(){
            var val = $(this).val();
            changeSelect(val);
        });
    },

    generalApprovalSheetById:function(id){
        window.open("/mortgage/approval_download?id="+id,'_blank');
    },

    add:function(){
        var flag,flag2,flag3;
        $("#editForm .js_houseList").each(function (i,ele) {
            flag2 = $(ele).form(validate2).form("validate form");
            $(ele).children('.js_userList').each(function(n,ths){
                certifType = $(ths).find('select[name="certifType"]').val();
                flag3 = $(ths).form(validate3).form("validate form");
                flag = flag2 && flag3;
            });
        });

        if(flag){
            $('#editForm').form(validate1).api({
                action:'mortgage apply update',
                method:'post',
                serializeForm: false,
                beforeSend:function(settings){
                    var $this = $('#editForm');
                    var getVal = function(obj,type,name){
                        if(type == 'select'){
                            var val = obj.find('select[name='+name+']').val()
                        }else if(type == 'input'){
                            var val = obj.find('input[name='+name+'][disabled!=disabled]').val()
                        }
                        return val;
                    };

                    var data = {
                        'channel':getVal($this,'select','channel'),
                        "mortgageCode":getVal($this,'input','mortgageCode'),
                        "businessId":getVal($this,'input','businessId'),
                        "businessName":getVal($this,'input','businessName'),
                        "houseMortgageType":getVal($this,'select','houseMortgageType'),
                        "loanSubjectId":getVal($this,'select','loanSubjectId'),
                        "id":getVal($this,'input','mortgageId')
                    };
                    data.mortgageHouseList = [];

                    $('#editForm .js_houseList').each(function(n,ele){
                        var userList= [];
                        $(ele).find('.js_userList').each(function(i,ths){
                            var $this = $(ths);
                            userList.push({
                                'name':getVal($this,'input','name'),
                                'homeAddress':getVal($this,'input','homeAddress'),
                                'certifType':getVal($this,'select','certifType'),
                                'certificateNo':getVal($this,'input','certificateNo'),
                                'id':getVal($this,'input','userId')
                            })
                        });

                        var $this = $(ele);
                        data.mortgageHouseList.push({
                            'maximumLoanAmount':getVal($this,'input','maximumLoanAmount'),
                            'loanInterestRate':getVal($this,'input','loanInterestRate'),
                            'borrowingTime':getVal($this,'input','startBorrowingTime'),
                            'internalEvaluationValue':getVal($this,'input','internalEvaluationValue'),
                            'id':getVal($this,'input','houseListId'),
                            'house':{
                                'housePropertyNumber':getVal($this,'input','housePropertyNumber'),
                                'address':getVal($this,'input','address'),
                                'area':getVal($this,'input','area'),
                                'id':getVal($this,'input','houseId'),
                                'equityHolderList':userList
                            }
                        })
                    });
                    settings.data = JSON.stringify(data);
                    for(i in settings.data){
                        var val = settings.data[i];
                        settings.data[i] = $.trim(val);
                    }
                    return settings;
                },
                onSuccess: function (response) {
                    $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 1,onClosed:function(){
                        window.location.href='/mortgage/index'
                    }});
                },
                onFailure: function (response) {
                    $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
                }
            });
            $('#editForm').submit();
        }

    }

};
init.list();
var type = utils.getUrlParam('type');
if(type =='add'){
    $('input[name="startBorrowingTime"]').dateRangePicker({separator: '~'});
    $('#titleName').html('新增房产抵押');
    init.channelSearch();
    init.add_user();
    init.add_house();
    init.del_house();
    init.del_user();
    init.add_channel_change_event();
    $('#channelSearch input').attr('disabled', true);
    searchChannel('channelSearch');
}else if(type =='edit'){
    $('#titleName').html('编辑房产抵押');
    init.getDetail(function(){
        init.channelSearch();
        init.add_user();
        init.add_house();
        init.del_house();
        init.del_user();
        init.add_channel_change_event();
        searchChannel('channelSearch');
        $('input[name="startBorrowingTime"]').each(function(){
            $(this).dateRangePicker({separator: '~'});
        })
        $('.js_houseList:eq(0) .js_removeHouse').addClass('ks-hidden')
        $('.js_houseList').each(function(){
            $(this).find('.js_userRemove:eq(0)').addClass('ks-hidden')
        })
    });
}



function changeSelect(val) {

    if (val == 'ZY') {
        $('#businessSearchDiv').removeClass('ks-hidden');
        $('#channelSearchDiv').addClass('ks-hidden');
        $('#businessSearch input').attr('disabled', false);
        $('#channelSearch input').attr('disabled', true);
        return;
    }
    if (val == 'QD') {
        $('#channelSearchDiv').removeClass('ks-hidden');
        $('#businessSearchDiv').addClass('ks-hidden');
        $('#channelSearch input').attr('disabled', false);
        $('#businessSearch input').attr('disabled', true);
        return;
    }
}

function searchChannel(id){
    $('#'+id).search({
          apiSettings: {
              method: "post",
              url: '/channel/list_channel_name' + '?channelName={query}&channelType=1'
          },
          fields: {
              results: 'data',
              title: 'name',
              description: 'fullName'
          },
          onSelect: function (data) {
              $("#channelSearch input[name='businessId']").val(data.id);
          },onResults: function(data) {

          }
     });
}