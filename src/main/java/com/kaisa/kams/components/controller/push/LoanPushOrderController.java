package com.kaisa.kams.components.controller.push;

import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.params.push.LoanPushApproval;
import com.kaisa.kams.components.service.push.BillLoanPushService;
import com.kaisa.kams.components.service.push.LoanPushOrderApprovalService;
import com.kaisa.kams.components.service.push.LoanPushOrderService;
import com.kaisa.kams.components.service.push.LoanPushService;
import com.kaisa.kams.components.utils.ApiRequestUtil;
import com.kaisa.kams.components.view.push.LoanPushOrderView;
import com.kaisa.kams.enums.ApprovalCode;
import com.kaisa.kams.enums.push.ItemType;
import com.kaisa.kams.enums.push.LoanPushOrderStatus;
import com.kaisa.kams.enums.push.PushRepayMethodType;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.push.BillLoanPush;
import com.kaisa.kams.models.push.LoanPush;
import com.kaisa.kams.models.push.LoanPushOrder;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.trans.Molecule;
import org.nutz.trans.Trans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.entity.Task;

import java.util.List;

/**
 * 推单管理
 *
 * @author Created by pengyueyang on 2016/12/12.
 */
@IocBean
@At("/loan_push_order")
@Ok("json")
public class LoanPushOrderController {

    private final static Logger log = LoggerFactory.getLogger(LoanPushOrderController.class);

    @Inject
    private LoanPushService loanPushService;
    @Inject
    private LoanPushOrderService loanPushOrderService;
    @Inject
    private BillLoanPushService billLoanPushService;
    @Inject
    private LoanPushOrderApprovalService loanPushOrderApprovalService;

    @POST
    @At("/query_user")
    @Ok("raw")
    public String queryUser(@Param("mobile") String mobile) {
        return ApiRequestUtil.queryUserByAPI(mobile);
    }

    @GET
    @At("/index")
    @Ok("beetl:/push/order_list.html")
    public void index() {
    }

    @POST
    @At("/list")
    public NutMap list(@Param("loanPushId") String loanPushId) {
        NutMap result = new NutMap();
        result.put("base", loanPushService.getLoanPushById(loanPushId));
        result.put("loanPushOrderList", loanPushOrderService.getListByLoanPushId(loanPushId));
        result.put("ok", true);
        return result;
    }

    @GET
    @At("/attach_download")
    @Ok("beetl:/push/attach_download.html")
    public void attachDownload() {
    }

    @POST
    @At("/attach_download_list")
    public NutMap attachDownloadList(@Param("loanId") String loanId) {
        NutMap result = new NutMap();
        result.put("attachList", loanPushService.getAttachList(loanId));
        result.put("ok", true);
        return result;
    }

    @GET
    @At("/add")
    @Ok("beetl:/push/order_add.html")
    public void add() {
    }

    @POST
    @At("/add_init")
    public NutMap addInit(@Param("loanPushId") String loanPushId) {
        NutMap result = new NutMap();
        LoanPush loanPush = loanPushService.getLoanPush(loanPushId);
        if (null == loanPush) {
            result.setv("ok", false);
            return result;
        }
        result.put("content", loanPushOrderService.getInitContent(loanPush));
        result.put("baseInfo", loanPushOrderService.getInitInfo(loanPush));
        if (ItemType.isBill(loanPush.getItemType())) {
            result.put("billLoanPushList", billLoanPushService.getBillLoanPushListByPushId(loanPushId));
        }
        result.put("pushRepayMethods", PushRepayMethodType.toMap());
        result.put("ok", true);
        return result;
    }

    @GET
    @At("/edit")
    @Ok("beetl:/push/order_edit.html")
    public void edit() {
    }

    @POST
    @At("/get_detail")
    public NutMap getDetail(@Param("id") String id) {
        NutMap result = new NutMap();
        LoanPushOrder order = loanPushOrderService.getLoanPushOrderById(id);
        if (null == order) {
            result.setv("ok", false);
            return result;
        }
        LoanPush loanPush = loanPushService.getLoanPush(order.getPushId());
        if (null == loanPush) {
            result.setv("ok", false);
            return result;
        }
        if (ItemType.isBill(order.getItemType())) {
            result.put("billLoanPushList", billLoanPushService.getBillLoanPushListByPushAndOrderId(order.getPushId(), id));
        }
        result.put("loanAmount", loanPush.getAmount());
        result.put("pushRepayMethods", PushRepayMethodType.toMap());
        result.put("order", order);
        result.put("ok", true);
        return result;
    }

