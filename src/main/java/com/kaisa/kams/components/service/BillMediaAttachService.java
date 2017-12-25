package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.enums.ProductMediaItemType;
import com.kaisa.kams.models.*;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 票据影像资料
 * Created by lw on 2017/03/01.
 */
@IocBean(fields="dao")
public class BillMediaAttachService extends IdNameEntityService<BillMediaAttach> {

    private static final Log log = Logs.get();

    @Inject
    private MediaTemplateService mediaTemplateService;

    @Inject
    private ProductService productService;

    @Inject
    private BorrowerService borrowerService;


    @Inject
    private LoanService loanService;

    @Inject
    private ProductMediaAttachService productMediaAttachService;

    /**
     * 通过贴现人查找影像和详情
     * @param masterBorrowerId
     * @return
     */
    public NutMap queryBillByBorrowerIdAndType(String masterBorrowerId, String productId ,String loanId) {
        NutMap result = new NutMap();
        //查找到产品
        Product prd = productService.fetchEnableProductById(productId);
        if (null == prd) {
            result.put("ok", false);
            result.put("msg", "查找不到产品");
            return result;
        }
        //根据Id查找
        if(loanId != null){
            Loan resultLoan = loanService.fetchById(loanId);
            if (null != resultLoan) {
                result.setv("loan", resultLoan);
            }
        }

        //根据masterBorrowerIdId查找
        if(masterBorrowerId != null){
            Borrower resultLoan = borrowerService.fetchById(masterBorrowerId);
            if (null != resultLoan) {
                result.setv("masterBorrower", resultLoan);
            }
        }

        List<ProductMediaItem> productMediaAttacheList = null;
        List<BillMediaAttach> billMediaAttachList = dao().fetchLinks(dao().query(BillMediaAttach.class, Cnd.where("masterBorrowerId","=",masterBorrowerId).asc("itemName")), "productMediaAttachDetails",Cnd.where("1","=","1").desc("updateTime"));
        if(billMediaAttachList.isEmpty()){
            productMediaAttacheList = this.queryByTmp(prd.getMediaTmpId(),ProductMediaItemType.BILL);
            result.setv("data", productMediaAttacheList);
        }else {
            for(int i = 0 ; i<billMediaAttachList.size() ; i++){
                productMediaAttacheList = this.queryByTmp(billMediaAttachList.get(i).getTmplId(),ProductMediaItemType.BILL);
                if(!productMediaAttacheList.isEmpty()){
                    for(int j = 0 ; j<productMediaAttacheList.size() ; j++){
                        if(productMediaAttacheList.get(j).getName().equals(billMediaAttachList.get(i).getItemName())){
                            billMediaAttachList.get(i).setCode(productMediaAttacheList.get(j).getCode());
                            break;
                        }
                    }
                }else {
                    billMediaAttachList.get(i).setCode("B00");
                }
            }
            result.setv("data", billMediaAttachList);
            //result.setv("code", productMediaAttacheList != null ? productMediaAttacheList.get(0).getCode() : "B00");
        }
        return result;
    }

    /**
     * 通过类型查找影像和详情
     * @param productMediaItemType
     * @return
     */
    public List<ProductMediaAttach> queryDetailByType(ProductMediaItemType productMediaItemType) {
        return dao().fetchLinks(dao().query(ProductMediaAttach.class, Cnd.where("mediaItemType","=",productMediaItemType).asc("itemName")), "productMediaAttachDetails",Cnd.where("1","=","1").desc("updateTime"));
    }

    /**
     * 通过类型查找影像和详情
     * @param loanId,name
     * @return
     */
    public ProductMediaAttach fetchMediaAttachByLoanIdAndName(String loanId, String name) {
        return dao().fetch(ProductMediaAttach.class, Cnd.where("loanId","=",loanId).and("itemName","=",name));
    }

