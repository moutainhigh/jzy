/**
 * Created by pengs on 2016/11/25.
 */
/* Define API endpoints once globally */
$.fn.api.settings.successTest = function (response) {
    if (response) {
        if (response.ok === undefined) {
            return true
        } else if (response.ok === true) {
            return true
        } else if (response.ok === false) {
            return false
        }
    }
}

$.fn.api.settings.errorDuration = 1;

$.fn.api.settings.api = {};

// //组织 老接口
// $.extend($.fn.api.settings.api, {
//     'add orgnazition': '/organize/save_organize',
//     'edit orgnazition': '/organize/update_organize',
//     'get orgnaztion': '/organize/list_organize',
//     'delete orgnazition': '/organize/delete_organize',
//     'get orgnaztion datails': '/organize/fetch_organize',
// });


//人员
$.extend($.fn.api.settings.api, {
    'add user origin': '/user/save_user',
    'find user password': '/user/findPassword',
    'check login': '/user/checkLogin',
    'update user origin': '/user/update_user',
    'get UserById': '/user/fetch_user',
    'get userByOrg': '/user/list_user_Id',
    'get userListAll': '/user/list_all',
    'query user': '/user/list_user'
});

//角色
$.extend($.fn.api.settings.api, {
    'get roleList': '/role/list',
    'get roleUserList': '/role/user_list_by_role_id',
    'get roleListByName': '/role/list_by_name',
    'get roleDataTable' : '/role/get_table',
    'add role': '/role/add',
    'update roleUser': '/role/update_user',
    'update role': '/role/update',
    'delete roleMenu': '/role/delete_role_menu',
    'delete role': '/role/delete',
    'get role channelList':'/user/get_channel',
    'get role productList':'/user/get_product'
});

//菜单
$.extend($.fn.api.settings.api, {
    'get menuList': '/role/menu_list_all',
    'get roleMenuList': '/role/menu_list_by_role_id',
    'query menu': '/menu/list_by_name',
    'delete menu': '/menu/delete',
    'get parentMenuList': '/menu/list_parent',
    'add menu': '/menu/save_menu',
    'update menu': '/menu/update_menu',
    'get MenuById': '/menu/fetch_menu',
    'test permission button':'/permission/approval',
    'test permission button test':'/permission/test'
});


//产品大类
$.extend($.fn.api.settings.api, {
    'save productType': '/product_type/add',
    'edit productType': '/product_type/update',
    'get productType': '/product_type/listAll',
    'delete productType': '/product_type/delete'
});
//影像资料
$.extend($.fn.api.settings.api, {
    'query mediaTemp': '/media_temp/list_media_temp',
    'add mediaTemp': '/media_temp/save_media_temp',
    'update mediaTemp': '/media_temp/update_media_temp',
    'fetch mediaTempById': '/media_temp/fetch_media_temp',
    'query mediaItem':'/media_temp/list_mediaItem'
});

//机构
$.extend($.fn.api.settings.api, {
    'get agencies': '/business_agency/list_agency_tree',
    'fetch agency datails': '/business_agency/fetch_agency',
    'add agency': '/business_agency/save_agency',
    'update agency': '/business_agency/update_agency',
    'search agency': '/business_agency/list_agency',
    'get agency id':'/business_agency/fetch_agency'
});

//影像上传管理
$.extend($.fn.api.settings.api, {
    'list mediaTemplate': '/media_upload/listMediaTemplate',
    'update media': '/media_upload/upload',
    'delete byLoanIdAndItemName': '/media_upload/updateByLoanIdAndItemName'
});

//放款主体信息维护
$.extend($.fn.api.settings.api, {
    'delete loanSubject': '/loan_subject/delete',
    'add loanSubject': '/loan_subject/save',
    "update loanSubject":'/loan_subject/update',
    'fetch loanSubject': '/loan_subject/fetch_by_id'
});

//组织 新接口
$.extend($.fn.api.settings.api, {
    'search organizition': '/business_organize/list_organize_search',
    'get organizition': '/business_organize/list_organize',
    'get business line': "/business_organize/get_business_line",
    'get organizition code': '/business_organize/get_org_code',
    'add organizition': '/business_organize/save_organize',
    'update organizition': '/business_organize/update_organize',
    'get organizition by id': '/business_organize/fetch_organize',

});
//业务人员
$.extend($.fn.api.settings.api, {
    'search b_user': '/business_user/list_user_search',
    'query b_user': '/business_user/list_user',
    'get b_user position': '/business_user/get_user_position',
    'get b_user credentials':'/business_user/get_user_credentials',
    'add b_user':'/business_user/save_user',
    'update b_user':'/business_user/update_user',
    'get b_user code':'/business_user/get_user_code',
    'get b_user by id':'/business_user/fetch_user',
    'add b_user account':'/business_user/create_user_account'
});

