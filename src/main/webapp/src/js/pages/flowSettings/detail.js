/**
 * Created by yangzb01 on 2017-08-29.
 */

var init ={

    /**
    * 流程类型查询
    * */
    getFlowType:function(callBack){
        $(document).api({
            on:'now',
            action:'get flow type',
            method:'post',
            onSuccess:function(data){
                for(i in data){
                    var value = data[i].name;
                    var name = data[i].desc;
                    $('#js_flowType').append('<option value='+value+'>'+name+'</option>');
                }
                if(typeof callBack == 'function' && callBack){
                    callBack()
                }         
            },
            onFailure: function (data) {
                $.uiAlert({
                    type: "danger",
                    textHead: '流程类型获取失败',
                    text: data.msg,
                    time: 1
                });
            }
        })
    },

    /**
     * 业务模块查询
     * */
    flowModuleList:function(){
        $(document).api({
            on:'now',
            action:'get flow modal',
            method:'post',
            onSuccess:function(data){
                for(i in data){
                    var value = data[i].name;
                    var name = data[i].desc;
                    $('.js_modal').append('<option value='+value+'>'+name+'</option>');
                }
            },
            onFailure: function (data) {
                $.uiAlert({
                    type: "danger",
                    textHead: '业务模块获取失败',
                    text: data.msg,
                    time: 1
                });
            }
        })
    },

    /*
    * 添加模块
    */
    addModal:function(){
        $('#add_flowModal').on('click',function(){
            init.cloneModal('prepend');
            init.postSearch();
        });

        $(document).on('click','.add_flowNode',function(){
            var $this = $(this).parents('.js_demo_box').find('tbody');
            init.cloneNode($this);
            init.postSearch();
            init.addPoint($this);
        });
    },

    /*
    * 追加模块
    */
    afterAddMoadal:function(){
        $(document).on('click','.js_afterAddMoadal',function(){
            $(this).parents('.js_demo_box').after($('.js_demo').clone().removeClass('ks-hidden js_demo').addClass('js_modalList'));
            init.postSearch();
        })
    },

    //克隆模块 default append
    cloneModal:function(P){
        var htm = $('.js_demo').clone().removeClass('ks-hidden js_demo').addClass('js_modalList')
        if (P == 'prepend') {
            $('#flow_box').prepend(htm)
        }else{
            $('#flow_box').append(htm)
        }
    },


    //克隆节点
    cloneNode:function($this){
        $this.append($('.js_node_demo').clone().removeClass('ks-hidden js_node_demo'))
    },

    //删除（模块|节点）
    remove:function(){
        $(document).on('click','.js_remove',function(){
            $(this).parents('.js_demo_box').remove();
        });

        $(document).on('click','.js_node_remove',function(){
            var $this = $(this).parents('tbody');
            $(this).parents('.js_node').remove();
            init.addPoint($this)
        });
    },

    postSearch:function(){
        //岗位查询
        $('.js-postSearch').search({
            apiSettings: {
                method: "post",
                url: $(document).api.settings.api['get roleListByName'] + '?name={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            // minCharacters : 3
            onSelect: function (result, response) {
                $(this).find('input[name="postId"]').val(result.id)
                $(this).find('input[name="pointRole"]').attr('value',result.name)
            },

        })
        $('.js-postSearch  .js-input').on('input propertychange', function () {
            $(this).prev('input').val("");
        })
    },

    /*
    *增加节点
    */
    addPoint:function($this){
        var $this = $this.find('.js_node');
        $this.each(function(n,ele){
            $(this).find('.node_num').html(n+1);
        })
    },


    getDetail:function(){
        //获取填充表单的信息
        var id = utils.getUrlParam('id')
        $(document).api({
            on:'now',
            action:'flow detail',
            method:'post',
            data: {
                'id':id,
            },
            onSuccess:function(data){
                $('#editForm').form('set values',{
                    'flowType':data.flowType,
                    'flowName':data.flowName,
                    'flowDesc':data.flowDesc,
                    'id':data.id
                });

                init.getProductList(data.flowType,id,function(){
                    $('#productList').dropdown('set selected',data.productId.split(','))
                });
                
                var modal_list = data.flowConfigureRelations;

                //渲染html
                for(var n=0;n<modal_list.length;n++){
                    var modals = modal_list[n];
                    init.cloneModal()//clone
                    $('.js_modal:eq('+n+') option[value='+modals.moduleType+']').attr('selected',true);
                    $('.js_modalList:eq('+n+') input[name="modalId"]').val(modals.id);
                    $('.js_modalList:eq('+n+') input[name="tempId"]').val(modals.template.id);

                    var node_list = modals.template.flowControlItems;
                    var $thisModal = $('.js_modalList .tbody:eq('+n+')');
                    for(var i=1;i<node_list.length;i++){
                        init.cloneNode($thisModal);//clone
                    }

                    for(var i=0;i<node_list.length;i++){
                        var Val = node_list[i];
                        var $this = $('.js_modalList .tbody:eq('+n+') .js_node:eq('+i+')');
                        $this.find('input[name="pointId"]').val(Val.id);
                        $this.find('input[name="pointName"]').val(Val.name);
                        $this.find('input[name="postId"]').val(Val.organizeId);
                        $this.find('input[name="pointRole"]').val(Val.organizeName);
                        $this.find('input[name="approveAmount"]').val(Val.approveAmount);
                        $this.find('select[name="enterprise"] option[value='+Val.enterprise+']').attr('selected',true);
                    }
                }
                init.postSearch();
                $('.js_modalList').each(function(n,ele){
                    var $this = $(this);
                    init.addPoint($this);
                });

            },
            onFailure: function (data) {
                $.uiAlert({
                    type: "danger",
                    textHead: '数据获取失败',
                    text: data.msg,
                    time: 1
                });
            }
        })
    },


    /**
    * 获取product列表
    * */
    getProductList:function(type,id,callBack){
        $(document).api({
            action:'get product list',
            on:'now',
            method:'post',
            data:{
                flowConfigureType:type,
                id:id
            },
            onSuccess:function(data){
                $('#productList').html('')
                for(i in data.data){
                    var _data = data.data[i];
                    $('#productList').append('<option value='+_data.id+'>'+_data.name+'')
                }
                $('#productList').dropdown({
                    allowAdditions: true,
                }).removeClass('ks-hidden')

                if(typeof callBack =='function' && callBack){
                    callBack()
                }
            },
            onFailure:function(data){
                $.uiAlert({
                    type: "danger",
                    textHead: '产品列表数据获取失败',
                    text: data.msg,
                    time: 1
                });
            }
        })
    },


    /*
    * 选择流程类型调用产品列表
    */
    changeType:function(){
        var type = $('#js_flowType').val();
        init.getProductList(type,'');
        $(document).on('change','#js_flowType',function(){
            var type = $(this).val()
            init.getProductList(type,'');
        })
    }
};


//校验
$.fn.form.settings.rules.isDiff = function(value){
    var i = 0;
    $('.js_modal').each(function(n,ele){
        if($(ele).val() == value){
            i++;
        }
    });
    return i==1
}

var validate1 = {
    inline: true,
    fields:{
        flowName: {
            identifier: 'flowName',
            rules: [{
                type: 'empty',
                prompt: '流程名称不为空'
            }, {
                type: 'maxLength[20]',
                prompt: '不超过20个字符'
            }]
        },
        flowDesc: {
            identifier: 'flowDesc',
            rules: [{
                type: 'empty',
                prompt: '流程说明不为空'
            }, {
                type: 'maxLength[200]',
                prompt: '不超过200个字符'
            }]
        },
        productId:{
            identifier:'productId',
            rules:[{
                type:'empty',
                prompt:'关联产品不可为空'
            }]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }

}

var validate2 = {
    inline: true,
    fields:{
        pointName: {
            identifier: 'pointName',
            rules: [{
                type: 'empty',
                prompt: '节点名称不为空'
            },{
                type: 'maxLength[50]',
                prompt: '不超过50个字符'
            }]
        },
        pointRole: {
            identifier: 'pointRole',
            rules: [{
                type: 'maxLength[50]',
                prompt: '不超过50个字符'
            }]
        },
        postId:{
            identifier: 'postId',
            rules: [{
                type: 'empty',
                prompt: '请选择查询中的角色'
            }]
        },
        approveAmount: {
            identifier: 'approveAmount',
            rules: [{
                type: 'empty',
                prompt: '限额不为空'
            },{
                type:'validateNumFloat[0.00-99999999.99]',
                prompt:'限额在[0.00-99999999.99]'
            }]
        },
        onSuccess: function (e, fidlds) {
            e.preventDefault();
        }
    }
};

function changeModal() {
    $('#errorMessage').html('');
    if($('.js_modalList').length <1){
        $('#errorMessage').html('<li>请添加至少一个模块</li>');
    }
    return true;
}

function addFlowSettings(){
    var flag,flag2;
    var flag3 = changeModal();
    $("#addForm .js_node").each(function (i,ele) {
        flag2 = $(ele).form(validate2).form("validate form");
        if(flag2 == false)
            return flag2
    });
    flag = flag2&&flag3;
    if(!flag){
        $('.ui.form').addClass('error')
    }
    if(flag){
        $('#addForm').form(validate1).api({
            action:'flow settings update',
            method:'POST',
            serializeForm: false,
            beforeSend:function(settings){
                //组装json字符串
                var flowConfigureRelations = [];
                $('.js_modalList').each(function(n,ele){
                    var node = [];
                    var key = $(ele).find('.js_modal').val();
                    var $this = $(ele).find('.js_node');
                    $this.each(function(i,ths){
                        node.push({
                            'name':$(ths).find('input[name="pointName"]').val(),
                            'organizeId':$(ths).find('input[name="postId"]').val(),
                            'approveAmount':$(ths).find('input[name="approveAmount"]').val(),
                            'enterprise':$(ths).find('select[name="enterprise"]').val()
                        })
                    })
                    flowConfigureRelations.push({
                        'moduleType':key,
                        'template':{
                            'flowControlItems':node
                        }
                    })
                })
                var FlowConfigure = {
                    'flowType':$('#js_flowType').val(),
                    'flowName':$('#flowName').val(),
                    'flowDesc':$('#flowDesc').val(),
                    'productId':$('#productList').val().join(','),
                    'flowConfigureRelations':flowConfigureRelations
                };
                settings.data = JSON.stringify(FlowConfigure)

                for(i in settings.data){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
                return settings;
            },

            onSuccess: function (response) {
                $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 1,onClosed:function(){
                    window.location.href='/flow_configure_control/index'
                }});
            },
            onFailure: function (response) {
                $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
            }
        });
        $('#addForm').submit();
    }
}

function editFlowSettings(){
    var flag,flag2;
    var flag3 = changeModal();
    $("#editForm .js_node").each(function (i,ele) {
        flag2 = $(ele).form(validate2).form("validate form");
        if(flag2 == false)
            return flag2
    });
    flag = flag2&&flag3;
    if(!flag){
        $('.ui.form').addClass('error')
    }
    if(flag){
        $('#editForm').form(validate1).api({
            action:'flow settings update',
            method:'POST',
            serializeForm: false,
            beforeSend:function(settings){
                //组装json字符串
                var flowConfigureRelations = [];
                $('.js_modalList').each(function(n,ele){
                    var node = [];
                    var key = $(ele).find('.js_modal').val();
                    var modalId = $(ele).find('input[name="modalId"]').val();
                    var tempId = $(ele).find('input[name="tempId"]').val();

                    var $this = $(ele).find('.js_node');
                    $this.each(function(i,ths){
                        node.push({
                            'id':$(ths).find('input[name="pointId"]').val(),
                            'name':$(ths).find('input[name="pointName"]').val(),
                            'organizeId':$(ths).find('input[name="postId"]').val(),
                            'approveAmount':$(ths).find('input[name="approveAmount"]').val(),
                            'enterprise':$(ths).find('select[name="enterprise"]').val()
                        })
                    });
                    flowConfigureRelations.push({
                        'id':modalId,
                        'moduleType':key,
                        'template':{
                            'flowControlItems':node,
                            'id':tempId
                        }
                    })
                })
                var FlowConfigure = {
                    'flowType':$('#js_flowType').val(),
                    'flowName':$('#flowName').val(),
                    'flowDesc':$('#flowDesc').val(),
                    'productId':$('#productList').val().join(','),
                    'id':$('#id').val(),
                    'flowConfigureRelations':flowConfigureRelations
                };
                settings.data = JSON.stringify(FlowConfigure)

                for(i in settings.data){
                    var val = settings.data[i];
                    settings.data[i] = $.trim(val);
                }
                return settings;
            },
            onSuccess: function (response) {
                $.uiAlert({type: "success", textHead: '保存成功', text: '', time: 1,onClosed:function(){
                    window.location.href='/flow_configure_control/index'
                }});

            },
            onFailure: function (response) {
                $.uiAlert({type: "danger", textHead: '保存失败', text: response.msg, time: 3});
            }
        });
        $('#editForm').submit();
    }
}

var path = window.location.pathname;
if(path.indexOf('edit') > -1){
    init.addModal();
    init.remove();
    init.flowModuleList();
    init.afterAddMoadal();
    init.getFlowType();
    init.getDetail();
    $('#editForm_btn').click(function(){
        editFlowSettings()
    })
}else if(path.indexOf('add') > -1){
    init.addModal();
    init.remove();
    init.flowModuleList();
    init.afterAddMoadal();
    init.postSearch();
    init.getFlowType(function(){
        init.changeType();
    });

    $('#addForm_btn').click(function(){
        addFlowSettings()
    })
}else if(path.indexOf('view') > -1){
    init.addModal();
    init.flowModuleList();
    init.getFlowType();
    init.getDetail();
}
