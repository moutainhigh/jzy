/**
 * Created by pengs on 2016/12/8.
 */
$(function(){

var menuRouter = {
    _getRouter: function () {
        var router = {};
        $(".js-itemHeader").each(function (index, ele) {
            var othis = this;
            router[$(this).html()] = {}
            $(othis).next(".js-itemMenu ").find("a").each(function (i, e) {
                router[$(othis).html()][$(this).html()] = [$(this).attr("href")]
            })
        })
    },
    router: {
        "业务管理": {
            "业务申请 ": ["/business_apply/to_process_list", "/business_apply/query_product"],
            "业务审批 ": ["/business_apply/to_approval_list"],
            "业务审批查询 ": ["/business_apply/list"],
            "业务查询 ": ["/business_apply/list_all"],
            "业务展期 ":["/business_apply/extension"],
            "渠道提单 ":["/business_apply/query_channel_product"]
        },
        "风控管理": {
            "待审批 ": ["/business_apply/to_approval_list"],
            "审批查询 ": ["/business_apply/to_approval_complete"]
        },
        "财务管理": {
            "待审批 ": ["/business_apply/to_approval_list"],
            "待放款 ": ["/loan/pending"],
            "待收款 ": ["/loan/gather"],
            "放款查询 ": ["/loan/list"],
            "放款主体维护 ": ["/loan_subject/list"],
            "赎楼要件控制":["/loan/element"]
        },
        "贷后管理": {
            "快到期贷款 ": ["/post_loan/list"],
            "已逾期贷款 ": ["/post_loan/overdueList"]
        },
        "推单管理": {
            "待推单 ": ["/loan_push/list"],
            "推单查询 ": ["/loan_push/pushedList"]
        },
        "用印管理": {
            "用印管理 ": ["/seal/list"]
        },
        "产品管理": {
            "产品大类维护 ": ["/product_type/list"],
            "产品维护 ": ["/product/list", "/product/edit_init", "/product/add_init", "/product/view"],
            "业务审批流程配置 ": ["/flow_control/bussiness_index"],
            "风控审批流程配置 ": ["/flow_control/risk_index"],
            "财务审批流程配置 ": ["/flow_control/finance_index"],
            "影像模板配置 ": ["/media_temp/index", "/media_temp/add", "/media_temp/view", "/media_temp/edit"]
        },
        "业务人员管理": {
            "机构管理 ": ["/business_agency/index"],
            "组织管理 ": ["/business_organize/index"],
            "业务人员管理 ": ["/business_user/index"]
        },
        "用户管理": {
            "系统角色维护 ": ["/role/index"],
            "用户信息维护 ": ["/user/index"]
        },
        "授信管理":{
            "渠道维护 ": ["/channel/list", "/channel/add", "/channel/edit", "/channel/view"],
            "自营合作方维护 ":["/channel/partnerList","/channel/partnerAdd","/channel/partnerEdit","/channel/partnerView"],
            "贴现企业维护 ": ["/borrower/discountCompanyList", "/borrower/add", "/borrower/edit", "/borrower/view"]
        },
        "流程配置":{
            "流程维护 ":["/flow_configure_control/edit","/flow_configure_control/add","/flow_configure_control/view"]
        }
    },
    //特殊的详情页，一个页面，一个路由，不同query，多个菜单
    specialPath:{
        'pathname':[
            "/business_apply/to_update", "/business_apply/to_add", "/business_apply/detail",
            "/flow/to_approval_list",
            "/bill_intermediary_apply/detail", "/bill_intermediary_apply/approval",
            "/house_noMortgage_apply/detail", "/house_noMortgage_apply/approval",
            "/mortgage/edit", "/mortgage/view", "/mortgage/approval_page",
            "/extension/approval_page", "/extension/view",
            "/cost_exemption/view", "/cost_exemption/edit", "/cost_exemption/approval_page",
            "/loan_push_order/detail", "/loan_push_order/index", "/loan_push_order/add",
            "/loan_push_order/edit", "/loan_push_order/attach_download"
        ]
    },

    init: function () {
        var flag=false;
        var path = window.location.pathname;

        //特殊的详情页，一个页面，一个路由，不同query，多个菜单
        for(i in menuRouter.specialPath.pathname){
            var val = menuRouter.specialPath.pathname[i];
            if (path == val) {
                path=utils.getUrlParam("comeFrom");
            }
        }

        var othis = this;

        $("#toc .js-item").each(function (index, ele) {
            var $this = $(this);
            var secondName = $this.html();
            var firstName = $this.parent(".js-itemMenu").prev(".js-itemHeader").find(".js-itemHeaderName").html();
            var inMenu = false;
            //判断是否属于某个目录的二级地址
            if (othis.router[firstName]) {
                if (othis.router[firstName][secondName]) {
                    if (othis.router[firstName][secondName].indexOf(path) >= 0) {
                        inMenu = true
                    }
                }
            }
            //如果path直接匹配到或者属于某个目录的二级地址
            if ($this.attr("href") == path || inMenu) {
                flag = true;
                $("#toc .item").removeClass("active");
                $this.addClass("active");
                $this.parent(".js-itemMenu").addClass("active");
                $this.parent(".js-itemMenu").prev(".js-itemHeader").addClass("active");
                // $(".js-breadcumb__second").html(secondName);
                // $(".js-breadcumb__first").html(firstName);
                return;
            }
        });


        if(!flag){
            var firstItemHeader =  $("#toc .js-itemHeader:first");
            firstItemHeader.addClass("active");
            firstItemHeader.next(".js-itemMenu").addClass("active");
        }

        $("#toc").accordion({
            // exclusive:false
        });
        // $("#toc_loader").removeClass("active")

    }
}

var menuScroll = {
    init: function () {
        $("#toc").on("scroll", function () {
            window.sessionStorage.setItem("menuScroll", $("#toc").scrollTop());
        });

        var lastScroll = 0,
            sessionScroll = window.sessionStorage.getItem("menuScroll");
        if (sessionScroll) {
            lastScroll = sessionScroll;
        }
        $("#toc").scrollTop(lastScroll);
    }
}


menuRouter.init();
//todo 开启上次滚动记录后左侧菜单还是会抖动，用户体验很不好
// menuScroll.init();

})

