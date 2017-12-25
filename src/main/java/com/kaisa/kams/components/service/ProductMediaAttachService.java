package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.enums.ProductMediaItemType;
import com.kaisa.kams.models.BillLoan;
import com.kaisa.kams.models.BillMediaAttach;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.ProductMediaAttach;
import com.kaisa.kams.models.ProductMediaAttachDetail;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * 影像资料
 * Created by weid on 2016/12/14.
 */
@IocBean(fields="dao")
public class ProductMediaAttachService extends IdNameEntityService<ProductMediaAttach> {

    @Inject
    private  BillMediaAttachService billMediaAttachService;

    /**
     * 通过Id查找影像和详情
     * @param loanId
     * @return
     */
    public List<ProductMediaAttach> queryDetailByLoanIdAndType(String loanId, ProductMediaItemType productMediaItemType) {
        return dao().fetchLinks(dao().query(ProductMediaAttach.class, Cnd.where("loanId","=",loanId).and("mediaItemType","=",productMediaItemType).asc("itemName")), "productMediaAttachDetails",Cnd.where("1","=","1").desc("updateTime"));
    }

    /**
     * 通过Id查找影像和详情
     * @param loanId
     * @return
     */
    public List<ProductMediaAttach> queryDetailByLoanId(String loanId) {
        return dao().fetchLinks(dao().query(ProductMediaAttach.class, Cnd.where("loanId","=",loanId)), "productMediaAttachDetails",Cnd.where("1","=","1").desc("updateTime"));
    }

    /**
     * 新增
     * @param productMediaAttach
     * @return
     */
    public ProductMediaAttach add(ProductMediaAttach productMediaAttach) {
        if(null==productMediaAttach){
            return null;
        }
        return dao().insert(productMediaAttach);
    }

    /**
     * 新增
     * @param productMediaAttachList
     * @return
     */
    public List add(List<ProductMediaAttach> productMediaAttachList) {
        if(null==productMediaAttachList){
            return null;
        }
        return dao().insert(productMediaAttachList);
    }

    /**
     * 新增明细
     * @param productMediaAttachDetailList
     * @return
     */
    public List addDetail(List<ProductMediaAttachDetail> productMediaAttachDetailList) {
        if(null==productMediaAttachDetailList){
            return null;
        }
        return dao().insert(productMediaAttachDetailList);
    }