    @POST
    @At("/save")
    @AdaptBy(type = JsonAdaptor.class)
    public NutMap save(@Param("order") LoanPushOrder order,
                       @Param("billLoanPushList") BillLoanPush[] billLoanPushes,
                       @Param("submit") boolean submit) {
        NutMap result = new NutMap();
        if (submit && !loanPushOrderApprovalService.isConfigFlow(order)) {
            result.put("ok", false);
            result.put("msg", "请先配置并启动相关流程！");
            return result;
        }
        try {
            boolean isBill = ItemType.isBill(order.getItemType());
            LoanPushOrder loanPushOrder = addLoanPushOrder(order, billLoanPushes, isBill);
            if (isBill) {
                result.put("billLoanPushList", billLoanPushService.getBillLoanPushListByPushAndOrderId(order.getPushId(), loanPushOrder.getId()));
            }
            if (submit) {
                boolean startFlowResult = loanPushOrderApprovalService.startFlow(loanPushOrder);
                if (!startFlowResult) {
                    result.put("ok", false);
                    result.put("msg", "推单审批流程启动失败！");
                    return result;
                }
                loanPushOrderApprovalService.updateLoanPushOrderStatus(loanPushOrder.getId(), LoanPushOrderStatus.APPROVAL);
            }
            result.put("ok", true);
        } catch (Exception e) {
            log.error("Save push order error:{}", e.getMessage());
            result.put("ok", false);
            result.put("msg", "推单管理-后台处理异常，请联系管理员！");
        }
        return result;
    }

    @POST
    @At("/node_approval")
    public NutMap nodeApproval(@Param("..") LoanPushApproval loanPushApproval) {
        NutMap result = new NutMap();
        result.put("ok", true);
        if (!checkLoanPushApproval(loanPushApproval, result)) {
            result.put("ok", false);
            return result;
        }
        if (!loanPushOrderApprovalService.nodeApproval(loanPushApproval)) {
            result.put("ok", false);
            result.put("msg", "审批失败请联系后台开发人员");
        }
        return result;
    }

    @GET
    @At("/approval_list")
    @Ok("beetl:/push/approval_list.html")
    public void userApprovalList() {
    }

    @POST
    @At("/get_approval_list")
    public DataTables getUserApprovalList(@Param("..") DataTableBaseParam param) {
        return loanPushOrderApprovalService.getUserApprovalList(param);
    }

    @GET
    @At("/approved_list")
    @Ok("beetl:/push/approved_list.html")
    public void userApprovedList() {
    }

    @POST
    @At("/get_approved_list")
    public DataTables getUserApprovedList(@Param("..") DataTableBaseParam param) {
        int count = loanPushOrderApprovalService.getUserApprovedCount();
        List<LoanPushOrderView> list = loanPushOrderApprovalService.getUserApprovedList(param);
        return new DataTables(param.getDraw(), count, count, list);
    }

    @GET
    @At("/approval_detail")
    @Ok("beetl:/push/approval_detail.html")
    public void approvalDetail() {
    }

    @GET
    @At("/detail")
    @Ok("beetl:/push/detail.html")
    public void detail() {
    }

    @POST
    @At("/get_approval_detail")
    public NutMap getApprovalDetail(@Param("orderId") String orderId, @Param("taskId") String taskId) {
        NutMap result = new NutMap();
        result.put("ok", false);
        LoanPushOrder order = loanPushOrderService.getLoanPushOrderById(orderId);
        if (null == order || !LoanPushOrderStatus.APPROVAL.equals(order.getStatus())) {
            return result;
        }
        if (ItemType.isBill(order.getItemType())) {
            result.put("billLoanPushList", billLoanPushService.getBillLoanPushListByPushAndOrderId(order.getPushId(), orderId));
        }
        result.put("order", order);
        Task approvalInfo = loanPushOrderApprovalService.getApprovalInfo(taskId);
        if (null == approvalInfo) {
            return result;
        }
        result.put("approvalInfo", approvalInfo);
        result.put("ok", true);
        return result;
    }

    private LoanPushOrder addLoanPushOrder(LoanPushOrder order, BillLoanPush[] billLoanPushes, boolean isBill) {
        return Trans.exec(new Molecule<LoanPushOrder>() {
            @Override
            public void run() {
                LoanPushOrder loanPushOrder = order;
                if (StringUtils.isEmpty(order.getId())) {
                    loanPushOrder = loanPushOrderService.save(order);
                } else {
                    loanPushOrderService.updateByEdit(order);
                }
                if (isBill) {
                    billLoanPushService.updateBillLoanPush(billLoanPushes, loanPushOrder.getId());
                }
                setObj(loanPushOrder);
            }
        });
    }

    private boolean checkLoanPushApproval(LoanPushApproval loanPushApproval, NutMap result) {
        if (StringUtils.isEmpty(loanPushApproval.getLoanPushOrderId())) {
            result.put("msg", "当前审批单不存在");
            return false;
        }

        if (StringUtils.isEmpty(loanPushApproval.getOrderId())) {
            result.put("msg", "节点订单Id不能为空");
            return false;
        }

        if (StringUtils.isEmpty(loanPushApproval.getTaskId())) {
            result.put("msg", "节点任务Id不能为空");
            return false;
        }

        if (ApprovalCode.AGREE == loanPushApproval.getApprovalCode() && StringUtils.isEmpty(loanPushApproval.getContent())) {
            result.put("msg", "审批结果内容不能为空");
            return false;
        }
        return true;
    }


}