    /**
     * 通过loanId\name删除
     * @param loanId
     */
    public boolean deleteByLoanIdAndName(String loanId, String name) {
        return dao().clear(ProductMediaAttach.class,Cnd.where("loanId","=",loanId).and("itemName","=",name))>0;
    }

    /**
     * 通过Id查找影像和详情
     * @param loanId
     * @return
     */
    public List<BillMediaAttach> queryBillDetailByLoanIdAndType(String loanId, ProductMediaItemType productMediaItemType) {
        return dao().fetchLinks(dao().query(BillMediaAttach.class, Cnd.where("loanId","=",loanId).and("mediaItemType","=",productMediaItemType).asc("itemName")), "productMediaAttachDetails",Cnd.where("1","=","1").desc("updateTime"));
        //return dao().query(ProductMediaAttach.class, Cnd.where("loanId","=",loanId).and("mediaItemType","=",productMediaItemType).asc("itemName"));
    }

    /**
     * 通过loanId删除
     * @param loanId
     */
    public boolean deleteByLoanIdAndType(String loanId, ProductMediaItemType productMediaItemType) {
        return dao().clear(ProductMediaAttach.class,Cnd.where("loanId","=",loanId).and("mediaItemType","=",productMediaItemType))>0;
    }

    /**
     * 更新
     * @param productMediaAttach
     * @return
     */
    public boolean update(ProductMediaAttach productMediaAttach) {
        if(null==productMediaAttach){
            return false;
        }
        return dao().update(productMediaAttach)>0;
    }


