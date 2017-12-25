package com.kaisa.kams.components.service.flow;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BaseOrderService;
import com.kaisa.kams.components.service.CostExemptionService;
import com.kaisa.kams.components.service.ExtensionService;
import com.kaisa.kams.components.service.LoanOrderService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.MortgageService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.WfTempletSevice;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.data.ApproveWarnMessageData;
import com.kaisa.kams.enums.ApprovalStatusType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.FlowControlType;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.BaseOrder;
import com.kaisa.kams.models.CostExemption;
import com.kaisa.kams.models.CostExemptionOrder;
import com.kaisa.kams.models.Extension;
import com.kaisa.kams.models.ExtensionOrder;
import com.kaisa.kams.models.HouseNoMortgageApply;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanOrder;
import com.kaisa.kams.models.Mortgage;
import com.kaisa.kams.models.MortgageOrder;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductProcess;
import com.kaisa.kams.models.Role;
import com.kaisa.kams.models.WfTemplet;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.models.flow.WfProcess;
import com.kaisa.kams.models.push.LoanPushOrder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by weid on 2016/12/2.
 */
@IocBean(fields = "dao")
public class FlowService extends IdNameEntityService<WfProcess> {

    private static final Log log = Logs.get();

    @Inject
    private SnakerEngine snakerEngine;

    @Inject
    private ProductService productService;

    @Inject
    private LoanOrderService loanOrderService;

    @Inject
    private LoanService loanService;

    @Inject
    private WfTempletSevice wfTempletSevice;

    @Inject
    private FlowConfigureService flowConfigureService;

    @Inject
    private MortgageService mortgageService;
    @Inject
    private ExtensionService extensionService;
    @Inject
    private CostExemptionService costExemptionService;
    @Inject
    private BaseOrderService baseOrderService;

    /**
     * 获取Snaker引擎
     */
    public SnakerEngine getEngine() {
        return snakerEngine;
    }

    /**
     * 部署流程
     *
     * @param productId 产品Id
     * @return String 流程定义id
     */
    public String deploy(String productId) throws UnsupportedEncodingException {
        Product product = productService.fetchEnableProductById(productId);
        if (null == product) {
            return null;
        }
        WfTemplet wfTemplet = wfTempletSevice.getByProductId(productId);
        if (null == wfTemplet) {
            return null;
        }
        String content = wfTemplet.getContent();
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return this.getEngine().process().deploy(new ByteArrayInputStream(content.getBytes("UTF-8")));

    }

    /**
     * 部署新流程方法
     *
     * @param productId 产品Id
     * @return String 流程定义id
     */
    public String deployNewFlow(String productId, String flowConfigureType) throws UnsupportedEncodingException {
        Product product = productService.fetchEnableProductById(productId);
        if (null == product) {
            return null;
        }

      /*  Cnd cnd = Cnd.where("flowType","=",flowConfigureType);
        cnd.and("productId","like","%"+product.getId()+"%");
        FlowConfigure flowConfigure = flowConfigureService.fetch(cnd);
        if(flowConfigure==null){
            flowConfigure = flowConfigureService.getFlowConfigureByFlowTypeAndProductId(flowConfigureType,ApproveWarnMessageUtils.COMMON_PRODUCT_ID);
            if(flowConfigure==null)return null;
        }
*/
        FlowConfigure flowConfigure = flowConfigureService.getFlowConfigureByFlowypeAndProductIdMaybeNull(flowConfigureType, productId);
        WfTemplet wfTemplet = wfTempletSevice.getByProductIdAndType(productId, flowConfigureType);
        if (null == wfTemplet) {
            return null;
        }
        String content = wfTemplet.getContent();
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return this.getEngine().process().deploy(new ByteArrayInputStream(content.getBytes("UTF-8")));

    }


