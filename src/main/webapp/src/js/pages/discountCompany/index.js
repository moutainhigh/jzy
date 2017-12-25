/**
 * Created by zhangyy04 on 2017/6/15.
 */
var formQuery = {
    data: {
        name : '',
    },
    reset: function () {
        this.data = {
            name : '',
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
            "url":$(document).api.settings.api['get borrower list'],
            "type":'POST',
            "data": function (d) {
                var data = {};
                data.name = $('#name').val();
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            },
            "dataType":'json',
        },
        columns: [
            {data: 'name'},
            {data: 'certifNumber'},
            {data: 'status'}
        ],
        columnDefs:[{
            targets:[0,1],
            className:'',
            render: function (data, type, row, meta) {
                if(data!=undefined && data!=null){
                    if(data==="--" || data===""){
                        return "--"
                    }else{
                        return data
                    }
                }else{
                    return "--"
                }
            }
        },{
            targets:2,
            className:"center aligned",
            render: function (data, type, row, meta) {
                if(data){
                    return data == 'DISABLED'?'失效':'生效';
                }else{
                    return '--';
                }
            }
        },{
            targets:3,
            className:"center aligned",
            render:function(data,type,row,meta){
                return  '<a class="ui mini basic button" href="/borrower/edit?id='+row.id+'">'+
                        '<i class="edit icon"></i>编辑'+
                        '</a>'+
                        '<a class="ui mini basic button" href="/borrower/view?id='+row.id+'">'+
                        '<i class="Info Circle icon"></i>查看' +
                        '</a>';
            }
        }
        ],
        "iDisplayLength": 10,
        initComplete: function () {
            $(".right.aligned.eight.wide.column").append("<a class='ui teal small button ' href='/borrower/add'><i class='plus icon'></i>新增</a>");
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