    /**
     * 修改
     * @param productMediaAttach
     * @return
     */
    public boolean update(ProductMediaAttach productMediaAttach) {
        if(null==productMediaAttach){
            return false;
        }
        return dao().update(productMediaAttach)>0;
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
    public ProductMediaAttach addProductMediaAttach(ProductMediaAttach productMediaAttach,String mediaDetail) {
        if(null==productMediaAttach){
            return null;
        }
        List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
        if(!pmadList.isEmpty()){
            dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
        }
        List<ProductMediaAttachDetail> productMediaAttachDetailList = null;
        if(null != mediaDetail){
            productMediaAttachDetailList = assembleAttachDetail(mediaDetail);
        }
        if(null != productMediaAttach.getId()){
            productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
            productMediaAttach.setUpdateTime(new Date());
            productMediaAttach.setCreateTime(new Date());
            productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
            dao().update(productMediaAttach);
        }
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

    public ProductMediaAttach fetchLinkById(String id) {
        return dao().fetchLinks(dao().fetch(ProductMediaAttach.class, id), "productMediaAttachDetails");
    }

    /**
     * 风控资料修改
     * @param productMediaAttach
     * @return
     */
    public ProductMediaAttach updateProductMediaAttachByRisk(ProductMediaAttach productMediaAttach,String mediaDetail,String loanId) {
        if(null==productMediaAttach){
            return null;
        }
        BillLoan billLoan = null;
        LoanBorrower loanBorrower = null;
        if(null != loanId){
            billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        }
        if(null != billLoan){
            loanBorrower = this.dao().fetch(LoanBorrower.class, billLoan.getLoan().getMasterBorrowerId());
        }

        List<ProductMediaAttachDetail> productMediaAttachDetailList = null;
        if(null != mediaDetail){
            productMediaAttachDetailList = assembleAttachDetail(mediaDetail);
        }
        //票据修改标示
        boolean isChange = true;
        if(productMediaAttach.getMediaItemType().equals(ProductMediaItemType.BILL) && null != loanBorrower){
            List<ProductMediaAttachDetail> billMediaAttachDetailList = null;
            List<BillMediaAttach> billMediaAttachList = dao().query(BillMediaAttach.class,Cnd.where("masterBorrowerId","=",loanBorrower.getBorrowerId()).and("itemName","=",productMediaAttach.getItemName()));
            if(!billMediaAttachList.isEmpty()){
                for (BillMediaAttach billMediaAttachDiff : billMediaAttachList){
                    //判断多条明细是否修改
                    billMediaAttachDetailList = dao().query(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",billMediaAttachDiff.getId()));
                    if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail) && null != billMediaAttachDetailList){
                        isChange = billMediaAttachService.compare(billMediaAttachDetailList,productMediaAttachDetailList);

                    }
                    if(!isChange){
                        billMediaAttachService.deleteByproductMediaAttachId(billMediaAttachDiff.getId());
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


                        List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
                        if(!pmadList.isEmpty()){
                            dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
                        }
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
                    }
                }
                if(null != productMediaAttach.getId() && !isChange){
                    productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttach.setUpdateTime(new Date());
                    //productMediaAttach.setCreateTime(new Date());
                    //productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
                    dao().update(productMediaAttach);
                }
            }
        }
        else {
            List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
            if(!pmadList.isEmpty()){
                dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
            }
            if(null != productMediaAttach.getId()){
                productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
                productMediaAttach.setUpdateTime(new Date());
                //productMediaAttach.setCreateTime(new Date());
                //productMediaAttach.setCreateBy(ShiroSession.getLoginUser().getName());
                dao().update(productMediaAttach);
            }
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
        }

        return productMediaAttach;
    }

    /**
     * H5风控资料修改
     * @param productMediaAttachId
     * @param mediaDetail
     * @param loanId
     * @return
     */
    public ProductMediaAttach updateMediaAttachByRiskForH5(String productMediaAttachId,String mediaDetail,String loanId) {
        BillLoan billLoan = null;
        LoanBorrower loanBorrower = null;
        if(null != loanId){
            billLoan = this.dao().fetchLinks(this.dao().fetch(BillLoan.class, Cnd.where("loanId", "=", loanId)), "loan");
        }
        if(null != billLoan){
            loanBorrower = this.dao().fetch(LoanBorrower.class, billLoan.getLoan().getMasterBorrowerId());
        }

        List<ProductMediaAttachDetail> productMediaAttachDetailList = null;
        if(null != mediaDetail){
            productMediaAttachDetailList = assembleAttachDetail(mediaDetail);
        }
        ProductMediaAttach productMediaAttach = dao().fetch(ProductMediaAttach.class,productMediaAttachId);
        //票据修改标示
        boolean isChange = true;
        if(productMediaAttach.getMediaItemType().equals(ProductMediaItemType.BILL) && null != loanBorrower){
            List<ProductMediaAttachDetail> billMediaAttachDetailList = null;
            List<BillMediaAttach> billMediaAttachList = dao().query(BillMediaAttach.class,Cnd.where("masterBorrowerId","=",loanBorrower.getBorrowerId()).and("itemName","=",productMediaAttach.getItemName()));
            if(!billMediaAttachList.isEmpty()){
                for (BillMediaAttach billMediaAttachDiff : billMediaAttachList){
                    //判断多条明细是否修改
                    billMediaAttachDetailList = dao().query(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",billMediaAttachDiff.getId()));
                    if(null != productMediaAttach && null != productMediaAttachDetailList && !("[null]").equals(mediaDetail) && null != billMediaAttachDetailList){
                        isChange = billMediaAttachService.compare(billMediaAttachDetailList,productMediaAttachDetailList);

                    }
                    if(!isChange){
                        billMediaAttachService.deleteByproductMediaAttachId(billMediaAttachDiff.getId());
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


                        List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
                        if(!pmadList.isEmpty()){
                            dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
                        }
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
                    }
                }
                if(null != productMediaAttach.getId() && !isChange){
                    productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
                    productMediaAttach.setUpdateTime(new Date());
                    dao().update(productMediaAttach);
                }
            }
        }
        else {
            List<ProductMediaAttachDetail> pmadList = dao().query(ProductMediaAttachDetail.class, Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
            if(!pmadList.isEmpty()){
                dao().clear(ProductMediaAttachDetail.class,Cnd.where("productMediaAttachId","=",productMediaAttach.getId()));
            }
            if(null != productMediaAttach.getId()){
                productMediaAttach.setUpdateBy(ShiroSession.getLoginUser().getName());
                productMediaAttach.setUpdateTime(new Date());
                dao().update(productMediaAttach);
            }
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
        }

        return productMediaAttach;
    }

}
