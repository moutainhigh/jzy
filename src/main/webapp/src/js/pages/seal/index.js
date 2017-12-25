/**
 * Created by lw on 2016/12/12.
 */
var formQuery = {
    data: {
        bussinessNo: '',
        borrower: '',
        s_loanDate: '',
        s_used: '',
    },
    reset: function () {
        this.data = {
            bussinessNo: '',
            borrower: '',
            s_loanDate: '',
            s_used: '',
        }
    }
};
var dtTable;
function initSealList() {
    dtTable = $("#sealTable").DataTable({
    serverSide: true,//服务端分页
    searching: false,//显示默认搜索框
    ordering: false,//开启排序
    autoWidth: true,//自适应宽度
    "ajax": {
        "url":'/seal/seal_list',
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
            return row.code == null ? '--' : row.code;
        }},{
        targets:1,
        render:function(data,type,row,meta){
            return row.agencyName == null ? '--' : row.agencyName;
        }
    },{
        targets:2,
        render:function(data,type,row,meta){
            return row.saleName == null ? '--' : row.saleName;
        }
    },{
        targets:3,
        render:function(data,type,row,meta){
            return row.borrserName == null ? '--' : row.borrserName;
        }
    },{
        targets:4,
        render:function(data,type,row,meta){
            return row.loanDate == null ? '--' : moment(row.loanDate).format('YYYY-MM-DD');
        }
    },{
        targets:5,
        render:function(data,type,row,meta){
            return row.loanStatus == null ? '--' : row.loanStatus;
        }
    },{
        targets:6,
        render:function(data,type,row,meta){
            return row.userTime == null ? '--': moment(row.userTime).format('YYYY-MM-DD');
        }
    },{
        targets:7,
        render:function(data,type,row,meta){
            return row.status == null ? '未用印' : enums.seal_status[row.status];
        }
    },{
        className:"single line",
        targets:8,
        render:function(data,type,row,meta){
                var generalBtn = 'SHULOUPLATRRC'.indexOf(row.productTempType)>=0?'':'<a class="ui button mini basic" href="javascript:;" onclick="generalApprovalSheetById(\''+row.loanId+'\')"><i class="file pdf outline icon"></i>生成业务审批单</a>';
                if(row.status != null && ('UNUSED' == row.status || 'PREUSED' == row.status)){
                    return generalBtn+'<a class="ui button basic mini" href="javascript:;" onclick="updateSeal(\'' + row.loanId + '\',\''+ row.id +'\')"><i class="edit icon"></i>用印</a>'
                }else if(row.status == null){
                    return generalBtn+'<a class="ui button basic mini" href="javascript:;" onclick="updateSeal(\'' + row.loanId + '\')"><i class="edit icon"></i>用印</a>'
                }else{
                    return generalBtn+'<a class="ui button basic mini" href="javascript:;" disabled="true"><i class="edit icon"></i>已用印</a>'
                }

        }
    }
    ],
           "iDisplayLength": 10,
           "aLengthMenu": [
               [10],
               [10]
           ],
           "dom": "<'ui grid'<'row'<'eight wide column'><'right aligned eight wide column'>><'row dt-table'<'sixteen wide column'tr>><'row'<'seven wide column'i><'right aligned nine wide column'p>>>"
});

}

var searchForm = {
    init: function () {
        var othis = this;
        othis.initAgencySearch();
        othis.initSaleSearch();
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
                $(this).find('input[name="s_saleName"]').val(result.name)
            },
        })

        $('.js-saleSearch .js-input').on('input propertychange', function () {
            $(this).prev('input').val($(this).val());
        })
    },
    initAgencySearch: function () {
        $('.js-agcSearch').search({
            apiSettings: {
                method: "post",
                url: $(document).api.settings.api['search agency'] + '?search={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            // minCharacters : 3
            onSelect: function (result, response) {
                $(this).find('input[name="agencyId"]').val(result.id)
            },
        })

        $('.js-agcSearch .js-input').on('input propertychange', function () {
            $(this).prev('input').val($(this).val());
        })
    },

}

function initPage(){
    searchForm.init();
    initSealList();
    $("#s_loanDate").dateRangePicker({});

}


initPage();

function updateSeal(loanId,id) {
    get_seal_detail(loanId,id)
}
function generalApprovalSheetById(loanId){
    window.open("/seal/document_download?loanId="+loanId,'_blank');
}

