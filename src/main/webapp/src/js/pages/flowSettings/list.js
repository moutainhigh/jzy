/**
 * Created by yangzb01 on 2017-08-29.
 */

var flowInit = {

    list:function(){
        dtTable = $("#flowList").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            bDestroy:true,
            "ajax": {
                "url":$(document).api.settings.api['flow list'],
                "data":function(d) {
                    var data = {};
                    data.flowType = $('#js_flowType').val();
                    var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                    return JSON.stringify(_d);
                },
                "type": "POST"
            },
            columns: [
                {data: 'flowCode'},
                {data: 'flowConfigureTypeVO.desc'},
                {data: 'flowName'},
                {data: 'flowDesc'},
                {data: 'status'},
                {data: null},
                {data: null}
            ],
            columnDefs:[{
                targets:4,
                render:function(data,type,row,meta){
                    return data =='ABLE'? '是':'否'
                }
            },{
                targets: 5,
                className:'single line',
                render: function (data, type, row, meta) {
                    if(data.status =='ABLE'){
                        return  '<button class="ui teal button mini disabled"><i class="edit icon"></i>编辑</button>' +
                                '<button class="ui basic button mini" onclick="flowInit.linkPath(\'view\',\''+row.id+'\')"><i class="info circle icon"></i>查看</button>';
                    }else{
                        return  '<button class="ui teal button mini" onclick="flowInit.linkPath(\'edit\',\''+row.id+'\')"><i class="edit icon"></i>编辑</button>' +
                                '<button class="ui basic button mini" onclick="flowInit.linkPath(\'view\',\''+row.id+'\')"><i class="info circle icon"></i>查看</button>';
                    }
                }
            },{
                targets: 6,
                className:'single line',
                render:function(data, type, row, meta){
                    if(data.status =='ABLE'){
                        return '<button class="ui button mini" onclick="flowInit.changeFlow(\''+row.status+'\',\''+row.id+'\')">已启用</button>'
                    }else{
                        return '<button class="ui teal button mini" onclick="flowInit.changeFlow(\''+row.status+'\',\''+row.id+'\')">启用</button>'
                    }
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ],
        });

    },

    getFlowType:function(){
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

    search:function(){
        dtTable.ajax.reload();
    },

    reset:function(){
        $('#js_flowType').val("");
    },

    linkPath:function(path,id){
        if(id){
            window.location.href ='/flow_configure_control/'+path+'?id='+id;
        }else{
            window.location.href ='/flow_configure_control/'+path;
        }
    },

    /**
    * 是否开启流程
    * */
    changeFlow:function(status,id){
        var dialogText = status =='DISABLED'? '启用':'停用';

        $.uiDialog('确定'+dialogText+'这个流程？',{
            onApprove:function(){

                if(status =='DISABLED'){
                    $(document).api({
                        on:'now',
                        action:'start flow',
                        method:'post',
                        data:{
                            id:id,
                        },
                        onSuccess:function(data){
                            dtTable.ajax.reload();
                            $.uiAlert({
                                type: "success",
                                textHead: '启用成功',
                                text: data.msg,
                                time: 1
                            });
                        },
                        onFailure: function (data) {
                            $.uiAlert({
                                type: "danger",
                                textHead: '启用失败',
                                text: data.msg,
                                time: 1
                            });
                        }
                    })
                }else{
                    $(document).api({
                        on:'now',
                        action:'stop flow',
                        method:'post',
                        data:{
                            id:id,
                        },
                        onSuccess:function(data){
                            dtTable.ajax.reload();
                            $.uiAlert({
                                type: "success",
                                textHead: '停用成功',
                                text: data.msg,
                                time: 1
                            });
                        },
                        onFailure: function (data) {
                            $.uiAlert({
                                type: "danger",
                                textHead: '停用失败',
                                text: data.msg,
                                time: 1
                            });
                        }
                    })
                }

            }
        })
    },

};
flowInit.getFlowType();
flowInit.list();
