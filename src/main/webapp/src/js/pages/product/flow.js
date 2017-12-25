/**
 * Created by pengyueyang on 2016/12/19.
 */
$(document).ready(function () {
    $("#viewFlowBtn").on("click",function () {
        viewFlow($("#updateProductId").val(),"viewFlowContent");
        $("#productFlowModalView").modal("show");
    });

    $("#addFlowBtn").on("click",function () {
        addFlow();
    });

    $("#copyFlowSubmit").on("click",function (){
        if ($("#flowProductId").val()=="") {
            $.uiAlert(
                {
                    type: "danger",
                    textHead: '流程复制错误',
                    text: "请选择需要复制的流程产品",
                    time: 1
                });
        return;
        }
        copyFlow($("#updateProductId").val(),$("#flowProductId").val());
    });

    $('.js-productSearch').search({
                                  apiSettings: {
                                      method: "post",
                                      url: '/product/search_product_flow' + '?keyWord={query}'
                                  },
                                  fields: {
                                      results: 'data',
                                      title: 'name',
                                      description: 'code'
                                  },
                                  onSelect: function (result, response) {
                                      $("#flowProductId").val(result.id)
                                      viewFlow(result.id,"addFlowContent");
                                  },
                              })

    $('.js-productSearch .js-input').on('input propertychange', function () {
        $(this).prev('input').val("");
    })



});




function viewFlow(productId,contentId) {
    $(document).api({
                        on:"now",
                        url:"/flow_control/fetch_by_productId",
                        data: {productId: productId},
                        method:"post",
                        onSuccess:function(_data){
                            var data = _data.data;
                            var contentObj = $(("#"+contentId));
                            contentObj.html("");
                            if (data && data.length>0) {
                                contentObj.append("<span class='ui  teal label'>开始</span>").append("<i class='grey right arrow icon divider'></i>");
                                    for (var i=0; i<data.length; i++){
                                        contentObj.append("<span class='ui basic   label'>"+data[i].name+" </span>").append("<i class='grey right arrow icon divider'></i>");
                                    }
                                contentObj.append("<span class='ui  teal label'>结束</span>");
                            } else {
                                contentObj.html("尚未配置流程");
                            }
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


function addFlow() {
    $('#productSearch').val("");
    $("#addFlowContent").html("");
    $("#productFlowModal").modal("show");
}

function copyFlow(productId,flowProductId) {
            $(document).api({
                                on: "now",
                                url: "/product/add_flow",
                                data: {productId: productId, flowProductId: flowProductId},
                                method: "post",
                                onSuccess: function (data) {
                                    $.uiAlert(
                                        {
                                            type: "success",
                                            textHead: '复制流程成功',
                                            text: data.msg,
                                            time: 1
                                        });
                                    $("#productFlowModal").modal("hide");
                                }, onFailure: function (data) {
                    $.uiAlert(
                        {
                            type: "danger",
                            textHead: '后台处理错误',
                            text: data.msg,
                            time: 1
                        });
                }
                            });
}
