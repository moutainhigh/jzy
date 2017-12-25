/**
 * Created by pengs on 2016/12/1.
 */
/**
 * 数据存储
 */
var STORAGE = {
    chosedTreeItemData: {}
};


/**
 * 组织
 * @type {{}}
 */
var Agency = {};
var Orgnaztion = {};
var Position = {};
var Structure = {};


/**
 * 组织树
 * @type {{init: Tree.init, getData: Tree.getData, renderTree: Tree.renderTree, initAccordion: Tree.initAccordion}}
 */
var Tree = {
    init: function () {
        this.getData();
    },
    getData: function () {
        var othis = this;
        $(document).api({
            on: "now",
            method: 'post',
            action: "get orgnaztion",
            onSuccess: function (data) {
                var _data = data;
                othis.renderTree(_data);
            }
        });
    },
    renderTree: function (data) {
        var othis = this;

        data.hasNodes = function () {
            return (this.children.length > 0);
        };
        data.isAgency = function () {
            return this.orgType == SETTINGS.ORGANIZITION.AGENCY;
        };
        data.isOrganizition = function () {
            return this.orgType == SETTINGS.ORGANIZITION.ORGANIZE;
        };
        data.isPosition = function () {
            return this.orgType == SETTINGS.ORGANIZITION.POSITION;
        };
        var $treeTemplate = utils.render("#treeTemplate", data, "#partTreeTemplate");
        $("#mustContainer").append($treeTemplate);

        othis.initAccordion();

    },
    initAccordion: function () {
        var othis = this;
        var $lastActTreeItem;
        $(".js-accordion").accordion({
            onChange: function (a, b, c) {
                var $chosedElement = $(this);
                var $lastActiveElement;

                //下面是对according进行一些fix
                if ($chosedElement.prev(".title").hasClass("active")) {
                    $lastActiveElement = $chosedElement;
                    $lastActiveElement.siblings(".content ").each(function () {
                        var $this = $(this);
                        $this.find(".active").each(function (index, ele) {
                            $(this).removeClass("active")
                        })
                    })
                } else {
                    $lastActiveElement = $chosedElement.parent(".accordion").parent(".content");
                    $chosedElement.find(".active").each(function (index, ele) {
                        $(this).removeClass("active")
                    })
                }

                //存储选中的数据
                if ($lastActiveElement) {
                    STORAGE.chosedTreeItemData = {
                        orgType: $lastActiveElement.data("orgtype"),
                        text: $lastActiveElement.data("text"),
                        id: $lastActiveElement.data("id"),
                        path: $lastActiveElement.data("path"),
                        parenttext: $lastActiveElement.parent(".accordion").parent(".content").data("text")
                    };
                }

                console.log(STORAGE.chosedTreeItemData);

                Details.render(
                    STORAGE.chosedTreeItemData.orgType,
                    STORAGE.chosedTreeItemData.id
                );
            }
        });
    },


}


/**
 * 组织信息详情 *
 * @type {{store: {orgType: string, id: string, data: {}}, render: Details.render, getDataById: Details.getDataById, renderByOrgtypeAndData: Details.renderByOrgtypeAndData}}
 */
