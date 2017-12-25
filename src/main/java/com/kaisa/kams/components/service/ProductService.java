package com.kaisa.kams.components.service;

import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.utils.flow.ApproveWarnMessageUtils;
import com.kaisa.kams.components.view.product.BusinessUserView;
import com.kaisa.kams.components.view.product.OrganizeNode;
import com.kaisa.kams.components.view.product.ProductView;
import com.kaisa.kams.enums.BusinessOrganizeType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.flow.FlowConfigure;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductProcess;
import com.kaisa.kams.models.business.BusinessAgency;
import com.kaisa.kams.models.business.BusinessOrganize;
import com.kaisa.kams.models.business.ProductsToOrganize;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.FieldMatcher;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import java.util.*;

/**
 * 产品服务层
 * Created by lw on 2016/11/23.
 */
@IocBean(fields="dao")
public class ProductService extends IdNameEntityService<Product> {

    private final static String PRODUCT_ORGANIZE_PRE = "XS";

    @Inject
    private FlowConfigureService flowConfigureService;

    /**
     * 新增产品
     * @param product
     * @return
     */
    public Product add(Product product) {
        if (null==product){
            return null;
        }
        return  dao().insert(product);
    }

    /**
     * 修改产品
     * @param product
     * @return
     */
    public boolean update(Product product) {
        if (null==product) {
            return false;
        }
        //忽略少数字段更新
       return Daos.ext(dao(), FieldFilter.locked(Product.class, "^id|code|createBy|createTime$")).update(product)>0;
    }

    /**
     * 验证产品编码是否可用
     * @param code
     * @return
     */
    public boolean enableCode(String code) {
        return null == dao().fetch(Product.class,Cnd.where("code","=", code));
    }

    /**
     * 获取产品列表
     * @return
     */
    public List<Product> queryListAll() {
        FieldMatcher fieldMatcher = new FieldMatcher();
        fieldMatcher.setActived("^id|name|code|typeId|alias|createTime|status|flowConfigStatus$");
        return dao().query(Product.class,Cnd.orderBy().desc("createTime"),null,fieldMatcher);
    }

