/**
 * Created by wangqx on 2016/12/8.
 */
var enums = {
    public_status: {
        "DISABLED": "不生效",
        "ABLE": "生效"
    },

    public_status: {
        "DISABLED": "不生效",
        "ABLE": "生效"
    },

    business_type: {
        "PUBLIC": "对公业务",
        "PRIVATE": "对私业务"
    },

    //借款用途
    use_of_loan:{
        "DAY_COST":"日常消费",
        "CASH_COST":"资金周转"
    },

    guaranty_type : {
        "MORTGAGE": "抵押",
        "PLEDGE": "质押",
        "GUARANTY": "保证",
        "COUNTER_GUARANTY": "反担保",
        "NO_GUARANTY": "无担保"
    },

    business_line: {
        "CAR_LOAN": "车贷",
        "HOUSE_LOAN": "房贷",
        "CREDIT_LOAN": "信贷",
        "PUBLIC_LOAN": "对公",
        "CHANNEL_LOAN": "渠道",
        "GROUP_LOAN": "集团"
    },

    status: {
        "ABLE": "生效",
        "DISABLED": "失效"
    },

    Loan_repay_status: {
        "LOANED": "还款中",
        "CLEARED": "已还清",
        "OVERDUE": "已逾期",
        "OVERDUE_CLEARED": "逾期还清",
        "AHEAD_CLEARED": "提前还清"
    },
    organizeType: {
        "AGENCY": "部",
        "ORGANIZE": "组"
    },
    certifType:{
        "ID":"身份证",
        "DRIVE":"驾驶证",
        "BUSINESS_LICENSE":"营业执照编号",
        "PASS_ID":"港澳台证件号"
    },

    seal_status: {
        "USED": "已用印",
        "PREUSED": "拟用印",
        "UNUSED": "未用印"
    },
    // certifType: {
    //     ID: "身份证",
    //     DRIVE: "驾驶证"
    // },
    position: {
        "S1": "见习业务经理",
        "S2": "业务经理",
        "S3": "初级业务经理",
        "S4": "中级业务经理",
        "S5": "高级业务经理",
        "S6": "资深业务经理",
        "M1": "经理",
        "M2": "高级经理",
        "M3": "资深经理",
        "D1": "总监",
        "D2": "高级总监",
        "D3": "资深总监"
    },
    feeChargeNode: {
        "LOAN_NODE": "放款时收取",
        "REPAY_NODE": "还款时收取"
    },
    feeChargeType: {
        "FIXED_AMOUNT": "固定金额",
        "LOAN_AMOUNT_RATE": "贷款本金*比例",
        "REMAIN_PRINCIPAL_RATE": "剩余本金*比例",
        "OVERDUE_PRINCIPAL_RATE": "逾期本金*比例",
        "OVERDUE_REPAYMENT_RATE": "逾期本息*比例",
        "LOAN_REQUEST_INPUT": "前端录单输入"
    },

    feeCycleType: {
        "ONE_TIME": "一次性收取",
        "MONTHLY": "每期收取",
    },

    loanTermType: {     //用于列表数据渲染
        "YEAS": "年",
        "MOTHS": "个月",
        "DAYS": "天",
        "FIXED_DATE": "",
        "SEASONS":'季'
    },

    loanTermTypeForRate: {
        "YEAS": "年",
        "MOTHS": "月",
        "DAYS": "天",
        "FIXED_DATE": "天",
        "SEASONS":'季'
    },
    loan_term_type:{
        "YEAS": "年",
        "MOTHS": "月",
        "DAYS": "天",
        "FIXED_DATE": "固定时间",
        "SEASONS":'季'
    },

    feeType: {
        "SERVICE_FEE": "借款服务费",
        "GUARANTEE_FEE": "借款担保费",
        "MANAGE_FEE": "资金管理费",
        "OVERDUE_FEE": "逾期罚息",
        "PREPAYMENT_FEE": "提前结清罚息",
        "PREPAYMENT_FEE_RATE": "一次性服务费"
    },

    loanRepayMethod: {
        "INTEREST_MONTHS": "先息后本（按月）",
        "INTEREST_DAYS": "先息后本（按天）",
        "EQUAL_INSTALLMENT": "等额本息",
        "BULLET_REPAYMENT": "一次性还本付息",
        "INTEREST_SEASONS":"按季付息，到期还本",
        "ALL": "全部"
    },
    businessCredentialsType: {
        "SFZ": "身份证"
    },

    approval_code_code: {
        "AGREE": "01",
        "BACKPRE": "02",
        "BACKBEGIN": "03",
        "DISAGREE": "04"
    },

    approval_code_desc: {
        "AGREE": "同意",
        "BACKPRE": "退回上一步",
        "BACKBEGIN": "退回",
        "DISAGREE": "拒绝"
    },

    pushTarget: {
        "KAISAFAX": "佳兆业金服"
    },

    loan_status:{
        "CHANNELSAVE":"渠道编辑中",
        "SAVE":"编辑中",
        "CANCEL":"已取消",
        "SUBMIT":"审批中",
        "APPROVEREJECT":"已拒绝",
        "APPROVEEND":"等待放款",
        "LOANED":"还款中",
        "WAITPUSH":"待推单",
        "PARTPUSH":"部分推单",
        "PUSHED":"推单完成",
        "CLEARED":"已还清",
        "OVERDUE":"已逾期",
        "LOANCANCEL":"取消放款"
    },

    loan_push_status:{
        "UNPUSHED":"未推单",
        "PUSHED":"推单完成"
    },

    relation:{
        "1":"本人",
        "2":"配偶",
        "3":"亲属",
        "4":"合作人",
        "5":"合作伙伴",
        "6":"其他"
    },

    people_type:{
        "0":"受薪人士",
        "1":"经营企业"
    },

    push_status:{
        "WAITPUSH":"待推单",
        "PARTPUSH":"部分推单",
        "PUSHED":"推单完成"
    },

    pushTermType: {
        "YEAS": "年",
        "MOTHS": "月",
        "DAYS": "日",
        "FIXED_DATE": "固定还款日"
    },

    repayDateType:{
        "REPAY_PRE": "期初收息",
        "REPAY_SUF": "期末收息"
    },

    productTempType:{
        "HONGBEN":"红本",
        "SHULOU":"赎楼",
        "PIAOJU":"票据"
    },

    calculateMethodAboutDay:{
        "CALCULATE_HEAD_NOT_TAIL": "算头不算尾",
        "CALCULATE_TAIL_NOT_HEAD": "算尾不算头",
        "CALCULATE_HEAD_AND_TAIL": "算头算尾"
    },
    element:{
        "NOT_CONTROL":"未控制",
        "CONTROL":"已控制",
    },

    channelType:{
        "0":"自营",
        "1":"渠道"
    },

    loanLimitType:{
        'FIX_RATE':'比例计息(%)',
        'FIX_AMOUNT':'金额计息(元)'
    },

    StorageStatus:{
        "IN":"入库",
        "OUT":"出库"
    },

    PropertyRightStatus:{
        "SECURED":"已抵押",
        "SOLVED":"已解押",
        "WAITSECURED":"待抵押",
        "WAITSOLVED":"待解押"
    },

    MortgageType:{
        "MAXMORTGAGE":"最高额抵押",
        "GENMORTGAGE":"一般抵押"
    },

    amountType:{
        'LOAN_AMOUNT':'借款金额',
        'INTERMEDIAR_FEE':'居间费'
    },

    addressType:{
        'SZMORTGAGE':'深圳房产解押',
        'GZMORTGAGE':'广州房产解押'
    },

    houseMortgageType:{
        'SHENZHEN':'深圳房产抵押',
        'GUANGZHOU':'广州房产抵押'
    },

    //解压抵押公有状态枚举
    approvalStatusTypeList:{
        'IN_EDIT':'编辑中',
        'IN_APPROVAL':'审批中',
        'APPROVED':'已审批',
        'REJECT':'已拒绝',
        'CANCEL':'已取消'
    },

    feeList: {
        'prepaymentFee': '提前结清罚息',
        'prepaymentFeeRate': '一次性服务费',
        'overdueFee': '逾期罚息',
        'manageFee': '资金管理费',
        'guaranteeFee': '借款担保费',
        'serviceFee': '借款服务费',
        'interest': '利息'
    },

    psuh_order_status :{
        "EDIT":"编辑中",
        "APPROVAL":"审批中",
        "REJECTED":"审批拒绝",
        "PUSHED":"已推送",
        "PLATFORM_REJECTED":"平台审批拒绝",
        "SCHEDULED":"已排期",
        "FUNDING":"筹款中",
        "FULL_FUNDED":"已满标",
        "LOANED":"还款中",
        "OVERDUE":"已逾期",
        "CLEARED":"已还清"
    },


    itemType:{
        "CAR_LOAN":'车贷项目',
        "HOUSE_MORTGAGE_LOAN":"红本项目",
        "BANK_HOUSE_LOAN":"赎楼项目",
        "PERSONAL_LOAN":"个人贷项目",
        "FACTORING": "保理项目",
        "BILL":"票据项目",
        "BANK_BILL":"票据项目"
    },

    orderRepayMethod:{
        BULLET_REPAYMENT:"一次性还款",
        EQUALITY:"等额本金",
        EQUAL_INSTALLMENT: "等额本息",
        INTEREST:"先息后本",
    }

}