function get_seal_detail(loanId,id) {
    var type1 = ['RRC','SHULOU','GERENDAI','CHEDAI'],
        type2 = ['HONGBEN'],
        type3 = ['PIAOJU','YINPIAO'],
        type4 = ['BAOLI'];
        resetHtml = ['bill_info','house_info','borrower_type_detail','hongben_borrower_detail','baoli_info'];//添加数据容器id
    $(document).api({
        on: "now",
        method: 'post',
        action: "seal detail",
        data: {
            loanId: loanId
        },
        onSuccess:function(data){
            for(i in resetHtml){
                $('#'+resetHtml[i]).html("");
            }
            $('.js_seal_info').html("");
            var _data = data.info;
            var productType = _data.productType.productTempType;
            // var productType = 'BAOLI'


            if(type1.indexOf(productType) > -1){
                var $this = $('#edit_seal_fromElse .js_seal_info');
                $this.each(function(n,ele){
                    var name = $(ele).data('name');

                    if(_data[name]){
                        if(name =='calculateMethodAboutDay'){
                            $(ele).html('['+enums.calculateMethodAboutDay[_data[name]]+']');
                        }else if(name =='masterBorrserIDType'){
                            $(ele).html(enums.certifType[_data[name]]+'/');
                        }else if(name =='borrserIDType'){
                            $(ele).html(enums.certifType[_data[name]]+'/');
                        }else if(name =='amount'){
                            $(ele).html(accounting.formatNumber(_data[name], 2,","));
                        }else{
                            $(ele).html(_data[name]);
                        }
                    }

                });

                if(_data.loanBorrowerList && _data.loanBorrowerList !=''){
                    $(".js_borrower_type_tmp").removeClass("ks-hidden");
                    var borrowerList = _data.loanBorrowerList;
                    var borrower_len = borrowerList.length;
                    var tmp_len = $('.js_borrower_type_tmp').length;

                    if(borrower_len > 1 && tmp_len != borrower_len)
                        while(borrower_len>1){
                            $('#borrower_type_detail').append($('.js_borrower_type_tmp:eq(0)').clone())
                            borrower_len--
                        }

                    $('#type_detail .js_borrower_type_tmp').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');
                            //if(borrowerList[n].master==false){
                                if(name =='masterBorrserID'){
                                    $(ths).html(borrowerList[n].certifNumber);
                                }else if(name =='masterBorrserIDType'){
                                    $(ths).html(enums.certifType[borrowerList[n].certifType]+'/');
                                }else if(name =='masterBorrserName'){
                                    $(ths).html(borrowerList[n].name);
                                }
                            //}

                            //$(ths).html(borrowerList[n][name]);
                        })
                    });
                }else {
                    $(".js_borrower_type_tmp").addClass("ks-hidden");
                }


                $('#edit_seal_fromElse input[name="id"]').val(id);
                $('#edit_seal_fromElse input[name="loanId"]').val(loanId);
                $("#edit_seal_else").modal("show");
            }else if(type2.indexOf(productType) > -1){  //红本弹窗
                var $this = $('#edit_seal_fromHongben .js_seal_info');
                $this.each(function(n,ele){
                    var name = $(ele).data('name');

                    if(_data[name]){
                        if(name =='calculateMethodAboutDay'){
                            $(ele).html(enums.calculateMethodAboutDay[_data[name]]);
                        }else if(name =='masterBorrserIDType'){
                            $(ele).html(enums.certifType[_data[name]]+'/');
                        }else if(name =='borrserIDType'){
                            $(ele).html(enums.certifType[_data[name]]+'/');
                        }else if(name =='amount'){
                            $(ele).html(accounting.formatNumber(_data[name], 2,","));
                        }else{
                            $(ele).html(_data[name]);
                        }
                    }

                });

                if(_data.houseInfoList && _data.houseInfoList !=''){
                    var houseList = _data.houseInfoList;
                    var house_len = houseList.length;
                    var house_tmpLen = $('.js_house_tmp').length;
                    if(house_len > 1 && house_tmpLen !=house_len)
                        while(house_len>1){
                            $('#house_info').append($('.js_house_tmp:eq(0)').clone())
                            house_len--
                        }

                    $('#hongben_detail .js_house_tmp').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');
                            $(ths).html(houseList[n][name]);
                        })
                    });
                }

                if(_data.loanBorrowerList && _data.loanBorrowerList !=''){
                    $(".js_borrower_tmp").removeClass('ks-hidden');
                    var borrowerList = _data.loanBorrowerList;
                    var borrower_len = borrowerList.length;

                    if(borrower_len > 1)
                        while(borrower_len>1){
                            $('#hongben_borrower_detail').append($('.js_borrower_tmp:eq(0)').clone())
                            borrower_len--
                        }

                    $('#hongben_detail .js_borrower_tmp').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');

                            if(name =='masterBorrserID'){
                                $(ths).html(borrowerList[n].certifNumber);
                            }else if(name =='masterBorrserIDType'){
                                $(ths).html(enums.certifType[borrowerList[n].certifType]+'/');
                            }else if(name =='masterBorrserName'){
                                $(ths).html(borrowerList[n].name);
                            }

                        })
                    });
                }else {
                    $(".js_borrower_tmp").addClass('ks-hidden');
                }

                $('#edit_seal_fromHongben input[name="id"]').val(id);
                $('#edit_seal_fromHongben input[name="loanId"]').val(loanId);
                $("#edit_seal_hongben").modal("show");
            }else if(type3.indexOf(productType) > -1){ //票据银票弹窗
                var $this = $('#edit_seal_formPiaoju .js_seal_info');
                $this.each(function(n,ele){
                    var name = $(ele).data('name');
                    if(_data[name]) {
                        if (name == 'amount') {
                            $(ele).html(accounting.formatNumber(_data[name], 2, ","));
                        } else if (name == 'billTotalAmount') {
                            $(ele).html(accounting.formatNumber(_data[name], 2, ","));
                        } else {
                            $(ele).html(_data[name]);
                        }
                    }

                });

                if(_data.billList){
                    var billList = _data.billList;
                    var bill_len = billList.length;

                    if(bill_len > 1)
                        while(bill_len>1){
                            $('#bill_info').append($('.js_bill_tmp:eq(0)').clone())
                            bill_len--
                        }

                    $('#bill_detail .js_bill_tmp').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');
                            if(billList[n] && (billList[n][name] || billList[n].loanRepay[name])){
                                $(ths).html(billList[n][name]);
                                if (name == "amount") {
                                    $(ths).html(accounting.formatNumber(billList[n].loanRepay[name], 2, ","));
                                }
                                if (name == "actualDueDate") {
                                    $(ths).html(moment(billList[n].actualDueDate).format("YYYY-MM-DD"));
                                }
                            }
                        })
                    });
                }


                $('#edit_seal_formPiaoju input[name="id"]').val(id);
                $('#edit_seal_formPiaoju input[name="loanId"]').val(loanId);
                $("#edit_seal_piaoju").modal("show");
            }else if(type4.indexOf(productType) > -1){
                //to be add
                var $this = $('#edit_seal_formBaoli .js_seal_info');
                $this.each(function(n,ele){
                    var name = $(ele).data('name');
                    if(_data[name] || _data[name] == 0){
                        if(name =='re_amount' || name == 'amount'){
                            $(ele).html(accounting.formatNumber(_data[name], 2,","));
                        }else{
                            $(ele).html(_data[name]);
                        }
                    }
                });


                if(_data.bakAccountList || _data.bakAccountList.length >1){
                    var accountList = _data.bakAccountList;
                    var bl_len = accountList.length;
                    if(bl_len > 1)
                        var htm1 = $('.js_baoli_tmp1:eq(0)').clone();
                        var htm2 = $('.js_baoli_tmp2:eq(0)').clone();
                        while(bl_len>1){
                            $('#baoli_info').append(htm1).append(htm2);
                            bl_len--
                        }

                    $('#baoli_detail .js_baoli_tmp1').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');
                            if(accountList[n][name]){
                                $(ths).html(accountList[n][name]);
                            }
                        })
                    });
                    $('#baoli_detail .js_baoli_tmp2').each(function(n,ele){
                        $(ele).find('.js_seal_info').each(function(i,ths){
                            var name = $(ths).data('name');
                            if(accountList[n][name]){
                                $(ths).html(accountList[n][name]);
                            }
                        })
                    });
                }


                $('#edit_seal_formBaoli input[name="id"]').val(id);
                $('#edit_seal_formBaoli input[name="loanId"]').val(loanId);
                $("#edit_seal_baoli").modal("show");
            }
        },
        onFailure:function(data){
            $.uiAlert({
                type: "danger",
                textHead: '获取数据错误',
                text: data.msg,
                time: 1
            });
        }
    })

}



