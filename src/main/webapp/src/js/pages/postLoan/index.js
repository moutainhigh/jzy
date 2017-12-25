/**
 * Created by lw on 2016/12/12.
 */
var  productTypeCascade = TwoCascade({parentSelect:$("#productType"),subSelect:$("#product")})
productTypeCascade.create();
var formQuery = {
    data: {
        agencyId: '',
        orgId: '',
        businessLine: '',
        borrower: '',
        repayDate: moment(new Date()).format('YYYY-MM-DD') + " ~ " + moment(new Date()).format('YYYY-MM-DD'),
        productType: '',
        product: '',
        channelId:''
    },
    reset: function () {
        this.data = {
            agencyId: '',
            orgId: '',
            businessLine: '',
            borrower: '',
            repayDate: '',
            productType: '',
            product: '',
            channelId:''
        }
    }
};
var dtTable;
function initPostLoanList() {
    dtTable = $("#postLoanTable").DataTable({
    serverSide: true,//服务端分页
    searching: false,//显示默认搜索框
    ordering: false,//开启排序
    autoWidth: true,//自适应宽度
    "ajax": {
        "url":'/post_loan/list_post_loan',
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
            return row.code == null ? '' : row.code;
        }},{
        targets:1,
        render:function(data,type,row,meta){
            return row.productTypeName == null ? '' : row.productTypeName;
        }
    }, {
            targets:2,
            render:function(data,type,row,meta){
                return row.productName == null ? '' : row.productName;
            }
        },{
        targets:3,
        render:function(data,type,row,meta){
            return row.agencyName == null ? '' : row.agencyName;
        }
    },{
        targets:4,
        render:function(data,type,row,meta){
            return row.saleName == null ? '' : row.saleName;
        }
    },{
        targets:5,
        render:function(data,type,row,meta){
            return row.borrserName == null ? '' : row.borrserName;
        }
    },{
        targets:6,
        render:function(data,type,row,meta){
            return row.dueDate == null ? '' : moment(row.dueDate).format('YYYY-MM-DD');
        }
    },{
        targets:7,
        className:'right aligned',
        render:function(data,type,row,meta){
            return row.amount == null ? '' : accounting.formatNumber(row.amount,2,",");
        }
    },{
        targets:8,
        className:'right aligned',
        render:function(data,type,row,meta){
            return row.interest == null ? '' : accounting.formatNumber(row.interest,2,",");
        }
    },{
        targets:9,
        render:function(data,type,row,meta){
            return row.period == null ? '' : row.period;
        }
    },{
        targets:10,
        render:function(data,type,row,meta){
            return row.status == null ? '' : enums.Loan_repay_status[row.status];
        }
    },{
        className:"single line",
        targets:11,
        render:function(data,type,row,meta){
            return '<a class="ui button mini basic" href="javascript:;" onclick="fetchById(\''+row.id+'\',\''+row.loanId+'\')"><i class="info circle icon"></i>查看</a>'
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
        var startDate =moment(new Date()).format('YYYY-MM-DD');
        var endDate = moment(new Date()).format('YYYY-MM-DD');
        $("#repayDate").val(startDate+" ~ "+endDate);
        //查询
        $(".js-searchForm").form({
            onSuccess: function (e, fields) {
                e.preventDefault();
                formQuery.reset();
                $.extend(formQuery.data, fields);
                dtTable.ajax.reload();
            }
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
    initPostLoanList();
    $("#repayDate").dateRangePicker({separator: '~'});
}


initPage();

function fetchById(repayId,id) {
    window.open("/business_apply/detail?id="+id+"&repayId="+repayId+"&type=LOANED"+"&tab=postLoan"+"&comeFrom="+window.location.pathname)
}

function getManager(obj){
    $('#'+obj).search({
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
                              var $this = $('#'+obj);
                              $this.find('input').attr('readonly',true);
                              $this.find('.js_search').addClass('close');
                              $this.find('.js_search').css('pointer-events','auto');
                              $("#channelId").val(data.id);
                          },onResults: function(data) {}
                      });
}
getManager('managerChannelSearch');
$(document).on('click','.js_search.close',function(){
    $(this).css('pointer-events','none');
    $(this).siblings('input').attr('readonly',false);
    $(this).siblings('input').val("");
    $(this).removeClass('close');
    $("#channelId").val("");
});

$(document).on('click','#resetBnt',function(){
    productTypeCascade.reset();
    $("#repayDate").val(moment(new Date()).format('YYYY-MM-DD') + " ~ " + moment(new Date()).format('YYYY-MM-DD'));
    $(".js_search.close").each(function(){
        $(this).click();
    });
});