var Details = {
    store: {
        orgType: '',
        id: '',
        data: {}
    },
    render: function (orgType, id) {
        var othis = this;

        othis.store.orgType = orgType;
        othis.store.id = id;
        othis.getDataById(othis.store.id)
    },
    getDataById: function (id) {
        var othis = this;
        $(document).api({
            on: "now",
            method: 'post',
            action: "get orgnaztion datails",
            data: {
                id: id
            },
            onSuccess: function (data) {
                othis.store.data = data;
                $.extend(STORAGE.chosedTreeItemData, data)
                othis.renderByOrgtypeAndData(othis.store.orgType, othis.store.data)
            }
        });
        othis.renderByOrgtypeAndData(othis.store.orgType, othis.store.data)
    },
    /**
     * @param orgType  节点类型 1300:机构 1301:组织 1302:岗位
     * @param data
     */
    renderByOrgtypeAndData: function (orgType, data) {

        switch (orgType) {
            case 1300:
                //详细信息
                var $AgcDetailsTemplate = utils.render("#AgcDetailsTemplate", data, "#orgActionTemplate");
                $("#orgDetailsContainer").html($AgcDetailsTemplate);
                //操作按钮
                OrgAction.init();
                OrgAction.setDisabled([]);
                //人员列表
                $("#personList").hide();
                break;

            case 1301:
                //详细信息
                var $orgDetailsTemplate = utils.render("#orgDetailsTemplate", data, "#orgActionTemplate");
                $("#orgDetailsContainer").html($orgDetailsTemplate);
                //操作按钮
                OrgAction.init();
                OrgAction.setDisabled([
                    "addAgc"
                ]);
                //人员列表
                $("#personList").hide();
                break;

            case 1302:
                //详细信息
                var $orgDetailsTemplate = utils.render("#postDetailsTemplate", data, "#orgActionTemplate");
                $("#orgDetailsContainer").html($orgDetailsTemplate);
                //操作按钮
                OrgAction.init();
                OrgAction.setDisabled([
                    "addAgc", "addOrgn", "addPost"
                ]);
                //渲染人员列表
                PersonList.queryId =STORAGE.chosedTreeItemData.id
                if (PersonList.isInit) {
                    PersonList.dtTable.ajax.reload();
                } else {
                    PersonList.init();
                }
                $("#personList").show();
                break;
            default:
                //详细信息
                var $defaultDetailsTemplate = utils.render("#defaultDetailsTemplate", data);
                $("#orgDetailsContainer").html($defaultDetailsTemplate);
                //人员列表
                $("#personList").hide();
                break;
        }
    }
}


/**
 * 组织的 操作按钮
 * @type {{init: OrgAction.init, setDisabled: OrgAction.setDisabled}}
 */
var OrgAction = {
    init: function () {
        $('#orgAction').dropdown({
            onChange: function (value, text, $choice) {
                console.log(value)
                switch (value) {
                    case "addAgc":
                        Agency.save(
                            "add",
                            STORAGE.chosedTreeItemData.id
                        );
                        break;
                    case "addPost":
                        Position.save(
                            "add",
                            STORAGE.chosedTreeItemData.id
                        );
                        break;
                    case "addOrgn":
                        Orgnaztion.save(
                            "add",
                            STORAGE.chosedTreeItemData.id
                        );
                        break;
                    case "editOrgn":
                        editOrgnByOrgTypeAndId(
                            STORAGE.chosedTreeItemData.orgType,
                            STORAGE.chosedTreeItemData.id
                        );
                        break;
                    case "delOrgn":
                        delOrgn();
                        break;
                }
            }
        });
    },
    setDisabled: function (actionArray) {
        var $action = $('#orgAction');
        $('#orgAction .item ').removeClass("disabled");
        for (var i = 0; i < actionArray.length; i++) {
            $('#orgAction').find("[data-value=" + actionArray[i] + "]").addClass("disabled");
        }
    }
}


/**
 * 修改组织信息
 * @param type
 * @param id
 */
function editOrgnByOrgTypeAndId(type, id) {
    switch (type) {
        case SETTINGS.ORGANIZITION.AGENCY:
            Agency.save(
                "update",
                id
            );
            break;

        case SETTINGS.ORGANIZITION.ORGANIZE:
            Orgnaztion.save(
                "update",
                id
            );
            break;

        case SETTINGS.ORGANIZITION.POSITION:
            Position.save(
                "update",
                STORAGE.chosedTreeItemData.id
            );
            break;
    }
}


/**
 * 删除组织
 */
