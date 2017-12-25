$(function () {
    // 跳转返回影像资料列表
    $("body").on("click", ".backButton", function () {
        location.href = "/media_temp/index";//location.href实现客户端页面的跳转
    })
    // 设置影像资料Tab
    $('.viewDateMenu .item').tab();
    //初始化影像tables
    initDatatables();
})

function initDatatables(id) {
    // 初始化业务影像datatable
    init_Datatables("#y_dataTabel", "#y_addButton", "BUSINESS", "Y");
    // 初始化风控影像datatable
    init_Datatables("#f_dataTabel", "#f_addButton", "RISK", "F");
    // 初始化财务影像datatable
    init_Datatables("#c_dataTabel", "#c_addButton", "FINANCE", "C");
    // 初始化贷后影像datatable
    init_Datatables("#d_dataTabel", "#d_addButton", "POST_LOAN", "D");

}
//datatables公用方法
function init_Datatables(datatableName, addButton, mediaItemType) {
    $(document).ready(function () {
        var table = $(datatableName).DataTable({
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth: true,//自适应宽度
            bDestory: true,
            bRetrieve: true,
            serverSide: false,//服务端分页
            iDisplayLength: 50,
            "ajax": {
                "url": $(document).api.settings.api['query mediaItem'],
                "type": "POST",
                "data": function (d) {
                    d.id = $("#id").val();
                    d.type = mediaItemType;
                }
            },
            columns: [

                {
                    data: 'code',

                },
                {
                    data: 'name',

                },
                // {
                //     data: 'mediaItemType',
                //
                // },
                {
                    data: 'required',
                    render: function (data) {
                        if (data == 1) {
                            return '是'
                        } else if(data==0){
                            return'否'
                        }
                    },

                }
            ]
        });
        // //添加行
        // $(addButton).on('click', function () {
        //     table.row.add([
        //     ]).draw();
        // });
        //
        // // 删除行
        // $(datatableName + ' tbody').on('click', 'button.js-del', function (e) {
        //     e.preventDefault();
        //     if (table.data().length > 1) {
        //         table.row($(this).parents('tr')).remove().draw();
        //     }
        // })
    });

}
