/**
 * Created by pengs on 2016/12/9.
 */

var Organizition = {};
var formQuery = {
    data: {
        agencyId: '',
        orgId: '',
        businessLine: '',
        organizeType: '',
        date: '',
    },
    reset: function () {
        this.data = {
            agencyId: '',
            orgId: '',
            businessLine: '',
            organizeType: '',
            date: '',
        }
    }
};
var dtTable;


/**
 * 查询表单
 * @type {{init: searchForm.init, initAgencySearch: searchForm.initAgencySearch, initOrganizitionSearch: searchForm.initOrganizitionSearch}}
 */
var searchForm = {
    init: function () {
        var othis = this;
        othis.initAgencySearch();
        othis.initOrganizitionSearch();

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
    initAgencySearch: function () {
        $('.js-agcSearch').search({
            apiSettings: {
                method: "post",
                url: $(document).api.settings.api['search agency'] + '?search={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            // minCharacters : 3
            onSelect: function (result, response) {
                $(this).find('input[name="agencyId"]').val(result.id)
            },
        })

        $('.js-agcSearch .js-input').on('input propertychange', function () {
            $(this).prev('input').val("");
        })
    },
    initOrganizitionSearch: function () {
        $('.js-orgSearch').search({
            apiSettings: {
                method: "post",
                url: $(document).api.settings.api['search organizition'] + '?search={query}'
            },
            fields: {
                results: 'data',
                title: 'name',
                description: 'code'
            },
            // minCharacters : 3
            onSelect: function (result, response) {
                $(this).find('input[name="orgId"]').val(result.id)
            },

        })

        $('.js-orgSearch  .js-input').on('input propertychange', function () {
            $(this).prev('input').val("");
        })
    },

}


/**
 * 组织列表
 */
function initOrgList() {
    dtTable = $("#personTable").DataTable({
        serverSide: true,//服务端分页
        searching: false,//显示默认搜索框
        ordering: false,//开启排序
        autoWidth: true,//自适应宽度
        "ajax": {
            "url": $(document).api.settings.api['get organizition'],
            "data": function (d) {
                if($("input[name='agencyId']").val()==""&&$("input[name='agencyName']").val()!=""){
                    formQuery.data.agencyId=$("input[name='agencyName']").val();
                }
                if($("input[name='orgId']").val()==""&&$("input[name='orgName']").val()!=""){
                    formQuery.data.orgId=$("input[name='orgName']").val();
                }
                var _d = $.extend({},{searchKeys:formQuery.data},{start:d.start,length:d.length,draw:d.draw});
                return JSON.stringify(_d);
            },
            "type": "POST"
        },
        columns: [
            {data: 'agency.code'},
            {data: 'agency.name'},
            {data: 'code'},
            {data: 'name'},
            {
                data: 'businessLine',
                render: function (data) {
                    return enums.business_line[data];
                }
            },
            {data: 'managerName'},
            {
                data: 'status',
                render: function (data) {
                    return enums.status[data];
                }
            },
            {data: 'establishDate',
                render: function (data) {
                    return data==null?"":moment(data).format("YYYY-MM-DD");
                }
            },
        ],
        columnDefs: [{
            //   指定第最后一列
            className:"single line",
            targets: 8,
            render: function (data, type, row, meta) {
                return '<div class="ui mini basic button js-viewOrg" data-id="' + row.id + '"><i class="Info Circle icon"></i>查看</div>' + '<div  class="ui mini basic button js-editOrg" data-id="' + row.id + '"><i class="edit icon"></i>编辑</div>';
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
                Organizition.save(
                    "add"
                );
            })
        }
    })
}

/**
 * 条线
 */
function initBusinessLine() {
    $(document).api({
        on: "now",
        method: 'post',
        action: "get business line",
        onSuccess: function (data) {
            data.text = function () {
                return enums.business_line[this.value]
            }
            var $businessLineTemplate = utils.render("#businessLineTemplate", data);
            $(".js-SearchForm__lineField .menu").append($businessLineTemplate);
            $(".js-saveModal__lineField .menu").append($businessLineTemplate);
            $(".js-lineDropdown").dropdown()
        }
    });
}

function bindEvents() {


    $("#setupTime").dateRangePicker({})

    //编辑组织
    $(document).on("click", ".js-editOrg", function () {
        $(document).api({
            on: "now",
            method: 'post',
            action: "get organizition by id",
            data: {
                id: $(this).data("id")
            },
            onSuccess: function (data) {
                //open modal
                Organizition.save(
                    "update",
                    data.data
                );
            }
        });

    })

    //查看组织
    $(document).on("click", ".js-viewOrg", function () {
        //todo :need mustache template
        $(document).api({
            on: "now",
            method: 'post',
            action: "get organizition by id",
            data: {
                id: $(this).data("id")
            },
            onSuccess: function (data) {
                var data = data.data;
                data.blineName = function () {
                    return enums.business_line[this.businessLine];
                }
                data.statsInCn = function () {
                    return enums.status[this.status];
                }
                data.orgTypeName = function () {
                    return enums.organizeType[this.organizeType];
                }
                data.isAgc = function () {
                    return this.organizeType === "ORGANIZE" ? true : false;
                }
                data.e_date = function () {
                    var e_date = data.establishDate;
                    if (e_date != null) {
                        return moment(e_date).format("YYYY-MM-DD");
                    }
                }
                data.r_date = function () {
                    var r_date = data.revokeDate;
                    if (r_date != null) {
                        return moment(r_date).format("YYYY-MM-DD");
                    }
                }
                var $orgDetailsTemplate = utils.render("#orgDetailsTemplate", data);
                $("body").append($orgDetailsTemplate);
                $(".js-viewOrgModal").modal({
                    blurring: true,
                    onHidden: function () {
                        $(this).remove()
                    }
                }).modal("show")
            }
        });
    })
    $(".js-groupDropdown").dropdown()
}


function initPage() {
    initBusinessLine();
    searchForm.init();
    bindEvents();
    initOrgList();
}

initPage();