function delOrgn() {
    var id = STORAGE.chosedTreeItemData.id;
    var name;
    if (STORAGE.chosedTreeItemData.orgType == 1300) {
        name = STORAGE.chosedTreeItemData.agencyName
    } else {
        name = STORAGE.chosedTreeItemData.name;
    }

    $.uiDialog("当前节点为:" + name + "，你确定要删除吗？", {
        onApprove: function () {
            var $modal = $(this);
            $(document).api({
                on: "now",
                method: 'post',
                action: "delete orgnazition",
                data: {
                    "id": id
                },
                onSuccess: function (response) {
                    $modal.modal({
                        onHidden: function () {
                            if (response.ok) {
                                $.uiAlert({
                                    type: "success",
                                    textHead: '删除成功',
                                    text: '成功删除节点',
                                    time: 1,
                                    onClosed: function () {
                                        window.location.reload()
                                    }
                                });
                            } else {
                                $.uiAlert({
                                    type: "danger",
                                    textHead: '删除失败',
                                    text: response.msg,
                                    time: 1
                                });
                            }
                        }
                    }).modal("hide");
                }
            });

            return false;
        },
        onDeny: function () {
        }
    })
}


/**
 * 人员列表
 * @type {{dtTable: {}, isInit: boolean, init: PersonList.init}}
 */
var PersonList = {
    dtTable: {},
    isInit: false,
    queryId:"",
    init: function (id) {
        var othis = this;
        othis.dtTable = $("#personTable").DataTable({
            serverSide: true,//服务端分页
            searching: false,//显示默认搜索框
            ordering: false,//开启排序
            autoWidth:true,//自适应宽度
            "ajax": {
                "url": $(document).api.settings.api['get userByOrg'],
                "data": function (d) {
                    d.id = othis.queryId;
                },
                "dataSrc": function (json) {
                    for (var i = 0; i < json.data.length; i++) {
                        json.data[i] = $.extend({
                            id: 0,
                            name: "",
                            organizeId: 0,
                            orgName: "",
                            login: "",
                            mobile: "",
                            roleName: ""
                        }, json.data[i]);
                    }
                    return json.data
                },
                "type": "POST"
            },
            columns: [
                {data: 'name'},
                {data: 'orgName'},
                {data: 'roleName'},
                {data: 'login'},
                {data: 'mobile'},
            ],
            columnDefs: [{
                //   指定第最后一列
                targets: 5,
                render: function (data, type, row, meta) {
                    return '<button class="ui mini primary button js-personChange" data-id="' + row.id + '">修改</button>';
                }
            }],
            "iDisplayLength": 20,
            "aLengthMenu": [
                [20],
                [20]
            ],

        });
        othis.bindEvents();
        othis.isInit = true;
    },
    bindEvents: function () {
        //新增人员
        $("body").on("click", ".js-addPerson", function () {
            Person.add();
        })
        //修改人员
        $("body").on("click", ".js-personChange", function () {
            var id = $(this).data("id");
            Person.update(id);
        })
    }
}


/**
 * 人员
 * @type {{manage: Person.manage, add: Person.add, update: Person.update}}
 */
