// 执行初始化方法
function initPage(){
    bindEvents();
    initUserList();
}
initPage();
//初始化参数
var dtTable;
var url="";
var type="";
var sId="";

var User = {};
// 查询组件事件初始化
function bindEvents() {
    // 查询类别下拉列表事件
    initModelSearchTypeDropdown();
    // 点击查询按钮事件
    $("body").on("click", ".js-button-search", function () {
        search();
    })
    //重置按钮事件
    $("body").on("click", ".js-button-reset", function () {
        reset();
    })

    // 实时搜索查询内容事件
    initModalContextSearch();
    // 编辑业务人员信息按钮事件
    editUser();
    //查看业务人员信息按钮事件
    viewUser();
    //创建用户登陆账号
    createUserAccount();
}
// 查询类别下拉列表
function initModelSearchTypeDropdown(){
    $(".js-SearchTypeDropdown").dropdown({
        onChange: function (value, text, $selectedItem) {
            $("#js-input-search").val("");
            if(value==""){
                $(".js_field__contextSearch").hide();
            }else {
                $(".js_field__contextSearch").show();
            }
        }
    });
}
//调用查询搜索
function initModalContextSearch() {
    $(".js_modal__contextSearch").search({
        cache:false,
        apiSettings: {
            method: "post",
            beforeSend:function(settings){
                type=$("#searchType").val();
                if(type!=""){
                  if(type=="agencyType"){
                      url=$(document).api.settings.api['search agency'] + '?search={query}'
                  }else if(type=="orgType"){
                      url=$(document).api.settings.api['search organizition'] + '?search={query}'
                    }else {
                      url=$(document).api.settings.api['search b_user'] + '?search={query}'
                  }
                }
                settings.url=url;
               return settings;
            }
        },
        fields: {
            results: 'data',
            title: 'name',
            description: 'code'
        },
        onSelect: function (result, response) {
            $(this).find('input[name="sId"]').val(result.id)
        }
    })
    $(".js_modal__contextSearch").on('input propertychange', '.js-input', function () {
        $(this).prev('input').val("");
    })
}
//搜索
function search() {
    dtTable.ajax.reload();
}
//重置
function reset() {
    $("#searchType").val('');
    $(".js-SearchTypeDropdown").dropdown("set selected", "");
    $("#js-input-search").val("");
    $(".js_field__contextSearch").hide();
    dtTable.ajax.reload();
}
//编辑业务人员信息
function editUser() {
    $(document).on("click",".js-personEdit",function(){
        $(document).api({
            on: "now",
            method: 'post',
            action: "get b_user by id",
            data:{
                id:$(this).data("id")
            },
            onSuccess: function (data) {
                User.save(
                    "update",
                    data.data
                );
            }
        });
    })
}

//新增业务人员的登陆账号
function createUserAccount(){
    $(document).on("click",".js-createUserAccount",function(){
        $(document).api({
            on: "now",
            method: 'post',
            action: "add b_user account",
            data:{
                id:$(this).data("id")
            },
            onSuccess: function (data) {
                $.uiAlert({
                    type: "success",
                    textHead: '创建账号成功',
                    text: '成功创建人员',
                    time: 1,
                    onClosed: function () {
                        window.location.reload()
                    }
                });
            },
            onFailure: function (response, element) {
                $.uiAlert({
                    type: "danger",
                    textHead: '创建账号失败',
                    text: response.msg,
                    time: 1,
                });
            }
        });
    })
}
//查看业务人员信息
function viewUser() {
    $(document).on("click",".js-viewUser",function(){
        //todo :need mustache template
        $(document).api({
            on: "now",
            method: 'post',
            action: "get b_user by id",
            data:{
                id:$(this).data("id")
            },
            onSuccess: function (data) {
                var data1 = data.data;
                // data1.loginName=data.loginName;
                data1.businessLine = function () {
                    return enums.business_line[this.organize.businessLine];
                }
                data1.positionName=function () {
                    return enums.position[this.position];
                }
                data1.e_date=function () {
                    var e_date=data1.entryDate;
                    if (e_date!=null){
                        return moment(e_date).format("YYYY-MM-DD");
                    }
                }
                data1.q_date=function () {
                    var q_date=data1.quitDate;
                    if (q_date!=null){
                        return moment(q_date).format("YYYY-MM-DD");
                    }
                }
                data1.b_date=function () {
                    var b_date=data1.birthday;
                    if (b_date!=null){
                        return moment(b_date).format("YYYY-MM-DD");
                    }
                }
                data1.workStateName = function(){
                    return this.workState ==0 ? "在职":"离职";
                }
                data1.sexName = function(){
                    return this.sex ==0?"女":"男";
                }
                data1.statsInCn = function () {
                    return enums.status[this.status];
                }
                data1.credentialsTypeName = function(){
                    return enums.businessCredentialsType[this.credentialsType];
                }

                var $userDetailsTemplate = utils.render("#userDetailsTemplate",data1 );
                $("body").append($userDetailsTemplate);
                $(".js-viewUserModal").modal({
                    blurring: true,
                    onHidden:function(){
                        $(this).remove()
                    }
                }).modal("show")
            }
        });
    })
}

//人员查询列表初始化
function initUserList() {
    dtTable = $("#personTable").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax": {
            "url": $(document).api.settings.api['query b_user'],
            "data": function (d) {
                var data = {};
                type=$("#searchType").val();
                sId=$("#sId").val();
                if(sId==""&&$("input[name='searchContext']").val()!=""){
                    var searchContext=$("input[name='searchContext']").val();
                    data.sId=searchContext;
                }else {
                    data.sId=sId;
                }
                data.searchType=type;
                var _d = $.extend({},{searchKeys:data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
           },
            "type": "POST",
        },
        columns: [
            {data: 'code'},
            {data: 'name'},
            {data: 'organize.agency.code'},
            {data: 'organize.agency.name'},
            {data: 'organize.code'},
            {data: 'organize.name'},
            {
                data: 'position',
                render: function (data) {
                    return enums.position[data];
                }
            },
            {
                data: 'entryDate',
                render: function (data) {
                    return moment(data).format("YYYY-MM-DD");
                }
            },{
                data: 'status',
                render: function (data) {
                    return enums.status[data];
                }
            }
        ],
        columnDefs: [{
            //   指定第最后一列
            className:"single line",
            targets: 9,
            render: function (data,type, row, meta) {
                return   '<button  class="ui mini basic button js-createUserAccount" data-id="' + row.id + '"><i class="user icon"></i>创建账号</button>'+  '<button  class="ui mini basic button js-viewUser" data-id="' + row.id + '"><i class="Info Circle icon"></i>查看</button>' + '<button class="ui mini basic button js-personEdit" data-id="' + row.id + '"><i class="edit icon"></i>编辑</button>';
            }
        }],
        "iDisplayLength": 20,
        "aLengthMenu": [
            [20],
            [20]
        ],
        initComplete: function () {
            $(".right.aligned.eight.wide.column").append($("#addOrgTemplate").html());
            $(document).on("click", ".js-addOrgBtn", function () {
                User.save(
                    "add"
                );
            })
        }
    })
}