//贷后管理
$.extend($.fn.api.settings.api, {
    'get postLoan': '/post_loan/list_post_loan',
    'fetch postLoan': '/post_loan/fetch_post_loan'
});

//产品
$.extend($.fn.api.settings.api, {
    'get productList': '/product/listAll',
    'uploadShulou product':'/productImport/uploadShulou',
    'get importShulouList':'/productImport/listShulou',
    'uploadChedai product':'/productImportCheDai/uploadChedai',
    'get importChedaiList':'/productImportCheDai/listChedai',
    'get importRenRenCheList':'/productImportRenRenChe/listRenRenChe',
    'get importHongbenList':'/productImport/listHongben',
    'get importBaoliList':'/productImport/listBaoli',
    'get importPiaojuList':'/productImport/listPiaoju'
});


//借款人
$.extend($.fn.api.settings.api, {
    'fetch borrowerByCertifNumber': '/borrower/fetch_by_certifNumber',
    'query listByCertifNumber':'/borrower/query_list_by_certifNumber'
});

//业务申请 查看
$.extend($.fn.api.settings.api, {
    'get prdUser': '/product/get_prd_user',
    'add borrowers': '/business_apply/add_borrowers',
    'update borrowers': '/business_apply/update_borrowers',
    'update loanStatus': '/business_apply/update_loanStatus',
    'get base': '/business_apply/fetch_base',
    'get loan': '/business_apply/fetch_loan',
    'get businessInfo': '/business_apply/query_business_info',
    'get process list':'/business_apply/query_process_list',
    'get business channel list':'/business_apply/query_channel_list',
    'channel apply back':'/business_apply/ channel_back',
    'get process channel list':'/business_apply/query_channel_process_list',
    'get risk  media manifest':'/business_apply/query_risk_media_manifest',
    'get extension risk media manifest':'/business_apply/query_risk_media_manifest_list',
    'update risk  media':'/business_apply/update_risk_media',
    'get approval list':'/business_apply/query_approval_list',
    'update loan': '/business_apply/update_loan',
    'update loan fee': '/business_apply/update_loan_fee',
    'update loan business': '/business_apply/update_loan_business',
    'update loan business media': '/business_apply/update_loan_business_media',
    'submit loan': '/business_apply/submit_loan',
    'new loan': '/business_apply/new_loan',
    'update loan borrower': '/business_apply/update_loan_borrower',
    'update loan account': '/business_apply/update_loan_account',
    'query loan fee': '/business_apply/query_loanfee',
    'add bill loan':'/bill_business_apply/add_bill',
    'add bill loan submit':'/bill_business_apply/add_bill_submit',
    'query by name':'/borrower/query_by_name',
    'query by name type':'/borrower/query_by_name_type',
    'bill add borrower':'/bill_business_apply/add_borrower',
    'add bill repay':'/bill_business_apply/add_bill_repay',
    'query bill':'/bill_business_apply/query_bill',
    'borrwer bill media':'/bill_media_attach/query_bill_media_by_borrwer'
});

//流程审批
$.extend($.fn.api.settings.api, {
    'query approval': '/flow/query_approval',
    'query user approval': '/flow/query_user_approval',
    'flow node approval':'/flow/node_approval',
});

//流程设置
$.extend($.fn.api.settings.api, {
    'flow control list': '/flow_control/list',
    'query product': '/product/queryByType',
    'flow control update': '/flow_control/update',
    'flow control update financal': '/flow_control/update/financal'
});

//待放款列表
$.extend($.fn.api.settings.api, {
    'query loan list': '/loan/query_loan_list',
    'fetch loan info': '/loan/fetch_loan_info',
    'loan confirm': '/loan/confirm',
    'query loan repay':'/loan/query_loan_repay',
    'query gather loan list':'/loan/query_gather_loan_list',
    'query loan element list':'/loan/query_loan_element_list',
    'get pre repayment detail':'/loan/can_pre_repayment_list',//提前还款详情
    'clear pre repayment':'/loan/pre_repayment'
});


//推单管理
$.extend($.fn.api.settings.api, {
    'query loanPushing': '/loan_push/loan_push_list',
    'query loanPushed': '/loan_push/query_loan_push',
    'add loanPush': '/loan_push/add',
    'update loanPush': '/loan_push/update',
    'fetch loanPushDetail': '/loan_push/fetch_loan_push'
});

//渠道
$.extend($.fn.api.settings.api, {
    'query channels': '/channel/get_channels_by_name'
});

