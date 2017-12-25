/**
 * Created by pengs on 2016/12/1.
 */
/**
 * 数据存储
 */
var STORAGE = {
    chosedTreeItemData: {},
    modalChosedAgency: {}
};

/**
 * 阿拉伯数字转中文
 * @param num
 * @returns {中文数字 一到十}
 */
function numToChinese(num) {
    var NUM_STRING = '一二三四五六七八九十';
    var num = Number(num);
    return NUM_STRING[num - 1];
}

/**
 * 组织
 * @type {{}}
 */
var Agency = {};


/**
 * 组织树
 * @type {{init: Tree.init, getData: Tree.getData, renderTree: Tree.renderTree, initAccordion: Tree.initAccordion}}
 */
var Tree = {
    data: {},
    init: function () {
        this.getData();
    },
    getData: function () {
        var othis = this;
        $(document).api({
            on: "now",
            method: 'post',
            action: "get agencies",
            onSuccess: function (data) {
                othis.data = data;
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

        othis.initJsOrgTree();
        othis.initSearch();
        othis.bindEvents();
    },
    initJsOrgTree: function () {

        $(".js-orgTree").on("click", 'li', function () {
            $this = $(this);

            $(".js-orgTree").find("li").removeClass("active");
            $this.addClass("active");

            STORAGE.chosedTreeItemData = {
                orgType: $this.data("orgtype"),
                text: $this.data("text"),
                id: $this.data("id"),
                path: $this.data("path"),
            };

            Details.render(
                STORAGE.chosedTreeItemData.id
            );

        })

        //默认选中第一个
        $(".js-orgTree li:first").trigger("click");

    },
    initSearch: function () {
        var othis = this;

        $.fn.form.settings.rules.inTree = function (value) {
            var flag = false;

            $(".js-orgTree li").each(function (index, element) {
                if ($(this).data("text").indexOf(value) > -1) {
                    flag = true;
                }
            })

            return flag;
        };
        $(".js-searchTreeForm").form({
            inline: true,
            fields: {
                keyword: {
                    identifier: 'keyword',
                    rules: [
                        {
                            type: 'inTree',
                            prompt: '没有搜索结果'
                        }
                    ]
                }
            },
            onSuccess: function (e, fields) {
                e.preventDefault();
                var value = fields.keyword;

                var matchs = [];
                othis.initSearch.matchList = othis.initSearch.matchList || [];
                othis.initSearch.lastMatchKeyword = othis.initSearch.lastMatchKeyword || "";
                othis.initSearch.matchIndex = othis.initSearch.matchIndex || 0;

                if (value != othis.initSearch.lastMatchKeyword) {
                    $(".js-orgTree li").each(function (index, element) {
                        if ($(this).data("text").indexOf(value) > -1) {
                            matchs.push($(this));
                            othis.initSearch.lastMatchKeyword = value;

                        }
                    })
                    othis.initSearch.matchIndex = 0;
                    othis.initSearch.matchList = matchs;
                }
                var $ele = othis.initSearch.matchList[othis.initSearch.matchIndex];
                $ele.trigger("click");
                if (othis.initSearch.matchIndex < othis.initSearch.matchList.length - 1) {
                    othis.initSearch.matchIndex++;
                } else {
                    othis.initSearch.matchIndex = 0;
                }


            }
        })
    },
    bindEvents: function () {

        $(".js-addAgeBtn").on("click", function () {
            if (STORAGE.chosedTreeItemData.id == null) {
                Agency.save(
                    "add",
                    STORAGE.chosedTreeItemData.id
                );
            } else {
            $(document).api({
                on: "now",
                method: 'post',
                action: "get agency id",
                data: {
                    id: STORAGE.chosedTreeItemData.id
                },
                onSuccess: function (data) {
                    $.extend(STORAGE.modalChosedAgency, data.data);
                    Agency.save(
                        "add",
                        STORAGE.chosedTreeItemData.id
                    );
                }
            });
          }
        });

        $(document).on("click", ".js-editBtn", function () {
            //todo
            Agency.save(
                "update",
                STORAGE.chosedTreeItemData.id
            );
        });


    }


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
    render: function (id) {
        var othis = this;
        othis.store.id = id;
        othis.getDataById(othis.store.id)
    },
    getDataById: function (id) {
        var othis = this;
        $(document).api({
            on: "now",
            method: 'post',
            action: "fetch agency datails",
            data: {
                id: id
            },
            onSuccess: function (data) {
                var _data =data.data;
                othis.store.data =_data;
                $.extend(STORAGE.chosedTreeItemData, _data);
                _data.levelInCN = function () {
                    return numToChinese(this.level) + "级机构"
                };
                _data.statsInCn = function () {
                    return enums.status[this.status];
                    // return this.status == "ABLE" ? "有效" : "无效";
                }
                var $AgcDetailsTemplate = utils.render("#AgcDetailsTemplate", _data, "#orgActionTemplate");
                $("#orgDetailsContainer").html($AgcDetailsTemplate);
            }
        });
        // var $AgcDetailsTemplate = utils.render("#AgcDetailsTemplate", data, "#orgActionTemplate");
        // $("#orgDetailsContainer").html($AgcDetailsTemplate);
    },
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


function initAgencySearch() {
    $('.js-agcSearch').search({
        apiSettings: {
            method: "post",
            url: $(document).api.settings.api['search agency']+'?search={query}'
        },
        fields: {
            results: 'data',
            title: 'name',
            description: 'code'
        },
        // minCharacters : 3
        onSelect: function (result, response) {
            $.extend(STORAGE.modalChosedAgency, result);
            $("input[name='agency.level']").val(+result.level + 1);
        },
        // onResults:function(response){
        //     console.log("response:");
        //     console.log(response)
        // }
        // onSearchQuery: function (query) {
        //     console.log(query)
        // }
    })

    $('.js-agcSearch input').on('input propertychange',function(){
        var $this=$(this);
        $("input[name='agency.level']").val("");
    })

}


function initApp() {
    Tree.init();
    // Details.render();
    initAgencySearch();
}
initApp();


