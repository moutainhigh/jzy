package com.kaisa.kams.components.controller.base;

import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.service.push.LoanPushService;
import com.kaisa.kams.models.flow.FlowControlItem;
import com.kaisa.kams.models.flow.FlowControlTmpl;
import com.kaisa.kams.components.service.flow.FlowControlItemService;
import com.kaisa.kams.components.service.flow.FlowControlTmplService;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.*;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.flow.ApprovalResult;

import com.kaisa.kams.models.flow.IntermediaryApplyLoanedResult;
import com.kaisa.kams.models.flow.LoanedResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;

import java.util.*;

/**
 * Created by pengyueyang on 2017/1/6.
 */
@IocBean
public class FlowBaseController {

    @Inject
    protected LoanService loanService;

    @Inject
    protected UserService userService;

    @Inject
    protected LoanOrderService loanOrderService;

    @Inject
    protected FlowService flowService;

    @Inject
    protected ApprovalResultService approvalResultService;

    @Inject
    protected LoanedResultService loanedResultService;


    @Inject
    protected ProductService productService;

    @Inject
    protected LoanRiskInfoService loanRiskInfoService;

    @Inject
    protected FlowControlTmplService flowControlTmplService;

    @Inject
    protected FlowControlItemService flowControlItemService;

    @Inject
    protected IntermediaryApplyService intermediaryApplyService;

    @Inject
    protected HouseNoMortgageApplyService houseNoMortgageApplyService;

    @Inject
    protected MortgageService mortgageService;

    @Inject
    private ExtensionService extensionService;

    @Inject
    private CostExemptionService costExemptionService;

    @Inject
    private BusinessExtensionService businessExtensionService;

    @Inject
    private BaseOrderService baseOrderService;

    @Inject
    private BillLoanService billLoanService;