//业务展期
$.extend($.fn.api.settings.api,{
    'extension insert':'/extension/submit_approval', //新增展期
    'extension query history':'/business_extension/query_history',
    'query extension loan list':'/business_extension/query_loan_list',
    'extension loan repay':'/business_extension/generate_loan_repay'
});

//渠道
$.extend($.fn.api.settings.api, {
    'get channel list':'/channel/list_by_channel_name',
    'get channel code':'/channel/get_channel_code',//获取渠道code
    'get channel detail':'/channel/fetch_by_id',//渠道详情
    'get product tree':'/product_type/list_productType_tree', //产品树
    'save channel':'/channel/save_channel', //新增渠道
    'update channel':'/channel/update_channel', //更新渠道
});

/*贴现企业*/
$.extend($.fn.api.settings.api, {
    'get borrower list':'/borrower/discountCompany_list',
    'save borrower':'/borrower/add_discount', //新增
    'update borrower':'/borrower/update_discount', //更新
    'view borrower':'/borrower/fetch_by_Id' //查看
});


//企业管理
$.extend($.fn.api.settings.api,{
    'add enterprise':'/enterprise/add_enterprise',
    'update enterprise':'/enterprise/update_enterprise',
    'query enterprise':'/enterprise/query_enterprise_by_name',
    'query credit company':'/enterprise/query_enterprise_by_name_type'
});

//回款账户管理
$.extend($.fn.api.settings.api,{
    'add account':'/account/add_account',
    'update account':'/account/update_account',
    'query account':'/account/query_account_by_name'
});

//个人贷历史数据导入
$.extend($.fn.api.settings.api, {
    'uploadGerendai product':'/gerendaiImport/uploadGerendai',
    'get importGerendaiList':'/gerendaiImport/listGerendai',
    'get importXinyongdaiList':'/gerendaiImport/listXinyongdai'
});

//报表管理
$.extend($.fn.api.settings.api, {
    'query finance report':'/report/report_list',
    'export finance report':'/report/report_export',
    'query business report':'',
    'export business report':'',
    'query risk report':'/report/riskControl_list', //风控报表
    'query b_cd report':'/business_report/business_chedai_list',//业务-车贷
    'query b_sl report':'/business_report/business_shulou_list',//业务-赎楼
    'query b_yg report':'/business_report/business_gerendai_list',//业务-员工贷
    'query b_hb report':'/business_report/business_hongben_list',//业务-红本
    'query b_pj report':'/business_report/business_bill_list',//业务-票据
    'query b_zh report':'/report/comprehensiveReport_list', //综合报表
    'query b_zhd report' :'/report/comprehensive_day_report_list',//综合日报表
    'query finance daily report':'/report/finance_daily_report_list',
    'finance daily report export':'/report/finance_daily_report_export'
});

//查询居间人
$.extend($.fn.api.settings.api, {
    'query intermediary':'/intermediary/query_by_name',
    'query intermediary list':'/intermediary/intermediary_list', //居间人列表
    'query intermediary detail':'/intermediary/fetch_by_id', //居间人详情（只能查到未失效）
    'query intermediary detail_rel':'/intermediary/fetch_by_Id', //居间人详情
    'add intermediary':'/intermediary/add' //新增居间人
});

//查询产品大类和子类
$.extend($.fn.api.settings.api, {
    'product type cascade':'/product_type_cascade/get_list',
});

//用印
$.extend($.fn.api.settings.api, {
    'seal detail':'/seal/init_seal',
});

//
$.extend($.fn.api.settings.api,{
    'profit data list':'/product/profit_calculation_data_page', //列表
    'get profit by id':'/product/get_product_profit_config', //获取详情
    'add profit':'/product/add_product_profit_config',
    'edit profit':'/product/edit_product_profit_config',
    'view profit':'/product/view_loan_profit' //查看利润
})


//资产管理
$.extend($.fn.api.settings.api,{
    'manage house list':'/house_manage/house_manage_list',
    'view house manage':'/house_manage/fetch_houseManage', //查看利润（houseId，id，loanId）
    'update house manage':'/house_manage/update', //更新（id，和相应的值）
    'init house manage':'/house_manage/init_houseManage' //初始化弹窗（houseId，id）
})

//流程配置
$.extend($.fn.api.settings.api,{
    'flow list':'/flow_configure_control/list',  //流程维护列表
    'get flow type':'/flow_configure_control/flowTypeList', //获取流程类型
    'get flow modal':'/flow_configure_control/flowModuleList',//获取业务模块
    'flow settings update':'/flow_configure_control/update',
    'flow detail':'/flow_configure_control/get',//详情
    'start flow':'/flow_configure_control/start',//启用
    'stop flow':'/flow_configure_control/stop',//禁用
    'get product list':'/product/queryAvailableProductList',//获取关联产品
});

