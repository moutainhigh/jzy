/**
 * Created by yangzb01 on 2017/4/14.
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
//获取树的数据
function initData(obj,channelId){
    if(!channelId)
        channelId = '';
    $(document).api({
        on: "now",
        method: 'post',
        action: "get product tree",
        data: {
            channelId: channelId,
            channelType:'1'
        },
        onSuccess: function (data) {
            ztreeObj = $.fn.zTree.init($('#'+obj), settingView , data.data);
            ztreeObj.expandAll(true);
        }
    });
}

$('#cooperationProductType').on('click',function(){
    $('#modal_product_add').modal('show');
});

$('#cooperationProductTypeView').on('click',function(){
    $('#modal_product_view').modal('show');
});

//赋值
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
    var ProductTypeStr = JSON.stringify(nodes);
    $("#form-addChannel").find('input[name="cooperationProductType"]').val(ProductTypeStr);
    $('#modal_product_add').modal('hide');
});

