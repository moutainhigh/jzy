/**
 * Created by yangzb01 on 2017/4/17.
 */

function initPartner(){
    dtTable = $("#partnerList").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax": {
            "url": '/channel/list_by_channel_name',
            "data":function(d){
                var data = {};
                data.channelType = 0;
                data.channelName = $('#name').val();
                data.managerId = $('#managerId').val();
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            },
            "type": "post",
        },
        columns: [
            {data: null},
            {data: 'name'},
            {data: null},
            {data: null},
            {data: null}
        ],
        columnDefs:[{
            targets:0,
            render: function (data, type, row, meta) {
                if(data.fullName){
                    return data.fullName;
                }else{
                    return '--';
                }
            }
        },{
            targets:2,
            render: function (data, type, row, meta) {
                if(data.manager){
                    return data.manager;
                }else{
                    return '--';
                }
            }
        },{
            targets:3,
            className:"center aligned",
            render: function (data, type, row, meta) {
                if(data.status){
                    return data.status == 'DISABLED'?'失效':'生效';
                }else{
                    return '--';
                }
            }
        },{
            targets:4,
            className:"center aligned",
            render: function (data, type, row, meta) {
                return  '<a class="ui mini basic button" href="/channel/partnerEdit?id='+row.id+'">'+
                        '<i class="edit icon"></i>编辑'+
                        '</a>'+
                        '<a class="ui mini basic button" href="/channel/partnerView?id='+row.id+'">'+
                        '<i class="Info Circle icon"></i>查看' +
                        '</a>';
            }
        }],
        "iDisplayLength": 20,
        "aLengthMenu": [
            [20],
            [20]
        ]
    });
}
initPartner();

//列表查询
$('#search').on('click',function(){
    dtTable.ajax.reload();
});

/*业务员查询*/
function initSaleSearch() {
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
            $(this).find('input[name="managerId"]').val(result.id)
        },
    })

    $('.js-saleSearch .js-input').on('input propertychange', function () {
        $(this).prev('input').val($(this).val());
    })
}

initSaleSearch()