    /**
     * 重新部署流程
     *
     * @param productId 产品Id
     */
    public void redeploy(String productId) {
        Product product = productService.fetchEnableProductById(productId);
        Process process = this.getEngine().process().getProcessByName(product.getCode());
        WfTemplet wfTemplet = wfTempletSevice.getByProductId(productId);
        String content = wfTemplet.getContent();
        if (null != process && null != wfTemplet && StringUtils.isNotEmpty(content)) {
            this.getEngine().process().redeploy(process.getId(), new ByteArrayInputStream(content.getBytes()));
        }

    }

    /**
     * 卸载部署流程
     *
     * @param productId 产品Id
     */
    public void undeploy(String productId) {
        Product product = productService.fetchEnableProductById(productId);
        Process process = this.getEngine().process().getProcessByName(product.getCode());
        if (null != process) {
            snakerEngine.process().undeploy(process.getId());
        }
    }

    /**
     * 启动流程实例并且获取到订单
     */
    public Order startInstanceWithMortgage(Mortgage mortgage, FlowConfigureType flowType) {

        if (null == mortgage || mortgage.getApprovalStatusType() != ApprovalStatusType.IN_EDIT) {
            return null;
        }

        MortgageOrder mortgageOrder = mortgageService.getMortgageOrderByMortgageId(mortgage.getId(), flowType);
        if (null != mortgageOrder) {
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("mortgageId", mortgage.getId());
            variable.put("amount", Double.MAX_VALUE);
            snakerEngine.order().addVariable(mortgageOrder.getOrderId(), variable);
            //延时提醒短信
            //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
            return new Order();
        }

        Product product = productService.fetchEnableProductById(mortgage.getProductId());

        //首先去sl_product_process里面取，娶不到再去productId
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(mortgage.getProductId(), flowType);
        Process process = null;
        if (productProcess != null) {
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if (null == process) {
            return null;
        }

        String user = ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("mortgageId", mortgage.getId());
        args.put("amount", Double.MAX_VALUE);
        Order order = this.getEngine().startInstanceById(process.getId(), user, args);
        if (null != order) {
            //保存录单和流程订单的关联关系
            MortgageOrder newLoanOrder = new MortgageOrder();
            newLoanOrder.setOrderId(order.getId());
            newLoanOrder.setCurrentNode("0");
            newLoanOrder.setFlowControlType(FlowControlType.M_BUSINESS_CONTROL);
            newLoanOrder.setFlowConfigureType(flowType);
            newLoanOrder.setMortgageId(mortgage.getId());
            newLoanOrder.setCreateBy(user);
            newLoanOrder.setCreateTime(new Date());
            newLoanOrder.setUpdateBy(user);
            newLoanOrder.setUpdateTime(new Date());
            mortgageService.addMortgageOrder(newLoanOrder);
        }
        //延时提醒短信
        //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
        return order;
    }

    /**
     * 启动费用减免流程实例并且获取到订单
     */
    public Order startInstanceWithCostExemption(CostExemption costExemption, FlowConfigureType flowType){

        if(null == costExemption || costExemption.getApprovalStatusType()!= ApprovalStatusType.IN_EDIT){
            return null;
        }

        CostExemptionOrder extensionOrder = baseOrderService.getBaseOrderById(CostExemptionOrder.class,"costExemptionId",costExemption.getId(),flowType);
        if(null != extensionOrder){
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("costExemptionId",costExemption.getId());
            variable.put("amount",Double.MAX_VALUE);
            snakerEngine.order().addVariable(extensionOrder.getOrderId(),variable);
            //延时提醒短信
            //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
            return new Order();
        }
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(costExemption.getProductId(),flowType);
        Process process =null;
        if(productProcess!=null){
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if(null == process){
            return null;
        }

        String user =ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("costExemptionId",costExemption.getId());
        args.put("amount",Double.MAX_VALUE);
        Order order  = this.getEngine().startInstanceById(process.getId(),user,args);
        if(null!=order){
            //保存录单和流程订单的关联关系
            try{
                CostExemptionOrder costExemptionOrder = getApprovalOrder(CostExemptionOrder.class,order.getId(),flowType,user);
                costExemptionOrder.setCostExemptionId(costExemption.getId());
                baseOrderService.addBaseOrder(costExemptionOrder);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //延时提醒短信
        //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
        return order;
    }

    private <T extends BaseOrder> T getApprovalOrder(Class<T> clazz, String orderId, FlowConfigureType flowType, String user )throws Exception{
        T baseOrder = clazz.newInstance();
        baseOrder.setOrderId(orderId);
        baseOrder.setCurrentNode("0");
        baseOrder.setFlowControlType(FlowControlType.M_BUSINESS_CONTROL);
        baseOrder.setFlowConfigureType(flowType);
        baseOrder.setCreateBy(user);
        baseOrder.setCreateTime(new Date());
        baseOrder.setUpdateBy(user);
        baseOrder.setUpdateTime(new Date());
        return baseOrder;
    }
    /**
     * 启动展期流程实例并且获取到订单
     */
    public Order startInstanceWithExtension(Extension extension, FlowConfigureType flowType){

        if(null == extension || extension.getApprovalStatusType()!= ApprovalStatusType.IN_EDIT){
            return null;
        }

//        ExtensionOrder extensionOrder = extensionService.getExtensionOrderByExtensionId( extension.getId(),flowType);
        ExtensionOrder extensionOrder  = baseOrderService.getBaseOrderById(ExtensionOrder.class,"extensionId",extension.getId(),flowType);
        if(null != extensionOrder){
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("extensionId",extension.getId());
            variable.put("amount",extension.getAmount());
            snakerEngine.order().addVariable(extensionOrder.getOrderId(),variable);
            //延时提醒短信
            //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
            return new Order();
        }
        //首先去sl_product_process里面取，娶不到再去productId
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(extension.getProductId(),flowType);
        Process process =null;
        if(productProcess!=null){
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if(null == process){
            return null;
        }

        String user =ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("extensionId",extension.getId());
        args.put("amount",extension.getAmount());
        Order order  = this.getEngine().startInstanceById(process.getId(),user,args);
        if(null!=order){
            //保存录单和流程订单的关联关系
            try{
                ExtensionOrder extensionOrder1 = getApprovalOrder(ExtensionOrder.class,order.getId(),flowType,user);
                extensionOrder1.setExtensionId(extension.getId());
                baseOrderService.addBaseOrder(extensionOrder1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //延时提醒短信
        //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
        return order;
    }
    /**
     * 启动流程实例并且获取到订单forNoMortgage
     */
    public Order startInstanceWithNoMortgage(HouseNoMortgageApply mortgage, FlowConfigureType flowType) {

        LoanOrder loanOrder = loanOrderService.fetchByLoanId(mortgage.getId(), flowType);
        if (null != loanOrder) {
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("loanId", mortgage.getId());
            variable.put("amount", Double.MAX_VALUE);
            snakerEngine.order().addVariable(loanOrder.getOrderId(), variable);
            //延时提醒短信
            //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
            return new Order();
        }

        Product product = productService.fetchEnableProductById(mortgage.getProductId());

        //首先去sl_product_process里面取，娶不到再去productId
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(mortgage.getProductId(), flowType);
        Process process = null;
        if (productProcess != null) {
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if (null == process) {
            return null;
        }

        String user = ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("loanId", mortgage.getId());
        args.put("amount", Double.MAX_VALUE);
        Order order = this.getEngine().startInstanceByName(product.getCode(), process.getVersion(), user, args);
        if (null != order) {
            //保存录单和流程订单的关联关系
            LoanOrder newLoanOrder = new LoanOrder();
            newLoanOrder.setOrderId(order.getId());
            newLoanOrder.setCurrentNode("0");
            newLoanOrder.setFlowControlType(FlowControlType.BUSINESS_CONTROL);
            newLoanOrder.setFlowConfigureType(flowType);
            newLoanOrder.setLoanId(mortgage.getId());
            newLoanOrder.setCreateBy(user);
            newLoanOrder.setCreateTime(new Date());
            newLoanOrder.setUpdateBy(user);
            newLoanOrder.setUpdateTime(new Date());
            loanOrderService.add(newLoanOrder);
        }
        //延时提醒短信
        //sendWarnMessageForMortgage(mortgageOrder.getOrderId(), mortgage);
        return order;
    }

    /**
     * 启动流程实例并且获取到订单
     *
     * @param productId 产品Id
     */
    public Order startInstance(String productId, String loanId, FlowConfigureType flowType) {

        Loan loan = loanService.fetchById(loanId);
        if(null == loan || !"CHANNELSAVE_SAVE".contains(loan.getLoanStatus().name()) ){
            return null;
        }

        LoanOrder loanOrder = loanOrderService.fetchByLoanId(loanId, flowType);
        if (null != loanOrder) {
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("loanId", loanId);
            variable.put("amount", loan.getAmount().doubleValue());
            snakerEngine.order().addVariable(loanOrder.getOrderId(), variable);
            //延时提醒短信
            sendWarnMessage(loanOrder.getOrderId(), loan);
            return new Order();
        }

        Product product = productService.fetchEnableProductById(productId);

        //首先去sl_product_process里面取，娶不到再去productId
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(productId, flowType);
        Process process = null;
        if (productProcess != null) {
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if (null == process) {
            return null;
        }

        String user = ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("loanId", loanId);
        args.put("amount", loan.getAmount().doubleValue());
        Order order = this.getEngine().startInstanceByName(product.getCode(), process.getVersion(), user, args);
        if (null != order) {
            //保存录单和流程订单的关联关系
            LoanOrder newLoanOrder = new LoanOrder();
            newLoanOrder.setOrderId(order.getId());
            newLoanOrder.setCurrentNode("0");
            newLoanOrder.setFlowControlType(FlowControlType.BUSINESS_CONTROL);
            newLoanOrder.setFlowConfigureType(flowType);
            newLoanOrder.setLoanId(loanId);
            newLoanOrder.setCreateBy(user);
            newLoanOrder.setCreateTime(new Date());
            newLoanOrder.setUpdateBy(user);
            newLoanOrder.setUpdateTime(new Date());
            loanOrderService.add(newLoanOrder);
        }
        //延时提醒短信
        sendWarnMessage(order.getId(), loan);
        return order;
    }

    /**
     * 启动流程实例并且获取到订单IntermediaryApply
     *
     * @param productId 产品Id
     */
    public Order startInstanceForIntermediaryApply(String productId, String loanId, FlowConfigureType flowType) {

        Loan loan = loanService.fetchById(loanId);

        LoanOrder loanOrder = loanOrderService.fetchByLoanId(loanId, flowType);
        if (null != loanOrder) {
            //查找到订单出来返回
            //修改单的金额
            Map variable = new HashMap<>();
            variable.put("loanId", loanId);
            if (FlowConfigureType.BROKERAGE_FEE.equals(flowType)) {
                variable.put("amount", loan.getAmount().doubleValue());
            } else if (FlowConfigureType.DECOMPRESSION.equals(flowType)) {
                variable.put("amount", loan.getActualAmount().doubleValue());
            }
            snakerEngine.order().addVariable(loanOrder.getOrderId(), variable);
            //延时提醒短信
            //sendWarnMessage(loanOrder.getOrderId(), loan);
            return new Order();
        }


        Product product = productService.fetchEnableProductById(productId);

        //首先去sl_product_process里面取，娶不到再去productId
        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(productId, flowType);
        Process process = null;
        if (productProcess != null) {
            process = this.getEngine().process().getProcessById(productProcess.getProcessId());
        }
        if (null == process) {
            return null;
        }

        String user = ShiroSession.getLoginUser().getName();
        Map args = new HashMap<>();
        args.put("loanId", loanId);
        if (FlowConfigureType.BROKERAGE_FEE.equals(flowType)) {
            args.put("amount", loan.getAmount().doubleValue());
        } else if (FlowConfigureType.DECOMPRESSION.equals(flowType)) {
            args.put("amount", loan.getActualAmount().doubleValue());
        }
        Order order = this.getEngine().startInstanceByName(product.getCode(), process.getVersion(), user, args);
        if (null != order) {
            //保存录单和流程订单的关联关系
            LoanOrder newLoanOrder = new LoanOrder();
            newLoanOrder.setOrderId(order.getId());
            newLoanOrder.setCurrentNode("0");
            newLoanOrder.setFlowControlType(FlowControlType.BUSINESS_CONTROL);
            newLoanOrder.setFlowConfigureType(flowType);
            newLoanOrder.setLoanId(loanId);
            newLoanOrder.setCreateBy(user);
            newLoanOrder.setCreateTime(new Date());
            newLoanOrder.setUpdateBy(user);
            newLoanOrder.setUpdateTime(new Date());
            loanOrderService.add(newLoanOrder);
        }
        //延时提醒短信
        //sendWarnMessage(order.getId(),loan);
        return order;
    }


    /**
     * 执行任务
     */
    public void executeTask(String taskId) {
        this.getEngine().executeTask(taskId, ShiroSession.getLoginUser().getName());
    }


    /**
     * 查看当前snaker文件是否存在
     */
    public boolean existSnakerFile(String productId, FlowConfigureType flowConfigureType) {
        if (StringUtils.isEmpty(productId)) {
            return false;
        }
        WfTemplet wfTemplet = wfTempletSevice.getByProductIdAndType(productId, flowConfigureType.name());
        if (wfTemplet == null) {
            wfTemplet = wfTempletSevice.getByProductIdAndType(productId, null);
        }
        return wfTemplet != null;
    }

    public String getProcessIdByLoanOrder(LoanOrder loanOrder) {
        String orderId = loanOrder.getOrderId();
        return getProcessIdByOrderId(orderId);
    }

    public String getProcessIdByOrderId(String orderId) {
        if (StringUtils.isNotEmpty(orderId)) {
            Sql sql = Sqls.queryEntity("select process_Id from wf_order where id = @id limit 1");
            sql.params().set("id", orderId);
            setProcessIdCallBack(sql);
            dao().execute(sql);
            List<String> result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
            sql = Sqls.queryEntity("select process_Id from wf_hist_order where id = @id limit 1");
            sql.params().set("id", orderId);
            setProcessIdCallBack(sql);
            dao().execute(sql);
            result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
        }
        return null;
    }

    public String getProcessIdByMortgageId(String mortgageId) {
        if (StringUtils.isNotEmpty(mortgageId)) {
            Sql sql = Sqls.queryEntity("select process_Id from wf_order where variable like @mortgageId limit 1");
            sql.params().set("mortgageId", "%" + mortgageId + "%");
            setProcessIdCallBack(sql);
            dao().execute(sql);
            List<String> result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
            sql = Sqls.queryEntity("select process_Id from wf_hist_order where variable like @mortgageId limit 1");
            sql.params().set("mortgageId", "%" + mortgageId + "%");
            setProcessIdCallBack(sql);
            dao().execute(sql);
            result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
        }
        return null;
    }

    public String getProcessId(String loanId) {
        if (StringUtils.isNotEmpty(loanId)) {
            Sql sql = Sqls.queryEntity("select process_Id from wf_order where variable like @loanId limit 1");
            sql.params().set("loanId", "%" + loanId + "%");
            setProcessIdCallBack(sql);
            dao().execute(sql);
            List<String> result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
            sql = Sqls.queryEntity("select process_Id from wf_hist_order where variable like @loanId limit 1");
            sql.params().set("loanId", "%" + loanId + "%");
            setProcessIdCallBack(sql);
            dao().execute(sql);
            result = sql.getList(String.class);
            if (CollectionUtils.isNotEmpty(result)) {
                return result.get(0);
            }
        }
        return null;
    }

    private void setProcessIdCallBack(Sql sql) {
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<String> list = new LinkedList<>();
                while (rs.next())
                    list.add(rs.getString("process_Id"));
                return list;
            }
        });
    }

    public void sendWarnMessage(String orderId, Loan loan) {
        String taskId = getTaskId(orderId);
        if (StringUtils.isEmpty(taskId)) {
            return;
        }
        ApproveWarnMessageData data = ApproveWarnMessageData.create(taskId, loan);
        ApproveWarnMessageUtils.addTask(data);
    }

    public void sendWarnMessageForMortgage(String orderId, Mortgage mortgage) {
        String taskId = getTaskId(orderId);
        if (StringUtils.isEmpty(taskId)) {
            return;
        }
        ApproveWarnMessageData data = ApproveWarnMessageData.create(taskId, mortgage);
        ApproveWarnMessageUtils.addTask(data);
    }


    private String getTaskId(String orderId) {
        return null;
    }

    public boolean startInstanceLoanPush(String productId, LoanPushOrder loanPushOrder, FlowConfigureType flowType) {

        LoanOrder loanOrder = loanOrderService.fetchByLoanId(loanPushOrder.getId(), flowType);

        if (null != loanOrder) {
            Map variable = setLoanPushVariable(loanPushOrder);
            snakerEngine.order().addVariable(loanOrder.getOrderId(), variable);
            return true;
        }

        ProductProcess productProcess = flowConfigureService.fetchEnableProductProductById(productId, flowType);
        if (productProcess == null) {
            return false;
        }
        Process process = getEngine().process().getProcessById(productProcess.getProcessId());
        if (null == process) {
            return false;
        }

        String user = ShiroSession.getLoginUser().getName();
        Map variable = setLoanPushVariable(loanPushOrder);
        Order order = getEngine().startInstanceById(process.getId(), user, variable);
        if (null == order) {
            return false;
        }
        saveLoanOrder(order, flowType, loanPushOrder.getId());
        return true;
    }

    private Map setLoanPushVariable(LoanPushOrder loanPushOrder) {
        Map variable = new HashMap<>();
        variable.put("loanPushOrderId", loanPushOrder.getId());
        variable.put("amount", loanPushOrder.getAmount().doubleValue());
        return variable;
    }

    private void saveLoanOrder(Order order, FlowConfigureType flowType, String loanPushOrderId) {
        Date now = new Date();
        String user = ShiroSession.getLoginUser().getName();
        LoanOrder newLoanOrder = new LoanOrder();
        newLoanOrder.setOrderId(order.getId());
        newLoanOrder.setCurrentNode("0");
        newLoanOrder.setFlowControlType(FlowControlType.BUSINESS_CONTROL);
        newLoanOrder.setFlowConfigureType(flowType);
        newLoanOrder.setLoanId(loanPushOrderId);
        newLoanOrder.setCreateBy(user);
        newLoanOrder.setCreateTime(now);
        newLoanOrder.setUpdateBy(user);
        newLoanOrder.setUpdateTime(now);
        loanOrderService.add(newLoanOrder);
    }

    public boolean isContainTaskActor(String taskId, String loginName) {
        Sql sql = Sqls.fetchInt("select count(*) from wf_task_actor where task_Id=@taskId and actor_Id=@loginName");
        sql.setParam("taskId",taskId);
        sql.setParam("loginName",loginName);
        dao().execute(sql);
        return sql.getInt()>=1;
    }

    public List<WorkItem> getWorkItemList(List<Role> roles){
        String[] roleIds = new String[roles.size()];
        for (int i=0; i<roles.size(); i++){
            Role role = roles.get(i);
            roleIds[i] = role.getId()+"";
        }

        //根据角色Id获取到当前需要处理的节点
        Page<WorkItem> majorPage = new Page<WorkItem>(Integer.MAX_VALUE);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOperators(roleIds);
        queryFilter.setTaskType(TaskModel.TaskType.Major.ordinal());

        List<WorkItem> majorWorks =  getEngine().query().getWorkItems(majorPage,queryFilter);
        return majorWorks;
    }
}