    /**
     * 通过productMediaAttachId删除影像详情
     * @param productMediaAttachId
     */
    public boolean deleteByproductMediaAttachId(String productMediaAttachId) {
        int falg = 0;
        List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttachId));
        if(!pmadList.isEmpty()){
            falg = dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttachId));
        }
        return falg>0;
    }

    public List<ProductMediaAttachDetail> assembleAttachDetail(String mediaDetail){
        List<ProductMediaAttachDetail> productMediaAttachDetailList = Json.fromJsonAsList(ProductMediaAttachDetail.class,mediaDetail);
        return productMediaAttachDetailList;
    }

    /**
     * 新增or修改
     * @param productMediaAttach
     * @return
     */
    public ProductMediaAttach addProductAndBillMediaAttach(ProductMediaAttach productMediaAttach,String mediaDetail,String masterBorrowerId,String tmplId,String productId) {
        if(null==productMediaAttach){
            return null;
        }
        List<ProductMediaAttachDetail> productMediaAttachDetailList = null;
        if(null != mediaDetail){
            productMediaAttachDetailList = assembleAttachDetail(mediaDetail);
        }
        BillMediaAttach billMediaAttach = new BillMediaAttach();
        List<BillMediaAttach> billMediaAttachList = dao().query(BillMediaAttach.class,Cnd.where("masterBorrowerId","=",masterBorrowerId).and("itemName","=",productMediaAttach.getItemName()));
        if(billMediaAttachList.isEmpty()) {
            billMediaAttach.setTmplId(tmplId);
            billMediaAttach.setRequired(productMediaAttach.isRequired());
            billMediaAttach.setMediaItemType(productMediaAttach.getMediaItemType());
            billMediaAttach.setMasterBorrowerId(masterBorrowerId);
            billMediaAttach.setItemName(productMediaAttach.getItemName());
            billMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
            billMediaAttach.setCreateTime(new Date());
            billMediaAttach.setUpdateTime(new Date());
            billMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            billMediaAttach = dao().insert(billMediaAttach);
            if (null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)) {
                for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList) {
                    productMediaAttachDetail.setProductMediaAttachId(billMediaAttach.getId());
                    productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttachDetail.setCreateTime(new Date());
                    productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttachDetail.setUpdateTime(new Date());

                }
            }
            this.dao().insert(productMediaAttachDetailList);
        }
        productMediaAttach.setId(null);
        productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach.setUpdateTime(new Date());
        productMediaAttach.setCreateTime(new Date());
        productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
        productMediaAttach = productMediaAttachService.add(productMediaAttach);

        if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)){
            for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList){
                productMediaAttachDetail.setProductMediaAttachId(productMediaAttach.getId());
                productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                productMediaAttachDetail.setCreateTime(new Date());
                productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                productMediaAttachDetail.setUpdateTime(new Date());

            }
        }
        this.dao().insert(productMediaAttachDetailList);
        return productMediaAttach;
    }

    /**
     * 新增or修改
     * @param productMediaAttach
     * @return
     */
    public ProductMediaAttach updateProductAndBillMediaAttach(ProductMediaAttach productMediaAttach,String mediaDetail,String masterBorrowerId,String tmplId,String loanId) {
        if(null==productMediaAttach){
            return null;
        }
        List<ProductMediaAttachDetail> productMediaAttachDetailList = null;
        if(null != mediaDetail){
            productMediaAttachDetailList = assembleAttachDetail(mediaDetail);
        }
        BillMediaAttach billMediaAttach = new BillMediaAttach();
        boolean isChange = true;
        List<ProductMediaAttachDetail> billMediaAttachDetailList = null;
        List<BillMediaAttach> billMediaAttachList = dao().query(BillMediaAttach.class,Cnd.where("masterBorrowerId","=",masterBorrowerId).and("itemName","=",productMediaAttach.getItemName()));
        if(billMediaAttachList.isEmpty()){
            billMediaAttach.setTmplId(tmplId);
            billMediaAttach.setMasterBorrowerId(masterBorrowerId);
            billMediaAttach.setRequired(productMediaAttach.isRequired());
            billMediaAttach.setItemName(productMediaAttach.getItemName());
            billMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
            billMediaAttach.setCreateTime(new Date());
            billMediaAttach.setUpdateTime(new Date());
            billMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            billMediaAttach = dao().insert(billMediaAttach);
            if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)){
                for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList){
                    productMediaAttachDetail.setProductMediaAttachId(billMediaAttach.getId());
                    productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttachDetail.setCreateTime(new Date());
                    productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttachDetail.setUpdateTime(new Date());

                }
            }
            this.dao().insert(productMediaAttachDetailList);
        }else {
            for (BillMediaAttach billMediaAttachDiff : billMediaAttachList){
                //判断多条明细是否修改
                billMediaAttachDetailList = dao().query(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",billMediaAttachDiff.getId()));
                if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail) && null != billMediaAttachDetailList){
                    isChange = this.compare(billMediaAttachDetailList,productMediaAttachDetailList);

                }
                if(null != billMediaAttachDiff && productMediaAttach.getItemName().equals(billMediaAttachDiff.getItemName())){
                    this.deleteByproductMediaAttachId(billMediaAttachDiff.getId());
                    if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)){
                        billMediaAttachDiff.setUpdateBy(ShiroSession.getLoginUser().getName());
                        billMediaAttachDiff.setUpdateTime(new Date());
                        dao().update(billMediaAttachDiff);
                        for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList){
                            productMediaAttachDetail.setProductMediaAttachId(billMediaAttachDiff.getId());
                            productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                            productMediaAttachDetail.setCreateTime(new Date());
                            productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                            productMediaAttachDetail.setUpdateTime(new Date());
                        }
                    }
                    this.dao().insert(productMediaAttachDetailList);
                }else {
                    if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)){
                        BillMediaAttach newBillMediaAttach = new BillMediaAttach();
                        newBillMediaAttach.setTmplId(tmplId);
                        newBillMediaAttach.setMasterBorrowerId(masterBorrowerId);
                        newBillMediaAttach.setItemName(billMediaAttachDiff.getItemName());
                        newBillMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
                        newBillMediaAttach.setCreateTime(new Date());
                        newBillMediaAttach.setUpdateTime(new Date());
                        newBillMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
                        newBillMediaAttach = dao().insert(newBillMediaAttach);
                        for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList){
                            productMediaAttachDetail.setProductMediaAttachId(billMediaAttachDiff.getId());
                            productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                            productMediaAttachDetail.setCreateTime(new Date());
                            productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                            productMediaAttachDetail.setUpdateTime(new Date());
                        }
                    }
                    this.dao().insert(productMediaAttachDetailList);
                }
            }
        }

        if(!isChange){
            productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach.setUpdateTime(new Date());
            //productMediaAttach.setCreateTime(new Date());
            //productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            productMediaAttachService.update(productMediaAttach);
        }

        ProductMediaAttach p = new ProductMediaAttach();
        p = this.fetchMediaAttachByLoanIdAndName(loanId,productMediaAttach.getItemName());
        if(null!= p && null!= p.getId()){
            this.deleteByproductMediaAttachId(p.getId());
        }else {
            //this.deleteByLoanIdAndName(productMediaAttach.getLoanId(),productMediaAttach.getItemName());
            productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach.setUpdateTime(new Date());
            productMediaAttach.setCreateTime(new Date());
            productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach = productMediaAttachService.add(productMediaAttach);
        }


        if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail)){
            for (ProductMediaAttachDetail productMediaAttachDetail : productMediaAttachDetailList){
                productMediaAttachDetail.setProductMediaAttachId(p==null?productMediaAttach.getId():p.getId());
                productMediaAttachDetail.setCreateBy(ShiroSession.getLoginUser().getName());
                productMediaAttachDetail.setCreateTime(new Date());
                productMediaAttachDetail.setUpdateBy(ShiroSession.getLoginUser().getName());
                productMediaAttachDetail.setUpdateTime(new Date());

            }
        }
        this.dao().insert(productMediaAttachDetailList);
        return productMediaAttach;
    }

    /**
     * 根据tempId查询
     *
     * @param tmplId
     * @return
     */
    public List<ProductMediaItem> queryByTmp(String tmplId,ProductMediaItemType productMediaItemType) {
        return dao().query(ProductMediaItem.class, Cnd.where("tmplId", "=", tmplId).and("mediaItemType","=",productMediaItemType).asc("name"));
    }

    /**
     * 比较
     * @param
     * @param a
     * @param b
     * @return
     */
    public static boolean compare(List<ProductMediaAttachDetail> a, List<ProductMediaAttachDetail> b) {
        if(a.size() != b.size())
            return false;
        Collections.sort(a,new UrlComparator());
        Collections.sort(b,new UrlComparator());
        for(int i=0;i<a.size();i++){
            if(!a.get(i).getUrl().equals(b.get(i).getUrl()))
                return false;
        }
        return true;
    }

    public static class UrlComparator implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            ProductMediaAttachDetail p1 = (ProductMediaAttachDetail) object1; // 强制转换
            ProductMediaAttachDetail p2 = (ProductMediaAttachDetail) object2;
            return new String(p1.getUrl()).compareTo(new String(p2.getUrl()));
        }
    }

    /**
     * 比较
     * @param
     * @param a
     * @param b
     * @return
     */
    public static boolean mediaAttachcompare(List<ProductMediaAttach> a, List<ProductMediaAttach> b) {
        if(a.size() != b.size())
            return false;
        Collections.sort(a,new NameComparator());
        Collections.sort(b,new NameComparator());
        for(int i=0;i<a.size();i++){
            if(!a.get(i).getItemName().equals(b.get(i).getItemName()))
                return false;
        }
        return true;
    }

    public static class NameComparator implements Comparator {
        public int compare(Object object1, Object object2) {// 实现接口中的方法
            ProductMediaAttach p1 = (ProductMediaAttach) object1; // 强制转换
            ProductMediaAttach p2 = (ProductMediaAttach) object2;
            return new String(p1.getItemName()).compareTo(new String(p2.getItemName()));
        }
    }

}