    /**
     * 获取可用的产品列表
     * @return
     */
    public List<Product> queryAvailableProductList(FlowConfigureType flowConfigureType) {
        String  disableProductIds = flowConfigureService.queryDisableProductIdByFlowConfigureType(flowConfigureType);
        if(ApproveWarnMessageUtils.COMMON_PRODUCT_ID.equals(disableProductIds)){//匹配所有，则所有产品都绑定了，不能再为其绑定，不能返回任何产品
            return null;
        }else{
            FieldMatcher fieldMatcher = new FieldMatcher();
            fieldMatcher.setActived("^id|name|code|typeId|alias|createTime|status|flowConfigStatus$");
            Cnd cnd =  Cnd.where("status","=",PublicStatus.ABLE).and("id","not in",disableProductIds.split(","));
            cnd.orderBy().desc("createTime");
            return dao().query(Product.class,cnd,null,fieldMatcher);
        }
    }
    /**
     * 获取可展示的产品列表，可用的产品与当前产品的并集
     * @return
     */
    public List<Product> queryDisplayedProductList(FlowConfigureType flowConfigureType,String id){
        FieldMatcher fieldMatcher = new FieldMatcher();
        fieldMatcher.setActived("^id|name|code|typeId|alias|createTime|status|flowConfigStatus$");
        List<Product> availabes  = queryAvailableProductList(flowConfigureType);
        List<Product> products = new ArrayList<Product>();
        if(StringUtils.isNotEmpty(id)){
            FlowConfigure flowConfigure = flowConfigureService.getSimpleFlowConfigureById(id);
            if(flowConfigure!=null){
                String productIds = flowConfigure.getProductId();
                if(ApproveWarnMessageUtils.COMMON_PRODUCT_ID.equals(productIds)){//如果是通用，查询所有的
                    products = dao().query(Product.class,Cnd.where("status","=",PublicStatus.ABLE).desc("createTime"),null,fieldMatcher);
                }else{
                    products = dao().query(Product.class,Cnd.where("status","=",PublicStatus.ABLE).and("id","in",productIds.split(",")).desc("createTime"),null,fieldMatcher);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(availabes)){
            availabes.removeAll(products);
            products.addAll(availabes);
        }
        return products;
    }

    /**
     * 验证产品名称是否可用
     * @param name
     * @return
     */
    public boolean enableName(String id,String name) {
        if(id ==null){
            return null == dao().fetch(Product.class, Cnd.where("name", "=", name));
        }
        return null == dao().fetch(Product.class, Cnd.where("name", "=", name).and("id", "!=", id));
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public Product fetchEnableProductById(String id) {
        Product product = dao().fetch(Product.class,Cnd.where("status","=",PublicStatus.ABLE).and("id","=",id));
        return formatProductInterest(product);
    }

    public  Product fetchLinksProductById(String id){
        if(id!=null){
            return  dao().fetchLinks(dao().fetch(Product.class,Cnd.where("status","=",PublicStatus.ABLE).and("id","=",id)),"productInfoTmpl");
        }
          return null;
    }

    /**
     * 获取机构组织信息
     * @param productId
     * @return
     */
    public List<OrganizeNode> getOrganizeProduct(String productId) {
        List<OrganizeNode> organizeNodeList = new ArrayList<>();
        OrganizeNode node;

        List<BusinessAgency> agencyList = dao().query(BusinessAgency.class,Cnd.where("status","=",PublicStatus.ABLE));
        List<BusinessOrganize> organizeList = dao().query(BusinessOrganize.class,Cnd.where("status","=",PublicStatus.ABLE));
        List<ProductsToOrganize> productsOrganizeList = dao().query(ProductsToOrganize.class,Cnd.where("productId","=",productId));
        boolean isChecked = CollectionUtils.isNotEmpty(productsOrganizeList);


        if (CollectionUtils.isNotEmpty(agencyList)) {
            for (BusinessAgency agency : agencyList) {
                node = new OrganizeNode("a_"+agency.getId(),"a_"+agency.getParentId(),agency.getName(),false,true);
                organizeNodeList.add(node);
            }
        }
        if (CollectionUtils.isNotEmpty(organizeList)) {
            for (BusinessOrganize organize:organizeList) {
                if (BusinessOrganizeType.AGENCY == organize.getOrganizeType()) {
                    node = new OrganizeNode("o_" + organize.getId(), "a_" + organize.getAgencyId(), organize.getName(), isChecked?isChecked(productsOrganizeList,organize.getId()):isChecked,false);
                } else {
                    node = new OrganizeNode("o_" + organize.getId(), "o_" + organize.getParentId(), organize.getName(), isChecked?isChecked(productsOrganizeList,organize.getId()):isChecked,false);
                }
                organizeNodeList.add(node);
            }
        }
        return organizeNodeList;
    }

    private boolean isChecked(List<ProductsToOrganize> productsOrganizeList, String organizeId) {
        for (ProductsToOrganize organize:productsOrganizeList) {
            if (organizeId.equals(organize.getOrganizeId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取产品关联的机构信息
     * @param productId
     * @return
     */
    public List<ProductsToOrganize> listProductOrganize(String productId) {
        if (StringUtils.isEmpty(productId)) {
            return null;
        }
        return dao().query(ProductsToOrganize.class, Cnd.where("productId","=",productId));
    }


    /**
     * 添加修改信息
     * @param productId
     * @param nodes
     * @return
     */
    public boolean addProductToOrganize(String productId,List<OrganizeNode> nodes) {

        if (StringUtils.isEmpty(productId)) {
            return false;
        }

        Product product = dao().fetch(Product.class,productId);

        if (null == product) {
            return false;
        }
        //事务处理
        Trans.exec(new Atom(){
            public void run() {
                //清除原来数据
                dao().clear(ProductsToOrganize.class,Cnd.where("productId","=",productId));

                List<ProductsToOrganize> addList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(nodes)) {
                    ProductsToOrganize addNode = null;
                    int index = 1;
                    for(OrganizeNode node: nodes) {
                        if (node.getId().contains("o")) {
                            addNode = new ProductsToOrganize();
                            addNode.setId(addNode.uuid());
                            addNode.setCode(PRODUCT_ORGANIZE_PRE + product.getCode() + String.format("%03d", index));
                            addNode.setOrganizeId(node.getId().substring(2));
                            addNode.setOrganizeName(node.getName());
                            addNode.setProductId(productId);
                            addList.add(addNode);
                            index++;
                        }
                    }
                    //添加数据
                    dao().fastInsert(addList);
                }
            }
        });

        return true;
    }


    /**
     * 启用产品
     * @param productId
     * @return
     */
    public boolean updateProcessAndStatus(String productId) {
        Product product = dao().fetch(Product.class,productId);
        if (null==product) {
            return false;
        }
        product.setFlowConfigStatus(true);
        return dao().update(product,"^(flowConfigStatus|processId)$")>0;
    }

    public void bindProcessIdForProduct(String productId,String processId,FlowConfigureType flowType){

        Cnd cnd = Cnd.where("productId","=",productId).and("flowType","=",flowType.name());
        ProductProcess getProductProcess =  dao().fetch(ProductProcess.class,cnd);
        if(getProductProcess!=null){
            getProductProcess.setStatus(PublicStatus.ABLE);
            getProductProcess.setProcessId(processId);
            getProductProcess.updateOperator();
            dao().update(getProductProcess);
        }else{
            ProductProcess productProcess = new ProductProcess();
            productProcess.setStatus(PublicStatus.ABLE);
            productProcess.setFlowType(flowType);
            productProcess.setProcessId(processId);
            productProcess.setProductId(productId);
            productProcess.updateOperator();
            dao().insert(productProcess);
        }
    }

    public void unbindProcessIdForProduct(String productId,FlowConfigureType flowType){
        Cnd cnd = Cnd.where("productId","=",productId).and("flowType","=",flowType.name());
        ProductProcess getProductProcess =  dao().fetch(ProductProcess.class,cnd);
        if(getProductProcess!=null){
            getProductProcess.setStatus(PublicStatus.DISABLED);
            getProductProcess.updateOperator();
            dao().update(getProductProcess);
        }
    }

    /**
     * 禁用
     * @param productId
     * @return
     */
    public boolean updateFlowConfigStatus(String productId) {
        Product product = dao().fetch(Product.class,productId);
        if (null==product) {
            return false;
        }
        product.setFlowConfigStatus(false);
        return dao().update(product,"^(flowConfigStatus)$")>0;
    }

    /**
     * 查询可以申请的产品
     * @param typeId
     * @return
     */
    public List<Product> queryAbleByType(String typeId) {
        return dao().query(Product.class,Cnd.where("status","=", PublicStatus.ABLE).and("typeId","=",typeId).and("flowConfigStatus","=",true).asc("createTime"));
    }

    /**
     * 查询可用的产品
     * @param typeId
     * @return
     */
    public List<Product> queryAbleByTypeForChannel(String typeId) {
        return dao().query(Product.class,Cnd.where("status","=", PublicStatus.ABLE).and("typeId","=",typeId).asc("createTime"));
    }

    /**
     * 查询产品关联的机构下的业务员
     * @param productId
     * @param keyWord
     * @return
     */
    public List<BusinessUserView> getProductUsers(String productId, String keyWord) {
        if (StringUtils.isNotEmpty(productId) && StringUtils.isNotEmpty(keyWord)) {
            Sql sql = Sqls.queryEntity("select u.name name,u.code code,u.id id from sl_business_user u left join sl_business_products_organize o on u.organizeId=o.organizeId\n" +
                    "        where u.status='ABLE' and o.productId=@productId and (u.name like @keyWord or u.code like @keyWord)");//参数化
            sql.params().set("productId", productId);
            sql.params().set("keyWord", "%" + keyWord + "%");
            sql.setEntity(dao().getEntity(BusinessUserView.class));
            dao().execute(sql);
            return sql.getList(BusinessUserView.class);
        }
       return null;

    }
    public List<BusinessUserView> getProductUsers(String keyWord) {
        if (StringUtils.isNotEmpty(keyWord)) {
            Sql sql = Sqls.queryEntity("select u.name name,u.code code,u.id id  from sl_business_user u " +
                    "        where u.status='ABLE' and (u.name like @keyWord or u.code like @keyWord)");//参数化
            sql.params().set("keyWord", "%" + keyWord + "%");
            sql.setEntity(dao().getEntity(BusinessUserView.class));
            dao().execute(sql);
            return sql.getList(BusinessUserView.class);
        }
        return null;

    }

    public List<ProductView> getProductFlow(String keyWord) {
        if (StringUtils.isNotEmpty(keyWord)) {
            Sql sql = Sqls.queryEntity("select id,code,name from sl_product where status='ABLE' " +
                    "and flowConfigStatus=true  and ( name like @keyWord or code like @keyWord)");//参数化
            sql.params().set("keyWord", "%" + keyWord + "%");
            sql.setEntity(dao().getEntity(ProductView.class));
            dao().execute(sql);
            return sql.getList(ProductView.class);
        }
        return null;
    }

    public List<Product> queryByType(String typeId) {
        return dao().query(Product.class,Cnd.where("status","=", PublicStatus.ABLE).and("typeId","=",typeId).asc("createTime"));
    }

    /**
     * 格式化product中的利率信息
     * @param product
     * @return
     */
    public Product formatProductInterest(Product product) {
        if (null != product) {
            product.setInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getInterestAmount()));
            product.setInterestRate(DecimalFormatUtils.removeZeroFormat(product.getInterestRate()));
            product.setMinInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getMinInterestAmount()));
            product.setMinInterestRate(DecimalFormatUtils.removeZeroFormat(product.getMinInterestRate()));
            product.setDayInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getDayInterestAmount()));
            product.setDayInterestRate(DecimalFormatUtils.removeZeroFormat(product.getDayInterestRate()));
            product.setDayMinInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getDayMinInterestAmount()));
            product.setDayMinInterestRate(DecimalFormatUtils.removeZeroFormat(product.getDayMinInterestRate()));
            product.setMonthInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getMonthInterestAmount()));
            product.setMonthInterestRate(DecimalFormatUtils.removeZeroFormat(product.getMonthInterestRate()));
            product.setMonthMinInterestAmount(DecimalFormatUtils.removeZeroFormat(product.getMonthMinInterestAmount()));
            product.setMonthMinInterestRate(DecimalFormatUtils.removeZeroFormat(product.getMonthMinInterestRate()));
        }
        return product;
    }

    public ProductView fetchProductViewById(String id) {
        Sql sql = Sqls.fetchEntity("SELECT * FROM sl_product where id=@id");
        Entity<ProductView> entity = dao().getEntity(ProductView.class);
        sql.setEntity(entity).params().set("id", id);
        dao().execute(sql);
        return sql.getObject(ProductView.class);
    }

    /**
     * 查询产品关联的机构下的业务员
     * @param productIds
     * @return
     */
    public List<String> getProducts(String productIds) {
        String sqlstr = " select t.id from sl_product t where t.status='ABLE' ";
        Sql sql =null;
        if(StringUtils.isNotEmpty(productIds)&&!productIds.equals(ApproveWarnMessageUtils.COMMON_PRODUCT_ID)){
            sqlstr += " AND t.id in (@productIds) ";
            sql = Sqls.queryEntity(sqlstr);//参数化
            sql.params().set("productIds", productIds.split(","));
        }else{
            sql = Sqls.queryEntity(sqlstr);//参数化
        }
        sql.setCallback(Sqls.callback.strs());
        dao().execute(sql);
        return sql.getList(String.class);
    }

    public String getOneProductIdByProductType(String type){
        String sqlstr  = "select sp.id from sl_product sp left join sl_product_type spt on sp.typeId = spt.id where spt.productType = @type limit 0,1 ";
        Sql sql  = Sqls.queryEntity(sqlstr);
        sql.params().set("type",type);
        sql.setCallback(Sqls.callback.str());
        dao().execute(sql);
        return sql.getString();

    }

    /**
     * 通过ids查询产品
     * @param productIds
     * @return
     */
    public List<Product> getProductsByIds(String productIds) {
        String sqlstr = " select * from sl_product t where t.status='ABLE' ";
        Sql sql =null;
        if(StringUtils.isNotEmpty(productIds)){
            sqlstr += " AND t.id in (@productIds) ";
            sql = Sqls.queryEntity(sqlstr);
            sql.params().set("productIds", productIds.split(","));
            sql.setEntity(dao().getEntity(Product.class));
            dao().execute(sql);
            return sql.getList(Product.class);
        }else{
            return null;
        }
    }

}