    @Inject
    private LoanPushService loanPushService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowBaseController.class);

    /**
     * 获取当前用户对loanId对应的单的待处理节点
     * @param loanId
     * @return
     */
    public NutMap queryApproval(String loanId, FlowConfigureType flowConfigureType){
        //获取当前登录用户
        User loginUser = ShiroSession.getLoginUser();
        return getUserApproval(loanId, loginUser, flowConfigureType);
    }

    /**
     * 获取当前用户对展期订单
     * @return NutMap
     */
    public NutMap queryApprovalExtension(String extensionId ,FlowConfigureType flowConfigureType){
        Extension extension = extensionService.fetch(extensionId);
        ExtensionOrder extensionOrder = baseOrderService.getBaseOrderById(ExtensionOrder.class,"extensionId",extensionId,flowConfigureType);
        return queryApprovalBase(extension,extensionOrder,flowConfigureType);
    }
    /**
     * 获取当前用户对费用减免订单
     * @return NutMap
     */
    public NutMap queryApprovalCostExemption(String costExemptionId ,FlowConfigureType flowConfigureType){
        CostExemption costExemption = costExemptionService.fetch(costExemptionId);
        CostExemptionOrder costExemptionOrder = baseOrderService.getBaseOrderById(CostExemptionOrder.class,"costExemptionId",costExemptionId,flowConfigureType);
        return queryApprovalBase(costExemption,costExemptionOrder,flowConfigureType);
    }

    public NutMap queryApprovalBase(BaseApproval baseApproval,BaseOrder baseOrder ,FlowConfigureType flowConfigureType){
        User approvalUser = ShiroSession.getLoginUser();
        User user  = userService.fetchLinksById(approvalUser.getId());
        NutMap result = new NutMap();
        if(null!=flowConfigureType&&flowConfigureType.equals(flowConfigureType)){
            if(null==baseApproval||!baseApproval.getApprovalStatusType().equals(ApprovalStatusType.IN_APPROVAL)){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return result;
            }
        }
        List<Role> roles = user.getRoles();
        if(null==roles||roles.isEmpty()){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }
        //根据loanId查询到单信息
        if(null==baseOrder){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        Order order = flowService.getEngine().query().getOrder(baseOrder.getOrderId());
        if(null==order){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        //根据角色Id获取到当前需要处理的节点
        List<WorkItem> majorWorks = getWorkItems(roleIds, order);
        String prodcutId = baseApproval.getProductId();
        if (nextTask(flowConfigureType, user, result, majorWorks, prodcutId)) return result;
        return result;
    }
    /**
     * 获取当前用户对loanId对应的单的待处理节点,针对抵押的审批
     * @param mortgageId
     * @return
     */
    public NutMap queryApprovalMortgage(String mortgageId,FlowConfigureType flowConfigureType){
        User approvalUser = ShiroSession.getLoginUser();
        User user  = userService.fetchLinksById(approvalUser.getId());
        NutMap result = new NutMap();
        Mortgage mortgage  =  mortgageService.getSimpleMortgageById(mortgageId);
        if(null!=flowConfigureType&&flowConfigureType.equals(FlowConfigureType.MORTGAGE)){
            if(null==mortgage||!mortgage.getApprovalStatusType().equals(ApprovalStatusType.IN_APPROVAL)){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return result;
            }
        }
        List<Role> roles = user.getRoles();
        if(null==roles||roles.isEmpty()){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }
        //根据loanId查询到单信息
        MortgageOrder mortgageOrder =  mortgageService.getMortgageOrderByMortgageId(mortgageId,flowConfigureType);
        if(null==mortgageOrder){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        Order order = flowService.getEngine().query().getOrder(mortgageOrder.getOrderId());
        if(null==order){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }
        //根据角色Id获取到当前需要处理的节点
        List<WorkItem> majorWorks = getWorkItems(roleIds, order);
        String prodcutId = mortgage.getProductId();
        if (nextTask(flowConfigureType, user, result, majorWorks, prodcutId)) return result;
        return result;
    }
    public NutMap getUserApproval(String loanId, User approvalUser, FlowConfigureType flowConfigureType) {
        //获取当前用户所有的角色
        User user = userService.fetchLinksById(approvalUser.getId());
        HouseNoMortgageApply houseNoMortgageApply = null;
        NutMap result = new NutMap();
//        FlowConfigureType flowConfigureType = FlowConfigureType.BORROW_APPLY;
        Loan loan = loanService.fetchById(loanId);
        if(null != flowConfigureType && flowConfigureType.equals(FlowConfigureType.BROKERAGE_FEE)){
            IntermediaryApply intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
            if(null==intermediaryApply||intermediaryApply.getLoanStatus()!= LoanStatus.SUBMIT){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return result;
            }
        }else if(null != flowConfigureType && flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            houseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(loanId);
            loanId = houseNoMortgageApply.getId();
            if(null==houseNoMortgageApply||houseNoMortgageApply.getLoanStatus()!= ApprovalStatusType.IN_APPROVAL){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return result;
            }
        }else {
            if(null==loan||loan.getLoanStatus()!= LoanStatus.SUBMIT){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return result;
            }
        }
        List<Role> roles = user.getRoles();

        if(null==roles||roles.isEmpty()){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }

        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }

        //根据loanId查询到单信息
        LoanOrder loanOrder =  loanOrderService.fetchByLoanId(loanId,flowConfigureType);
        if(null==loanOrder){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }

        Order order = flowService.getEngine().query().getOrder(loanOrder.getOrderId());
        if(null==order){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return result;
        }

        //根据角色Id获取到当前需要处理的节点
        List<WorkItem> majorWorks = getWorkItems(roleIds, order);
        String prodcutId = null;
        if(flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            prodcutId = houseNoMortgageApply.getProductId();
        }else {
            prodcutId = loan.getProductId();
        }

        if (nextTask(flowConfigureType, user, result, majorWorks, prodcutId)) return result;
        return result;
    }
    private String getAttributeFromProcessByNodeId(String processId,String nodeId,String key){
        Process process = flowService.getEngine().process().getProcessById(processId);
        String value = "";
        try {
            String content = new String(process.getDBContent());
            Document document = DocumentHelper.parseText(content);
            Element root=document.getRootElement();
            List<Element> elements = root.elements();
            for(Element element :elements){
                if(element.getName().equals("task")&&element.attribute("name")!=null&&element.attribute("name").getValue().equals(nodeId)){
                    if(element.attribute(key)!=null) {
                        value = element.attribute(key).getValue();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    private boolean nextTask(FlowConfigureType flowConfigureType, User user, NutMap result, List<WorkItem> majorWorks, String prodcutId) {
        if (null==majorWorks||majorWorks.isEmpty()){
            result.put("ok",true);
            result.put("msg","当前用户暂无待处理节点");
            return true;
        }else{
            //业务需求，只可能会有一个页面的处理节点
            WorkItem wi = majorWorks.get(0);
            if(null==wi|| StringUtils.isEmpty(wi.getTaskKey())){
                result.put("ok",true);
                result.put("msg","当前用户暂无待处理节点");
                return true;
            }else{
                FlowControlItem flowControlItem=null;
                //动态给当前任务添加参与者
                flowService.getEngine().task().addTaskActor(wi.getTaskId(),user.getLogin());
                //获取当前节点是否参与  首先检查新通用流程（先检查productId,然后检查通用id），如果没有再去找旧流程,需要先判断新流程是否生效
                FlowControlTmpl ftl= flowControlTmplService.fetchByProductIdsAndType(prodcutId,getNewFlowControlType(wi.getTaskKey()),flowConfigureType);
                if(ftl==null){
                    ftl= flowControlTmplService.fetchByProductIdAndType(prodcutId,getFlowControlType(wi.getTaskKey()));
                }
                if(ftl!=null){
                    flowControlItem= flowControlItemService.queryByTmplIdAndname(ftl.getId(),wi.getTaskName());
                }
                Map data = new HashMap<>();
                data.put("workItem",wi);
                //这里修改为先检查节点里面的用印属性，如果找不到则可能为旧的流程配置，没有保存用印属性到xml里面，则取FlowControlItem里面找用印
                String processId = wi.getProcessId();
                String enterprise   = getAttributeFromProcessByNodeId(processId,wi.getTaskKey(),"enterprise");
                if(StringUtils.isNotEmpty(enterprise)){
                    data.put("enterprise",Boolean.valueOf(enterprise));
                }else{
                    data.put("enterprise",flowControlItem!=null?flowControlItem.isEnterprise():false);
                }
                data.put("type",wi.getTaskKey().substring(0,1));
                result.put("ok",true);
                result.put("msg","查询成功");
                result.put("data",data);
            }
        }
        return false;
    }

    private List<WorkItem> getWorkItems(String[] roleIds, Order order) {
        Page<WorkItem> majorPage = new Page<>(5);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setOrderId(order.getId());
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());

        return flowService.getEngine().query().getWorkItems(majorPage,queryFilter);
    }

    public FlowControlType getFlowControlType(String taskKey){

        if(taskKey.contains("Y")){
            return FlowControlType.BUSINESS_CONTROL;
        }else  if(taskKey.contains("F")){
            return FlowControlType.RISK_CONTROL;
        }else if(taskKey.contains("C")){
            return FlowControlType.FINANCE_CONTROL;
        }
       return  null;
    }

    public FlowControlType getNewFlowControlType(String taskKey){

        if(taskKey.contains("Y")){
            return FlowControlType.M_BUSINESS_CONTROL;
        }else  if(taskKey.contains("F")){
            return FlowControlType.M_RISK_CONTROL;
        }else if(taskKey.contains("C")){
            return FlowControlType.M_FINANCE_CONTROL;
        }else if(taskKey.contains("G")){
            return FlowControlType.M_SENIOR_EXECUTIVE;
        }
        return  null;
    }

    public Object nodeApproval(String loanId,String orderId,
                                     String taskId,String approvalCodeStr,
                                     ApprovalType approvalType,String content,
                                     boolean needRepeatFlow,boolean enterprise,
                                     FlowConfigureType flowConfigureType,boolean intermediary){
        User user = ShiroSession.getLoginUser();
        return nodeApprovalByUser(loanId, orderId, taskId, approvalCodeStr, approvalType, content, needRepeatFlow, enterprise,flowConfigureType, user,intermediary);
    }

    /**
     *展期节点审批
     */
    public NutMap nodeApprovalExtension(String extensionId,String orderId,
                                   String taskId,String approvalCodeStr,
                                   ApprovalType approvalType,String content,boolean needRepeatFlow,boolean enterprise,FlowConfigureType flowConfigureType){
        User user = ShiroSession.getLoginUser();
        NutMap result = new NutMap();
        if (checkParameterValidity(orderId, taskId, approvalCodeStr, content, result)) return result;
        //获取到当前任务
        Task task = flowService.getEngine().query().getTask(taskId);
        if(null==task){
            result.put("ok",false);
            result.put("msg","当前任务不存在或者已经审批完成");
            return result;
        }
        Map param = new HashMap<>();
        param.put("approvalType",approvalType);
        Extension extension = extensionService.fetch(extensionId);
        ApprovalCode approvalCode = ApprovalCode.valueOf(approvalCodeStr);
        //流程处理
        NutMap x = processByApprovalStatus(extensionId, orderId, taskId, needRepeatFlow, flowConfigureType, user, result, task, param, extension, approvalCode);
        if (x != null) return x;
        if(ApprovalCode.DISAGREE.equals(approvalCode)){
            //删除对应关系 删除对应的展期暂存数据
            baseOrderService.deleteBaseOrderById(ExtensionOrder.class,"extensionId",extensionId,flowConfigureType);
        }
        //修改单状态
        extension.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
        extension.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
        //如果是已审批状态，则修改展期的参数为可用
        if (ApprovalStatusType.APPROVED.equals(extension.getApprovalStatusType())&&ApprovalCode.AGREE.equals(approvalCode)) {
            businessExtensionService.enableExtension(extension);
        }
        extensionService.updateExtension(extension,null);
        //保存审批信息
        ApprovalResult approvalResult = getApprovalResult(extensionId, orderId, approvalType, content, flowConfigureType, user, task, approvalCode);
        ApprovalResult approvalResultReturn = approvalResultService.add(approvalResult);
        result.put("ok",true);
        result.put("msg","审批完成");
        result.put("data",approvalResultReturn);
        return result;
    }

    /**
     *费用减免节点审批
     */
    public NutMap nodeApprovalCostExemption(String costExemptionId,String orderId,
                                        String taskId,String approvalCodeStr,
                                        ApprovalType approvalType,String content,boolean needRepeatFlow,boolean enterprise,FlowConfigureType flowConfigureType){
        User user = ShiroSession.getLoginUser();
        NutMap result = new NutMap();
        if (checkParameterValidity(orderId, taskId, approvalCodeStr, content, result)) return result;
        //获取到当前任务
        Task task = flowService.getEngine().query().getTask(taskId);
        if(null==task){
            result.put("ok",false);
            result.put("msg","当前任务不存在或者已经审批完成");
            return result;
        }
        Map param = new HashMap<>();
        param.put("approvalType",approvalType);
        CostExemption costExemption = costExemptionService.fetch(costExemptionId);
        ApprovalCode approvalCode = ApprovalCode.valueOf(approvalCodeStr);
        //如果是最后一步并且为同意，则更新费用，如果更新时候出异常，抛出异常，修改状态为退回，并且保存退回的原因
        if (ApprovalCode.AGREE.equals(approvalCode)) {
            String msg = costExemptionService.validate(costExemption);
            if(StringUtils.isNotEmpty(msg)){
                result.put("ok",false);
                result.put("msg","当前"+msg.substring(0,msg.length()-1)+"剩余应还金额有变更，小于减免金额，审批结果不能为同意！");
                return result;
            }
        }
        if(ApprovalCode.BACKBEGIN.equals(approvalCode)||ApprovalCode.DISAGREE.equals(approvalCode)){
            String msg = costExemptionService.validate(costExemption);
            if(StringUtils.isNotEmpty(msg)){
                content += "  \n原因："+msg.substring(0,msg.length()-1)+"剩余应还金额有变更，小于减免金额，不同意！";
            }
        }
        //流程处理
        NutMap x = processByApprovalStatus(costExemptionId, orderId, taskId,needRepeatFlow, flowConfigureType, user, result, task, param, costExemption, approvalCode);
        if (ApprovalStatusType.APPROVED.equals(costExemption.getApprovalStatusType())&&ApprovalCode.AGREE.equals(approvalCode)) {
            //成功了要做些什么东西，TODO
            try {
                costExemptionService.updateFee(costExemption);
            }catch (Exception e){
                e.printStackTrace();
                result.put("ok",false);
                result.put("msg","修改费用时候出错");
                return result;
            }
        }
        if(ApprovalCode.DISAGREE.equals(approvalCode)){
            //删除对应关系
            baseOrderService.deleteBaseOrderById(CostExemptionOrder.class,"costExemptionId",costExemptionId,flowConfigureType);
            //costExemptionService.deleteCostExemptionItemByCostExemptionId(costExemptionId);
        }
        //修改单状态
        costExemption.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
        costExemption.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
        costExemptionService.updateCostExemption(costExemption,null);
        //保存审批信息
        ApprovalResult approvalResult = getApprovalResult(costExemptionId, orderId, approvalType, content, flowConfigureType, user, task, approvalCode);
        ApprovalResult approvalResultReturn = approvalResultService.add(approvalResult);
        result.put("ok",true);
        result.put("msg","审批完成");
        result.put("data",approvalResultReturn);
        return result;
    }


    //抵押节点审批
    public NutMap nodeApprovalMortgage(String mortgageId,String orderId,
                                       String taskId,String approvalCodeStr,
                                       ApprovalType approvalType,String content,boolean needRepeatFlow,boolean enterprise,FlowConfigureType flowConfigureType){

        User user = ShiroSession.getLoginUser();
        NutMap result = new NutMap();
        if (checkParameterValidity(orderId, taskId, approvalCodeStr, content, result)) return result;
        //获取到当前任务
        Task task = flowService.getEngine().query().getTask(taskId);
        if(null==task){
            result.put("ok",false);
            result.put("msg","当前任务不存在或者已经审批完成");
            return result;
        }
        Map param = new HashMap<>();
        param.put("approvalType",approvalType);

        Mortgage mortgage = mortgageService.getSimpleMortgageById(mortgageId);
        ApprovalCode approvalCode = ApprovalCode.valueOf(approvalCodeStr);
        //流程处理
        switch (approvalCode) {
            case AGREE:
                List<Task> tasks = flowService.getEngine().executeTask(taskId, user.getLogin(), param);
                //判断当前流程节点是否完全走完，如果完全走完改变loan中的loanstatus为审批结束
                if (null == tasks || tasks.size() == 0) {
                    mortgage.setApprovalStatusType(ApprovalStatusType.APPROVED);
                }
                break;
            case BACKPRE:
                //获取到上一步的节点名称
                ApprovalResult approvalResultFirst = approvalResultService.fetchFirst(mortgageId, flowConfigureType);
                if (null != approvalResultFirst) {
                    //获取到当前节点
                    String nodeName = approvalResultFirst.getNodeCode();
                    String taskName = task.getTaskName();
                    if (null == nodeName || null == taskName || nodeName.equals(taskName)) {
                        result.put("ok", true);
                        result.put("msg", "返回到业务修改");
                        mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                    } else {
                        Order order = flowService.getEngine().query().getOrder(task.getOrderId());
                        Process process = flowService.getEngine().process().getProcessById(order.getProcessId());
                        List<NodeModel> nodetmpss = process.getModel().getNodes();
                        List<NodeModel> nodes = new ArrayList<>();
                        //去掉所有的decision节点
                        for (NodeModel node : nodetmpss) {
                            if (!node.getDisplayName().equals("分支选择")) {
                                //判断当前节点金额是否需要审批
                                nodes.add(node);
                            }
                        }

                        String preNodeName = "";
                        if (null != nodes && nodes.size() > 0) {
                            for (int i = 0; i < nodes.size(); i++) {
                                NodeModel node = nodes.get(i);
                                if (null != node && node.getName().equals(task.getTaskName()) && i > 0) {
                                    preNodeName = nodes.get(i - 1).getName();
                                    break;
                                }
                            }
                        }
                        if (preNodeName.equals("")) {
                            result.put("ok", true);
                            result.put("msg", "返回到业务修改");
                            mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                        } else {
                            flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, preNodeName);
                        }
                    }
                } else {
                    if (null == task) {
                        result.put("ok", false);
                        result.put("msg", "当前节点无法回退到上一步");
                        return result;
                    } else {
                        result.put("ok", true);
                        result.put("msg", "返回到业务修改");
                        mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                    }
                }
                break;
            case BACKBEGIN:
                ApprovalResult approvalResultFirst1 = approvalResultService.fetchFirst(mortgageId, flowConfigureType);
                mortgage.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                if (null != approvalResultFirst1) {
                    if (needRepeatFlow) {
                        flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, approvalResultFirst1.getNodeCode());
                    }
                } else {
                    if (null == task) {
                        result.put("ok", false);
                        result.put("msg", "当前节点无法回退到开始节点");
                        return result;
                    }
                }
                break;
            case DISAGREE:
                mortgage.setApprovalStatusType(ApprovalStatusType.REJECT);
                flowService.getEngine().order().terminate(orderId, user.getLogin());
                break;
        }
        if(ApprovalCode.DISAGREE.equals(approvalCode)){
            //删除对应关系
            mortgageService.deleteByMortgageId(mortgageId, flowConfigureType);
        }
        //修改单状态
        mortgage.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
        mortgage.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
        mortgageService.updateMortgage(mortgage,null);
        //保存审批信息
        ApprovalResult approvalResult = getApprovalResult(mortgageId, orderId, approvalType, content, flowConfigureType, user, task, approvalCode);
        ApprovalResult approvalResultReturn = approvalResultService.add(approvalResult);
        result.put("ok",true);
        result.put("msg","审批完成");
        result.put("data",approvalResultReturn);
        return result;
    }


    /**
     * 流程节点审批
     * @param orderId
     * @param taskId
     * @param approvalCodeStr
     * @return
     */
    public Object nodeApprovalByUser(String loanId,String orderId,
                               String taskId,String approvalCodeStr,
                               ApprovalType approvalType,String content,
                               boolean needRepeatFlow,boolean enterprise,
                               FlowConfigureType flowConfigureType, User user,
                               boolean intermediary){
        NutMap result = new NutMap();
        if (checkParameterValidity(orderId, taskId, approvalCodeStr, content, result)) return result;
        //获取到当前任务
        Task task = flowService.getEngine().query().getTask(taskId);
        if(null==task){
            result.put("ok",false);
            result.put("msg","当前任务不存在或者已经审批完成");
            return result;
        }
        //移除延时发送短信
        ApproveWarnMessageUtils.removeTask(taskId);

        Map param = new HashMap<>();
        param.put("approvalType",approvalType);

        Loan loan = loanService.fetchById(loanId);
        IntermediaryApply intermediaryApply = null;
        Boolean flag = false;
        if(flowConfigureType.equals(FlowConfigureType.BROKERAGE_FEE)){
            flag = true;
            intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
        }
        HouseNoMortgageApply houseNoMortgageApply = null;
        Boolean houseFlag = false;
        if(flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            houseFlag = true;
            houseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(loanId);
            loanId = houseNoMortgageApply.getId();
        }
        //修改状态
        Loan l = new Loan();

        ApprovalCode approvalCode = ApprovalCode.valueOf(approvalCodeStr);
        //流程处理
        switch (approvalCode){
            case AGREE:
                List<Task> tasks = flowService.getEngine().executeTask(taskId,user.getLogin(),param);
                //判断当前流程节点是否完全走完，如果完全走完改变loan中的loanstatus为审批结束
                if(null==tasks||tasks.size()==0){
                    if (flag){
                        intermediaryApply.setLoanStatus(LoanStatus.APPROVEEND);
                    }else if(houseFlag){
                        houseNoMortgageApply.setLoanStatus(ApprovalStatusType.APPROVED);
                    }else {
                        l.setLoanStatus(LoanStatus.APPROVEEND);
                        //todo 生成业务推单
                        loanPushService.saveLoanPush(loan);
                    }

                }
                break;
            case BACKPRE:
                //获取到上一步的节点名称
                //ApprovalResult approvalResultLast = approvalResultService.fetchLast(loanId);
                ApprovalResult approvalResultFirst1 = approvalResultService.fetchFirst(loanId,flowConfigureType);
                if (null!=approvalResultFirst1){
                    //获取到当前节点
                    String nodeName = approvalResultFirst1.getNodeCode();
                    String taskName = task.getTaskName();
                    if(null==nodeName||null==taskName||nodeName.equals(taskName)){
                        result.put("ok",true);
                        result.put("msg","返回到业务修改");
                        if (flag){
                            intermediaryApply.setLoanStatus(LoanStatus.SAVE);
                        }else if(houseFlag){
                            houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_EDIT);
                        }else {
                            l.setLoanStatus(LoanStatus.SAVE);
                        }

                    }else{
                        Order order =  flowService.getEngine().query().getOrder(task.getOrderId());
                        Process process = flowService.getEngine().process().getProcessById(order.getProcessId());
                        List<NodeModel> nodetmpss = process.getModel().getNodes();
                        List<NodeModel> nodes = new ArrayList<>();
                        //去掉所有的decision节点
                        for (NodeModel node:nodetmpss){
                            if(!node.getDisplayName().equals("分支选择")){

                                //判断当前节点金额是否需要审批
                                if(FlowConfigureType.BROKERAGE_FEE.equals(flowConfigureType)){
                                    if (StringUtils.isEmpty(node.getLayout()) || (StringUtils.isNotEmpty(node.getLayout()) && Double.parseDouble(node.getLayout()) < loan.getAmount().doubleValue())) {
                                        nodes.add(node);
                                    }
                                }else {
                                    if (StringUtils.isEmpty(node.getLayout()) || (StringUtils.isNotEmpty(node.getLayout()) && Double.parseDouble(node.getLayout()) < loan.getAmount().doubleValue())) {
                                        nodes.add(node);
                                    }
                                }

                            }
                        }

                        String preNodeName = "";
                        if (null != nodes && nodes.size()>0) {
                            for (int i = 0; i < nodes.size(); i++) {
                                NodeModel node = nodes.get(i);
                                if (null!=node && node.getName().equals(task.getTaskName()) && i>0) {
                                    preNodeName = nodes.get(i-1).getName();
                                    break;
                                }
                            }
                        }
                        if (preNodeName.equals("")) {
                            result.put("ok",true);
                            result.put("msg","返回到业务修改");
                            if (flag){
                                intermediaryApply.setLoanStatus(LoanStatus.SAVE);
                            }else if(houseFlag){
                                houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_EDIT);
                            }else {
                                l.setLoanStatus(LoanStatus.SAVE);
                            }
                        } else {
                            flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, preNodeName);
                        }
                    }
                }else{
                    if(null==task){
                        result.put("ok",false);
                        result.put("msg","当前节点无法回退到上一步");
                        return result;
                    }else{
                        result.put("ok",true);
                        result.put("msg","返回到业务修改");
                        if (flag){
                            intermediaryApply.setLoanStatus(LoanStatus.SAVE);
                        }else if(houseFlag){
                            houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_EDIT);
                        }else {
                            l.setLoanStatus(LoanStatus.SAVE);
                        }
                    }
                }

                break;
            case BACKBEGIN:
                ApprovalResult approvalResultFirst = approvalResultService.fetchFirst(loanId,flowConfigureType);
                if (flag){
                    intermediaryApply.setLoanStatus(LoanStatus.SAVE);
                }else if(houseFlag){
                    houseNoMortgageApply.setLoanStatus(ApprovalStatusType.IN_EDIT);
                }else {
                    l.setLoanStatus(LoanStatus.SAVE);
                }
                if (null!=approvalResultFirst){
                    if (needRepeatFlow) {
                        flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, approvalResultFirst.getNodeCode());
                    }
                }else{
                    if(null==task){
                        result.put("ok",false);
                        result.put("msg","当前节点无法回退到开始节点");
                        return result;
                    }
                }
                break;
            case DISAGREE:

                if (flag){
                    //删除对应关系
                    loanOrderService.deleteByLoanId(loanId,flowConfigureType);
                    intermediaryApply.setLoanStatus(LoanStatus.APPROVEREJECT);
                }else if(houseFlag){
                    //删除对应关系
                    loanOrderService.deleteByLoanId(houseNoMortgageApply.getId(),flowConfigureType);
                    houseNoMortgageApply.setLoanStatus(ApprovalStatusType.REJECT);
                }else {
                    //删除对应关系
                    loanOrderService.deleteByLoanId(loanId,flowConfigureType);
                    l.setLoanStatus(LoanStatus.APPROVEREJECT);
                }
                flowService.getEngine().order().terminate(orderId,user.getLogin());
                break;
        }

        if (flag){
            intermediaryApply.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
            intermediaryApply.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
            intermediaryApplyService.update(intermediaryApply);
        }if(houseFlag){
            houseNoMortgageApply.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
            houseNoMortgageApply.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
            houseNoMortgageApplyService.updateHouseNoMortgageApply(houseNoMortgageApply);
        }else {
            l.setId(loanId);
            l.setApproveStatus(task.getTaskName() + "--" + approvalCode.getCode());
            l.setApproveStatusDesc(task.getDisplayName() + "--" + approvalCode.getDescription());
            loanService.update(l);
            if(null != l.getLoanStatus()){
                if(LoanStatus.SAVE.equals(l.getLoanStatus())
                        || (l.getLoanStatus().equals(LoanStatus.APPROVEREJECT))){
                    Product product = productService.fetchEnableProductById(loan.getProductId());
                    if(null != product && ("商业承兑汇票").equals(product.getName())) {
                        billLoanService.changeAmount(loanId);
                    }
                }
            }
        }

        //保存审批信息
        ApprovalResult approvalResult = getApprovalResult(loanId, orderId, approvalType, content, flowConfigureType, user, task, approvalCode);

        approvalResult.setEnterprise(enterprise);
        approvalResult.setIntermediary(intermediary);
        ApprovalResult approvalResultReturn = approvalResultService.add(approvalResult);
        result.put("ok",true);
        result.put("msg","审批完成");
        result.put("data",approvalResultReturn);
        if (isNeedSendWarnMessage(l.getLoanStatus()) && flowConfigureType.equals(FlowConfigureType.BORROW_APPLY)) {
            // 延时短信
            flowService.sendWarnMessage(orderId, loan);
        }
        return result;
    }

    private ApprovalResult getApprovalResult(String loanId, String orderId, ApprovalType approvalType, String content, FlowConfigureType flowConfigureType, User user, Task task, ApprovalCode approvalCode) {
        ApprovalResult approvalResult = new ApprovalResult();
        approvalResult.setLoanId(loanId);
        approvalResult.setFlowConfigureType(flowConfigureType);
        approvalResult.setApprovalCode(approvalCode);
        approvalResult.setApprovalTime(new Date());
        approvalResult.setApprovalType(approvalType);
        approvalResult.setContent(content);
        approvalResult.setNodeCode(task.getTaskName());
        approvalResult.setOrderId(orderId);
        approvalResult.setUserId(user.getId());
        approvalResult.setUserName(user.getName());
        approvalResult.setCreateBy(user.getName());
        approvalResult.setCreateTime(new Date());
        approvalResult.setUpdateBy(user.getName());
        approvalResult.setUpdateTime(new Date());
        approvalResult.setNodeName(task.getDisplayName());
        ApprovalResult  last = null;
        if("start".equals(task.getParentTaskId())&&(last = approvalResultService.fetchLast(loanId,flowConfigureType))!=null){
            approvalResult.setStartTime(last.getApprovalTime());
        }else{
            approvalResult.setStartTime(DateUtil.getStringToTime(task.getCreateTime()));
        }
        approvalResult.setDuration(DateUtil.minutesBetweenTowDate(approvalResult.getStartTime(),approvalResult.getCreateTime()));
        return approvalResult;
    }

    private boolean checkParameterValidity(String orderId, String taskId, String approvalCodeStr, String content, NutMap result) {
        if(StringUtils.isEmpty(orderId)){
            result.put("ok",false);
            result.put("msg","节点订单Id不能为空");
            return true;
        }

        if(StringUtils.isEmpty(taskId)){
            result.put("ok",false);
            result.put("msg","节点任务Id不能为空");
            return true;
        }

        if(StringUtils.isEmpty(approvalCodeStr) && !ApprovalCode.isContainCode(approvalCodeStr)){
            result.put("ok",false);
            result.put("msg","审批结果类型不能为空");
            return true;
        }

        if(ApprovalCode.AGREE==ApprovalCode.valueOf(approvalCodeStr) && StringUtils.isEmpty(content)){
            result.put("ok",false);
            result.put("msg","审批结果内容不能为空");
            return true;
        }
        return false;
    }

    private boolean isNeedSendWarnMessage(LoanStatus status) {
        if (LoanStatus.APPROVEREJECT.equals(status)) {
            return false;
        }
        if (LoanStatus.SAVE.equals(status)) {
            return false;
        }
        if (LoanStatus.APPROVEEND.equals(status)) {
            return false;
        }
        return true;
    }
    /**
     *查询展期单列表
     */
    public Object queryApprovalListExtension(String extensionId ,ApprovalType approvalType ,FlowConfigureType flowConfigureType){
        Extension extension = extensionService.fetch(extensionId);
        ExtensionOrder extensionOrder = baseOrderService.getBaseOrderById(ExtensionOrder.class,"extensionId",extensionId,flowConfigureType);
        return queryApprovalListBase(extension,extensionOrder,approvalType,flowConfigureType);
    }

    /**
     *查询费用免除单列表
     */
    public Object queryApprovalListCostExemption(String extensionId ,ApprovalType approvalType ,FlowConfigureType flowConfigureType){
        CostExemption costExemption = costExemptionService.fetch(extensionId);
        CostExemptionOrder costExemptionOrder = baseOrderService.getBaseOrderById(CostExemptionOrder.class,"costExemptionId",extensionId,flowConfigureType);
        return queryApprovalListBase(costExemption,costExemptionOrder,approvalType,flowConfigureType);
    }


    /**
     *基本审批列表查询
     */
    public Object queryApprovalListBase(BaseApproval baseApproval,BaseOrder baseOrder,ApprovalType approvalType,FlowConfigureType flowConfigureType){
        NutMap result = new NutMap();
        if (null==baseApproval){
            result.put("ok",false);
            result.put("msg","录单不存在");
            return result;
        }
        //查看已审批节点
        List<ApprovalResult> approvalResults = approvalResultService.query(baseApproval.getId(),approvalType,flowConfigureType);
        String processId =  flowService.getProcessIdByMortgageId(baseApproval.getId());
        //查看待审批节点
        List<Map> data = new ArrayList<>();
        boolean flag = false;
        String flagStr = "";
        if(null!=baseOrder){
            Order order = flowService.getEngine().query().getOrder(baseOrder.getOrderId());
            if(null!=order) {
                Page<WorkItem> majorPage = new Page<WorkItem>(5);
                QueryFilter queryFilter = new QueryFilter();
                queryFilter.setOrderId(order.getId());
                queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());
                List<WorkItem> majorWorks = flowService.getEngine().query().getWorkItems(majorPage, queryFilter);
                if (null != majorWorks && majorWorks.size() > 0) {
                    WorkItem workItem = majorWorks.get(0);
                    flagStr = workItem.getTaskKey();
                }
            }
        }

        //去掉已完成订单的数据

        removeCompletedOrder(approvalResults, data);

        // 如果当前单正在编辑，添加编辑的节点
        if(null != baseApproval && baseApproval.getApprovalStatusType()==ApprovalStatusType.IN_EDIT){
            //添加当前正在编辑的节点
            Map tmp = initEditNode();
            data.add(tmp);
        }

        if(null != baseApproval && baseApproval.getApprovalStatusType()==ApprovalStatusType.CANCEL){
            //添加当前正在取消的节点
            Map tmp = initCancelNode();
            tmp.put("isCancel",true);
            data.add(tmp);
        }


        //如果没有流程ID，则不需要做查询
        if(StringUtils.isNotEmpty(processId)){
            Process process  = flowService.getEngine().process().getProcessById(null==processId?"":processId);
            ProcessModel processModel = process.getModel();
            //所有的流程节点
            List<NodeModel> nodeModelList = processModel.getNodes();
            if (null != baseApproval && ApprovalStatusType.CANCEL != baseApproval.getApprovalStatusType()){
                for (NodeModel nodeModel:nodeModelList){
                    if (flag==false&&nodeModel.getName().equals(flagStr)){
                        flag = true;
                    }
                    if(flag){
                        if("start".equals(nodeModel.getName())||"end".equals(nodeModel.getName())||"分支选择".equals(nodeModel.getDisplayName())){
                            continue;
                        }

                        //去掉不需要显示的节点,这里主要做金额的过滤，如果是展期审批的话，不需要这个判断，所以删掉这些逻辑
                        if (FlowConfigureType.EXTENSION.equals(flowConfigureType)) {
                            if (StringUtils.isNotEmpty(nodeModel.getLayout()) && Double.parseDouble(nodeModel.getLayout()) >= DecimalFormatUtils.getNotNull(baseApproval.getAmount()).doubleValue()) {
                                continue;
                            }
                        }

                        Map tmp = new HashMap<>();
                        tmp.put("nodeName",nodeModel.getDisplayName());
                        tmp.put("nodeCode",nodeModel.getName());

                        tmp.put("isApproval",false);
                        data.add(tmp);
                    }
                }
            }
        }
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    //抵押审批
    public Object queryApprovalListMortgage(String mortgageId ,ApprovalType approvalType ,FlowConfigureType flowConfigureType){
        NutMap result = new NutMap();
        Mortgage mortgage = mortgageService.getSimpleMortgageById(mortgageId);
        if (null==mortgage){
            result.put("ok",false);
            result.put("msg","录单不存在");
            return result;
        }
        //查看已审批节点
        List<ApprovalResult> approvalResults = approvalResultService.query(mortgageId,approvalType,flowConfigureType);
        MortgageOrder mortgageOrder = mortgageService.getMortgageOrderByMortgageId(mortgageId,flowConfigureType);
        String processId =  flowService.getProcessIdByMortgageId(mortgage.getId());
        //查看待审批节点
        List<Map> data = new ArrayList<>();
        boolean flag = false;
        String flagStr = "";
        if(null!=mortgageOrder){
            Order order = flowService.getEngine().query().getOrder(mortgageOrder.getOrderId());
            if(null!=order) {
                Page<WorkItem> majorPage = new Page<WorkItem>(5);
                QueryFilter queryFilter = new QueryFilter();
                queryFilter.setOrderId(order.getId());
                queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());
                List<WorkItem> majorWorks = flowService.getEngine().query().getWorkItems(majorPage, queryFilter);
                if (null != majorWorks && majorWorks.size() > 0) {
                    WorkItem workItem = majorWorks.get(0);
                    flagStr = workItem.getTaskKey();
                }
            }
        }

        //去掉已完成订单的数据

        removeCompletedOrder(approvalResults, data);

        // 如果当前单正在编辑，添加编辑的节点
        if(null != mortgage && mortgage.getApprovalStatusType()==ApprovalStatusType.IN_EDIT){
            //添加当前正在编辑的节点
            Map tmp = initEditNode();
            data.add(tmp);
        }

        if(null != mortgage && mortgage.getApprovalStatusType()==ApprovalStatusType.CANCEL){
            //添加当前正在取消的节点
            Map tmp = initCancelNode();
            tmp.put("isCancel",true);
            data.add(tmp);
        }


        //如果没有流程ID，则不需要做查询
        if(StringUtils.isNotEmpty(processId)){
            Process process  = flowService.getEngine().process().getProcessById(null==processId?"":processId);
            ProcessModel processModel = process.getModel();
            //所有的流程节点
            List<NodeModel> nodeModelList = processModel.getNodes();
            if (null != mortgage && ApprovalStatusType.CANCEL != mortgage.getApprovalStatusType()){
                for (NodeModel nodeModel:nodeModelList){
                    if (flag==false&&nodeModel.getName().equals(flagStr)){
                        flag = true;
                    }
                    if(flag){
                        if("start".equals(nodeModel.getName())||"end".equals(nodeModel.getName())||"分支选择".equals(nodeModel.getDisplayName())){
                            continue;
                        }

                        //去掉不需要显示的节点,这里主要做金额的过滤，如果是抵押审批的话，不需要这个判断，所以删掉这些逻辑

                        Map tmp = new HashMap<>();
                        tmp.put("nodeName",nodeModel.getDisplayName());
                        tmp.put("nodeCode",nodeModel.getName());

                        tmp.put("isApproval",false);
                        data.add(tmp);
                    }
                }
            }
        }
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }
    public Object queryApprovalList(String loanId, ApprovalType approvalType,FlowConfigureType flowConfigureType) {
        NutMap result = new NutMap();
//        FlowConfigureType flowConfigureType = FlowConfigureType.BORROW_APPLY;
        //获取到Loan对象
        HouseNoMortgageApply houseNoMortgageApply = null;
        Loan loan = null;
        if(flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            houseNoMortgageApply = houseNoMortgageApplyService.queryHouseNoMortgageApplyById(loanId);
            loanId = houseNoMortgageApply.getId();
        }else {
            loan = loanService.fetchById(loanId);
            if (null==loan){
                result.put("ok",false);
                result.put("msg","录单不存在");
                return result;
            }
        }

        IntermediaryApply intermediaryApply = null;
        if(flowConfigureType.equals(FlowConfigureType.BROKERAGE_FEE)){
            intermediaryApply = intermediaryApplyService.fetchByLoanId(loanId);
        }

        //查看已审批节点
        List<ApprovalResult> approvalResults = approvalResultService.query(loanId,approvalType,flowConfigureType);
        List<LoanedResult> loanedResults = loanedResultService.loanedResultList(loanId);

        //通过loanId查找到orderId
        Product product;
        if(flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            product = productService.fetchEnableProductById(houseNoMortgageApply.getProductId());
        }else {
            product = productService.fetchEnableProductById(loan.getProductId());
        }


        //根据Id查找到最新版本


        //根据单号查询orderId
        //根据orderId查询processId
        List<Map> data = new ArrayList<>();
        boolean flag = false;
        String flagStr = "";
        List<NodeModel> nodeModelList = null;
        String processId = null;
        LoanOrder loanOrder = loanOrderService.fetchByLoanId(loanId,flowConfigureType);
        //String processId = flowService.getProcessIdByLoanOrder(loanOrder);
        if(null != loan){
            processId =  flowService.getProcessId(loan.getId());
        }else if(flowConfigureType.equals(FlowConfigureType.DECOMPRESSION)){
            processId =  flowService.getProcessId(houseNoMortgageApply.getId());
        }

        if(StringUtils.isEmpty(processId)) {
            processId = product.getProcessId();
        }


        //查看待审批节点
        Process process  = flowService.getEngine().process().getProcessById(null==processId?"":processId);


        //查找到当前的order
        if(null!=loanOrder){
            Order order = flowService.getEngine().query().getOrder(loanOrder.getOrderId());
            if(null!=order) {
                Page<WorkItem> majorPage = new Page<WorkItem>(5);
                QueryFilter queryFilter = new QueryFilter();
                queryFilter.setOrderId(order.getId());
                queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());
                List<WorkItem> majorWorks = flowService.getEngine().query().getWorkItems(majorPage, queryFilter);
                if (null != majorWorks && majorWorks.size() > 0) {
                    WorkItem workItem = majorWorks.get(0);
                    flagStr = workItem.getTaskKey();
                }
            }
        }


        ProcessModel processModel = process.getModel();

        //所有的流程节点
        nodeModelList = processModel.getNodes();


        //去掉已完成订单的数据

        removeCompletedOrder(approvalResults, data);

        // 如果当前单正在编辑，添加编辑的节点----渠道
        if((null!=loan && loan.getLoanStatus()==LoanStatus.CHANNELSAVE)){
            //添加当前正在编辑的节点
            Map tmp = initChanneEditNode();
            data.add(tmp);
        }

        // 如果当前单正在编辑，添加编辑的节点
        if((null!=loan && loan.getLoanStatus()==LoanStatus.SAVE) || (null != intermediaryApply && intermediaryApply.getLoanStatus()==LoanStatus.SAVE) || (null != houseNoMortgageApply && houseNoMortgageApply.getLoanStatus()==ApprovalStatusType.IN_EDIT)){
            //添加当前正在编辑的节点
            Map tmp = initEditNode();
            data.add(tmp);
        }

        // 如果当前单正在编辑，添加编辑的节点
        if((null!=loan && loan.getLoanStatus()==LoanStatus.CANCEL)|| (null != houseNoMortgageApply && houseNoMortgageApply.getLoanStatus()==ApprovalStatusType.CANCEL)){
            //添加当前正在编辑的节点
            Map tmp = initEditNode();
            tmp.put("isCancel",true);
            data.add(tmp);
        }

        if((null!=loan && loan.getLoanStatus()!=LoanStatus.CANCEL) || (null != intermediaryApply && LoanStatus.CANCEL != intermediaryApply.getLoanStatus()) || (null != houseNoMortgageApply && ApprovalStatusType.CANCEL != houseNoMortgageApply.getLoanStatus())){
        if(CollectionUtils.isNotEmpty(nodeModelList)) {
            for (NodeModel nodeModel : nodeModelList) {
                if (flag == false && nodeModel.getName().equals(flagStr)) {
                    flag = true;
                }
                if (flag) {
                    if ("start".equals(nodeModel.getName()) || "end".equals(nodeModel.getName()) || "分支选择".equals(nodeModel.getDisplayName())) {
                        continue;
                    }

                    //去掉不需要显示的节点
                    if (FlowConfigureType.BROKERAGE_FEE.equals(flowConfigureType)) {
                        if (StringUtils.isNotEmpty(nodeModel.getLayout()) && Double.parseDouble(nodeModel.getLayout()) >= loan.getAmount().doubleValue()) {
                            continue;
                        }
                    } else if (FlowConfigureType.BORROW_APPLY.equals(flowConfigureType)) {
                        if (StringUtils.isNotEmpty(nodeModel.getLayout()) && Double.parseDouble(nodeModel.getLayout()) >= loan.getAmount().doubleValue()) {
                            continue;
                        }
                    }


                    Map tmp = new HashMap<>();
                    tmp.put("nodeName", nodeModel.getDisplayName());
                    tmp.put("nodeCode", nodeModel.getName());

                    tmp.put("isApproval", false);
                    data.add(tmp);
                }
            }
          }
        }

        if(FlowConfigureType.BROKERAGE_FEE.equals(flowConfigureType)){
            List<IntermediaryApplyLoanedResult> intermediaryApplyLoanedResults = loanedResultService.intermediaryApplyLoanedResultList(loanId);
            if(CollectionUtils.isNotEmpty(intermediaryApplyLoanedResults)){
                for(IntermediaryApplyLoanedResult intermediaryApplyLoanedResult : intermediaryApplyLoanedResults) {
                    Map tmp = new HashMap<>();
                    tmp.put("nodeName", intermediaryApplyLoanedResult.getNodeName());
                    tmp.put("nodeCode", intermediaryApplyLoanedResult.getNodeCode());
                    tmp.put("approvalTime", intermediaryApplyLoanedResult.getApprovalTime());
                    tmp.put("approvalType", intermediaryApplyLoanedResult.getApprovalType());
                    tmp.put("userName", intermediaryApplyLoanedResult.getUserName());
                    tmp.put("isApproval", true);
                    tmp.put("startTime", intermediaryApplyLoanedResult.getStartTime());
                    tmp.put("approvalCode", "AGREE");
                    tmp.put("duration", intermediaryApplyLoanedResult.getDuration() + "天");
                    data.add(tmp);
                }
            }

        }else if(FlowConfigureType.DECOMPRESSION.equals(flowConfigureType)){

        }else {
            for(LoanedResult loanedResult : loanedResults){
                Map tmp = new HashMap<>();
                tmp.put("nodeName",loanedResult.getNodeName());
                tmp.put("nodeCode",loanedResult.getNodeCode());
                tmp.put("approvalTime",loanedResult.getApprovalTime());
                tmp.put("approvalType",loanedResult.getApprovalType());
                tmp.put("userName",loanedResult.getUserName());
                tmp.put("isApproval",true);
                tmp.put("startTime",loanedResult.getStartTime());
                tmp.put("approvalCode","AGREE");
                tmp.put("duration",loanedResult.getDuration()+"天");
                data.add(tmp);
            }
        }




        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }

    private Map initCancelNode() {
        Map tmp = new HashMap<>();
        tmp.put("nodeName","订单取消");
        tmp.put("nodeCode","Y0");
        tmp.put("approvalTime","");
        tmp.put("approvalType","");
        tmp.put("approvalCode","");
        tmp.put("userName","");
        tmp.put("content","");
        tmp.put("isApproval",false);
        return tmp;
    }

    private Map initEditNode() {
        Map tmp = new HashMap<>();
        tmp.put("nodeName","资料维护");
        tmp.put("nodeCode","Y0");
        tmp.put("approvalTime","");
        tmp.put("approvalType","");
        tmp.put("approvalCode","");
        tmp.put("userName","");
        tmp.put("content","");
        tmp.put("isApproval",false);
        return tmp;
    }

    private Map initChanneEditNode() {
        Map tmp = new HashMap<>();
        tmp.put("nodeName","渠道资料维护");
        tmp.put("nodeCode","Y0");
        tmp.put("approvalTime","");
        tmp.put("approvalType","");
        tmp.put("approvalCode","");
        tmp.put("userName","");
        tmp.put("content","");
        tmp.put("isApproval",false);
        return tmp;
    }

    private void removeCompletedOrder(List<ApprovalResult> approvalResults, List<Map> data) {
        for (int i=0; i<approvalResults.size();i++){
            ApprovalResult approvalResult = approvalResults.get(i);
            Map tmp = new HashMap<>();
            tmp.put("nodeName",approvalResult.getNodeName());
            tmp.put("nodeCode",approvalResult.getNodeCode());
            tmp.put("approvalTime",approvalResult.getApprovalTime());
            tmp.put("approvalType",approvalResult.getApprovalType());
            tmp.put("approvalCode",approvalResult.getApprovalCode());
            tmp.put("userName",approvalResult.getUserName());
            tmp.put("content",approvalResult.getContent());
            tmp.put("isApproval",true);
            tmp.put("startTime",approvalResult.getStartTime());
            tmp.put("duration",(approvalResult.getDuration()>60?(approvalResult.getDuration()/60+"时"):"")+approvalResult.getDuration()%60+"分");
            data.add(tmp);
        }
    }
    private NutMap processByApprovalStatus(String extensionId, String orderId, String taskId, boolean needRepeatFlow, FlowConfigureType flowConfigureType, User user, NutMap result, Task task, Map param, BaseApproval baseApproval, ApprovalCode approvalCode) {
        switch (approvalCode) {
            case AGREE:
                List<Task> tasks = flowService.getEngine().executeTask(taskId, user.getLogin(), param);
                //判断当前流程节点是否完全走完，如果完全走完改变loan中的loanstatus为审批结束
                if (null == tasks || tasks.size() == 0) {
                    baseApproval.setApprovalStatusType(ApprovalStatusType.APPROVED);
                }
                break;
            case BACKPRE:
                //获取到上一步的节点名称
                ApprovalResult approvalResultFirst = approvalResultService.fetchFirst(extensionId, flowConfigureType);
                if (null != approvalResultFirst) {
                    //获取到当前节点
                    String nodeName = approvalResultFirst.getNodeCode();
                    String taskName = task.getTaskName();
                    if (null == nodeName || null == taskName || nodeName.equals(taskName)) {
                        result.put("ok", true);
                        result.put("msg", "返回到业务修改");
                        baseApproval.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                    } else {
                        Order order = flowService.getEngine().query().getOrder(task.getOrderId());
                        Process process = flowService.getEngine().process().getProcessById(order.getProcessId());
                        List<NodeModel> nodetmpss = process.getModel().getNodes();
                        List<NodeModel> nodes = new ArrayList<>();
                        //去掉所有的decision节点
                        for (NodeModel node : nodetmpss) {
                            if (!node.getDisplayName().equals("分支选择")) {
                                //判断当前节点金额是否需要审批
                                nodes.add(node);
                            }
                        }

                        String preNodeName = "";
                        if (null != nodes && nodes.size() > 0) {
                            for (int i = 0; i < nodes.size(); i++) {
                                NodeModel node = nodes.get(i);
                                if (null != node && node.getName().equals(task.getTaskName()) && i > 0) {
                                    preNodeName = nodes.get(i - 1).getName();
                                    break;
                                }
                            }
                        }
                        if (preNodeName.equals("")) {
                            result.put("ok", true);
                            result.put("msg", "返回到业务修改");
                            baseApproval.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                        } else {
                            flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, preNodeName);
                        }
                    }
                } else {
                    if (null == task) {
                        result.put("ok", false);
                        result.put("msg", "当前节点无法回退到上一步");
                        return result;
                    } else {
                        result.put("ok", true);
                        result.put("msg", "返回到业务修改");
                        baseApproval.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                    }
                }
                break;
            case BACKBEGIN:
                ApprovalResult approvalResultFirst1 = approvalResultService.fetchFirst(extensionId, flowConfigureType);
                baseApproval.setApprovalStatusType(ApprovalStatusType.IN_EDIT);
                if (null != approvalResultFirst1) {
                    if (needRepeatFlow) {
                        flowService.getEngine().executeAndJumpTask(taskId, user.getLogin(), param, approvalResultFirst1.getNodeCode());
                    }
                } else {
                    if (null == task) {
                        result.put("ok", false);
                        result.put("msg", "当前节点无法回退到开始节点");
                        return result;
                    }
                }
                break;
            case DISAGREE:
                baseApproval.setApprovalStatusType(ApprovalStatusType.REJECT);
                flowService.getEngine().order().terminate(orderId, user.getLogin());
                break;
        }
        return null;
    }
}