/*
* 居间费付费申请
* */
$.extend($.fn.api.settings.api,{
    'intermediary apply list':'/bill_intermediary_apply/query_pending_list',//待申请列表
    'get intermediary info':'/bill_intermediary_apply/makeup_intermediary_init',//获取补录信息
    'save intermediary info':'/bill_intermediary_apply/makeup_intermediary',//保存
    'submit intermediary info':'/bill_intermediary_apply/submit_intermediary',//提交
    'query bill info':'/bill_intermediary_apply/query_bill',//获取bill详情
    'approval list':'/bill_intermediary_apply/query_process_list',//待审批
    'approved list':'/bill_intermediary_apply/query_process_approved_list',//已审批
    'intermediary loan list':'/bill_intermediary_apply/query_loan_list',
    'confirm loan info':'/bill_intermediary_apply/init_loan',
    'confirm loan':'/bill_intermediary_apply/confirm_loan',//放款
});


//解压流程
$.extend($.fn.api.settings.api,{
    'noMortgage apply list':'/house_noMortgage_apply/query_pending_list',
    'noMortgage apply init':'/house_noMortgage_apply/apply_init',//匹配单号
    'save house_noMortgage':'/house_noMortgage_apply/add_houseNoMortgageApply',
    'cancle house_noMortgage':'/house_noMortgage_apply/update_status',
    'query noMortgage detail':'/house_noMortgage_apply/query_detail',
    'noMortgage approved list':'/house_noMortgage_apply/query_process_approved_list',//已审批
    'noMortgage approval list':'/house_noMortgage_apply/query_process_list',//待审批
    'noMortgage file list':'/house_noMortgage_apply/document_list'
});

//抵押流程
$.extend($.fn.api.settings.api,{
    'mortgage apply list':'/mortgage/list',
    'mortgage apply update':'/mortgage/update',
    'get mortgage detail':'/mortgage/get',
    'submit mortgage approval':'/mortgage/submit_approval',
    'cancel mortgage approval':'/mortgage/cancel',
    'mortgage approval list':'/mortgage/approval_list',
    'mortgage approved list':'/mortgage/approval_complete_list',
    'mortgage file list':'/mortgage/mortgage_document_list'
});

//展期审批流程
$.extend($.fn.api.settings.api,{
    'extension approval list':'/extension/approval_list',
    'extension complete list':'/extension/approval_complete_list',
    'get extension detail':'/extension/get',
});


//业务单关联
$.fn.extend($.fn.api.settings.api,{
    'history loan relation':'/history_loan_relation/query_history_loan_relation',
    'relation loan code':'/history_loan_relation/relation_loan_by_code',
    'del relation code':'/history_loan_relation/del_history_loan_relation',
});

//关联房产抵押
$.fn.extend($.fn.api.settings.api,{
    'query loanMortgage':'/business_apply/query_loanMortgage',
    'del loanMortgage':'/business_apply/deleteByMortgageCode',
    'add loanMortgage':'/business_apply/add_loanMortgage',
    'query loan mortgage':'/business_apply/query_loanMortgage'
});

//费用减免
$.fn.extend($.fn.api.settings.api,{
    'get cost apply list':'/cost_exemption/list',
    'get cost apply detail':'/cost_exemption/getLoan',
    'get cost detail':'/cost_exemption/get',
    'submit cost apply':'/cost_exemption/update',
    'get cost loan repay':'/cost_exemption/loan_repay_list',
    'cost loan repay fee':'/cost_exemption/loan_repay_fee',
    'get loan record':'/loan/query_repay_record',//还款记录
    'submit cost approval':'/cost_exemption/submit_approval',
    'cancel cost approval':'/cost_exemption/delete',
    'cost approval list':'/cost_exemption/approval_list',
    'cost complete list':'/cost_exemption/approval_complete_list'
});

//推单
$.fn.extend($.fn.api.settings.api,{
    'get loan push list':'/loan_push/get_list',
    'get loan order list':'/loan_push_order/list',
    'get loan order add info':'/loan_push_order/add_init',//进入新增推单获取基础信息
    'get platform borrower info':'/loan_push_order/query_user',//获取平台借款人信息
    'order save':'/loan_push_order/save',
    'get order detail':'/loan_push_order/get_detail',
    'order file list':'/loan_push_order/attach_download_list',
    'order approval list':'/loan_push_order/get_approval_list',
    'order approved list':'/loan_push_order/get_approved_list',
    'order approval detail':'/loan_push_order/get_approval_detail',
    'order node approval':'/loan_push_order/node_approval',
    'order get status':'/loan_push/get_status',
    'order complete push':'/loan_push/complete_push'
});