var Person = {
    /**
     * manage抽象了新增和修改方法
     * @param opts add/update
     * @param opts
     */
    manage: function (type, opts) {
        var defaults = {
            action: "",
            resOk: {
                textHead: '',
                text: ''
            },
            resFail: {
                textHead: ''
            }
        };
        var options = $.extend(defaults, opts);

        var $modal = $("#personModal");
        var $form = $modal.find(".ui.form");
        var $submitBtn = $form.find(".submit.button");
        $modal.modal({
                blurring: true,
                onApprove: function () {
                    $(this).find(".form").submit();
                    return false;
                },
                onShow: function () {
                    $form.form({
                        inline: true,
                        fields: {
                            name: {
                                identifier: 'user.name',
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name}不能为空'
                                    }
                                ]
                            },
                            mobile: {
                                identifier: 'user.mobile',
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name}不能为空'
                                    }
                                ]
                            },
                            roles: {
                                identifier: 'user.roles',
                                rules: [
                                    {
                                        type: 'minCount[1]',
                                        prompt: '请选择至少一个角色'
                                    }
                                ]
                            },
                            login: {
                                identifier: 'user.login',
                                rules: [
                                    {
                                        type: 'empty',
                                        prompt: '{name}不能为空'
                                    }
                                ]
                            },
                            status: {
                                identifier: 'user.status',
                                rules: [
                                    {
                                        type: 'checked',
                                        prompt: '请选择一项'
                                    }
                                ]
                            },

                        },
                        onSuccess: function (e, fidlds) {
                            e.preventDefault();
                        }

                    }).api({
                        action: options.action,
                        method: 'POST',
                        serializeForm: true,
                        data: {
                            "user.organizeId": STORAGE.chosedTreeItemData.id,
                            "user.id": options.info.id
                        },
                        beforeSend: function (settings) {
                            var roles = settings.data["user.roles"];
                            var _return = [];
                            if (utils.isString(roles)) {
                                _return = [{"id": roles}];
                            } else {
                                for (var i = 0; i < roles.length; i++) {
                                    _return.push({"id": roles[i]});
                                }
                            }
                            settings.data["user.roles"] = _return;
                            return settings;
                        },
                        onSuccess: function (response) {
                            $modal.modal({
                                onHidden: function () {
                                    if (response.ok) {
                                        $.uiAlert({
                                            type: "success",
                                            textHead: options.resOk.textHead,
                                            text: options.resOk.text,
                                            time: 1,
                                            onClosed: function () {
                                                window.location.reload()
                                            }
                                        });
                                    } else {
                                        $.uiAlert({
                                            type: "danger",
                                            textHead: options.resFail.textHead,
                                            text: response.msg,
                                            time: 1
                                        });
                                    }
                                }
                            }).modal("hide");
                        }
                    })
                    if (type == "add") {
                        $modal.find(".js-personHeader").html('新增人员');
                        $form.form('clear')
                    } else if (type == "update") {
                        $modal.find(".js-personHeader").html('修改人员');
                        $form.form('clear').form('set values', {
                            'user.name': options.info.name,
                            'user.mobile': options.info.mobile,
                            'user.login': options.info.login,
                            'user.status': options.info.status,
                            'user.roles': options.info.roles
                        })
                    }

                }
            }
        ).modal('show');
    },
    add: function () {
        var othis = this;
        othis.manage("add", {
            action: "add user",
            resOk: {
                textHead: '新增成功',
                text: '成功新增人员'
            },
            resFail: {
                textHead: '新增失败'
            },
            info: {
                id: ''
            }
        })
    },
    update: function (id) {
        var othis = this;
        $(document).api({
            on: "now",
            method: 'post',
            action: "get UserById",
            data: {
                id: id
            },
            onSuccess: function (res) {
                var data = res.data;
                var tempRoles = [];
                for (var i = 0; i < data.roles.length; i++) {
                    tempRoles.push(data.roles[i].id + "");
                }
                var info = {
                    name: data.name,
                    mobile: data.mobile ? data.mobile : '',
                    login: data.login,
                    status: data.status,
                    roles: tempRoles,
                    id: id
                };
                othis.manage("update", {
                    action: "update user",
                    resOk: {
                        textHead: '修改成功',
                        text: '成功修改人员'
                    },
                    resFail: {
                        textHead: '修改失败'
                    },
                    info: info
                })
            }
        });
    }
}


/**
 * 获取用户角色
 */
function renderPesonRoles() {
    $(document).api({
        on: "now",
        method: 'post',
        action: "get roleList",
        onSuccess: function (data) {
            var $personRolesTemplate = utils.render("#personRolesTemplate", data);
            $("#personRoles").append($personRolesTemplate);
            //初始化人员角色选择框
            $('.js-dropdown__role').dropdown({});
        }
    });
}


function initApp() {
    Tree.init();
    Details.render();
    renderPesonRoles();
}
initApp();


