/**
 * Created by yangzb01 on 2017/4/13.
 */
/*
 * 列表初始化
 *
 * */
function initChannel(){
    dtTable = $("#channelList").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax": {
            "url": '/channel/list_by_channel_name',
            "data":function(d){
                var data = {};
                data.channelType = '1';
                data.channelName = $('#channelName').val();
                data.managerId = $('#channelID').val();
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            },
            "type": "post",
        },
        columns: [
            {data: 'code'},
            {data: 'name'},
            {data: null},
            {data: null},
            {data: null},
            {data: null},
            {data: null},
            {data: null}
        ],
        columnDefs:[{
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
            className:'right aligned',
            render: function (data, type, row, meta) {
                if(data.cooperationAmount || data.cooperationAmount==0){
                    return accounting.formatMoney(data.cooperationAmount,'',2,',','.');;
                }else{
                    return '--';
                }
            }
        },{
            targets:4,
            className:'right aligned',
            render: function (data, type, row, meta) {
                if((data.depositLimit || data.depositLimit==0)){
                    return accounting.formatMoney(data.depositLimit,'',2,',','.');
                }else{
                    return '0.00';
                }
            }
        },{
            targets:5,
            className:'right aligned',
            render: function (data, type, row, meta) {
                if((data.residualAmount || data.residualAmount==0)){
                    return accounting.formatMoney(data.residualAmount,'',2,',','.');
                }else{
                    return '0.00';
                }
            }
        },{
            targets:6,
            className:"center aligned",
            render: function (data, type, row, meta) {
                if(data.status){
                    return data.status == 'DISABLED'?'失效':'生效';
                }else{
                    return '--';
                }
            }
        },{
            targets:7,
            className:"center aligned",
            render: function (data, type, row, meta) {
                return  '<a class="ui mini basic button" href="/channel/edit?id='+data.id+'">'+
                            '<i class="edit icon"></i>编辑</div>'+
                        '</a>'+
                        '<a class="ui mini basic button" href="/channel/view?id='+data.id+'">'+
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
initChannel();

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
            $(this).find('input[name="channelID"]').val(result.id)
        },
    })

    $('.js-saleSearch .js-input').on('input propertychange', function () {
        $(this).prev('input').val($(this).val());
    })
}
initSaleSearch();

//列表查询
$('#search').on('click',function(){
    dtTable.ajax.reload();
});