$("#editCancelBnt").on('click', function () {
    $('#edit_seal_modal').modal("hide");
    dtTable.ajax.reload();

});
$(".close").on('click',function(){
    $("#useTime").val("");
});

$(".js_view_business").on('click',function(){
    var loanId = $(this).parent().siblings('.content').children('.ui.form').find('input[name="loanId"]').val();
    //console.log($(this).parent().siblings('.content').children('.ui.form').find('input[name="loanId"]').val());
    //console.log('/business_apply/detail?id='+loanId+'&tab=business&process=false&comeFrom='+window.location.pathname+'');
    $(".js_view_business").attr('href','/business_apply/detail?id='+loanId+'&tab=business&process=false&comeFrom='+window.location.pathname+'');
});




$(".js_edit_btn").on('click', function () {
    var status = $(this).data('status');
    $(this).parents('.ui.form').find('input[name="status"]').val(status);
    var $form = $(this).parents('.ui.form');
    var status_t = status =='USED' ? '确认用印':'拟用印'
    $.uiDialog('确认用印后，无法修改，是否'+status_t+'？',{
        onApprove:function(){
            $form.api({
                method: 'post',
                url:"/seal/update",
                serializeForm: true,
                onSuccess: function (data) {
                    $.uiAlert(
                        {
                            type: "success",
                            textHead: '修改成功',
                            text: data.msg,
                            time: 1,
                            onClosed: function () {
                                $('.ui.modal')
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
                            time: 3,
                            onClosed: function () {
                                $('.ui.modal')
                                    .modal('hide');
                                dtTable.ajax.reload();
                            }
                        });
                }
            });
            $form.submit();
        }
    });
});