/**
 * Created by wangqx on 2016/12/19.
 */

var setting = {
    check: {
        enable: true
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    view: {
        showIcon: false
    },
    callback: {
        //设置不能收缩
        // beforeCollapse : function () {
        //     return false;
        // }
    }
};

var settingView = {
    check: {
        enable: true
    },
    data: {
        simpleData: {
            enable: true
        }
    },
    view: {
        showIcon: false
    },
    callback: {
        //设置不能收缩
        // beforeCollapse : function () {
        //     return false;
        // }
    }
};

var ztreeObj;

$(document).ready(function () {
    $("#addOrganizeBtn").on("click",function () {
        initProductOrganize($("#updateProductId").val());
    });

    $("#viewOrganizeBtn").on("click",function () {
        initProductOrganizeView($("#updateProductId").val());
    });


    $("#addOrganizeSubmit").on("click",function () {
        var treeNodes = ztreeObj.getCheckedNodes(true);
        var nodes = new Array();
        if (treeNodes && treeNodes.length>0) {
            for (var i=0; i<treeNodes.length; i++) {
                var node = new Object();
                node.id = treeNodes[i].id;
                node.name = treeNodes[i].name;
                node.checked = true;
                node.open = false;
                node.pId = treeNodes[i].pId;
                nodes.push(node);
            }
        }
        addProductOrganize($("#updateProductId").val(),nodes);
    });
});



function initProductOrganizeView(productId) {
    $(document).api({
                        on:"now",
                        url:"/product/get_organize_product",
                        data: {productId: productId},
                        method:"post",
                        onSuccess:function(data){
                            $("#organizeTreeView").html("");
                            var tree = $.fn.zTree.init($("#organizeTreeView"), settingView , data.data);
                            tree.expandAll(true);
                            $("#productOrganizeModalView").modal("show");
                        },onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '后台处理错误',
                    text: data.msg,
                    time: 1
                });
        }});
}


function initProductOrganize(productId) {
    $(document).api({
                        on:"now",
                        url:"/product/get_organize_product",
                        data: {productId: productId},
                        method:"post",
                        onSuccess:function(data){
                            $("#organizeTree").html("");
                            ztreeObj = $.fn.zTree.init($("#organizeTree"), setting , data.data);
                            $("#productOrganizeModal").modal("show");
                        },onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '后台处理错误',
                    text: data.msg,
                    time: 1
                });
        }});
}

function addProductOrganize(productId,nodes) {
    $(document).api({
            on:"now",
            url:"/product/add_organize_product",
            data: {productId: productId,nodes:JSON.stringify(nodes)},
            method:"post",
            onSuccess:function(data){
                $.uiAlert(
                    {
                        type: "success",
                        textHead: '关联机构成功',
                        text: data.msg,
                        time: 1
                    });
                $("#productOrganizeModal").modal("hide");
            }, onFailure: function (data) {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '后台处理错误',
                    text: data.msg,
                    time: 1
                });
        }});
}
