package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.UploadUtil;
import com.kaisa.kams.models.ProductMediaAttach;
import com.kaisa.kams.models.User;
import org.apache.commons.lang3.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.TempFile;
import org.nutz.service.IdNameEntityService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunwanchao on 2016/11/30.
 */
@IocBean(fields = "dao")
public class MediaUploadService extends IdNameEntityService<ProductMediaAttach> {
    public void add(ProductMediaAttach pma){
        this.dao().insert(pma);
    }

    public void add(List<ProductMediaAttach> pma){
        this.dao().insert(pma);
    }

    public List<ProductMediaAttach> queryByLoanId(String loanId) {
        List<ProductMediaAttach> attachList = this.dao().query(ProductMediaAttach.class, Cnd.where("loanId","=",loanId));
        if(attachList != null && attachList.size()>0){
            return attachList;
        }
        //TODO，查模板配置表
        attachList = new ArrayList<>();
        ProductMediaAttach attach1=new ProductMediaAttach();
        attach1.setItemName("身份证复印件正反面");
        ProductMediaAttach attach2=new ProductMediaAttach();
        attach2.setItemName("购车发票");
        ProductMediaAttach attach3=new ProductMediaAttach();
        attach3.setItemName("购房发票");
        attachList.add(attach1);
        attachList.add(attach2);
        attachList.add(attach3);
        return attachList;
    }

    public void updateByLoanIdAndItemName(String loanId, String itemName) {
        User user = ShiroSession.getLoginUser();
        ProductMediaAttach attach = this.dao().fetch(ProductMediaAttach.class,Cnd.where("loanId","=",loanId).and("itemName","=",itemName));
        if(attach==null){
            return;
        }
        attach.setAttachName(null);
        attach.setUrl(null);
        attach.setUpdateBy(user.getLogin());
        attach.setUpdateTime(new Date());
        this.dao().update(attach);
    }

    public void upload(String loanId, String additional, TempFile[] tfs, HttpServletRequest hsr, AdaptorErrorContext aec) {
        String[] strArr = additional.split(",");
        User u = ShiroSession.getLoginUser();
        int index = 0;
        List<ProductMediaAttach> attachList = new ArrayList<>();
        for(String str : strArr){
            String[] itemStr = str.split("-");
            String itemName = itemStr[0];
            ProductMediaAttach pma = new ProductMediaAttach();
            pma.setItemName(itemName);
            pma.setLoanId(loanId);
            if(u!=null){
                pma.setUpdateBy(u.getLogin());
                pma.setCreateBy(u.getLogin());
            }
            Date now = new Date();
            pma.setUpdateTime(now);
            pma.setCreateTime(now);

            if(!"0".equals(itemStr[1]) && !itemName.equals(itemStr[1])){
                TempFile tempFile = tfs[index];
                Map rsg = UploadUtil.upload(tempFile,aec,hsr);
                if((Boolean) rsg.get("success")==true){
                    pma.setAttachName((String)rsg.get("headMini"));
                    pma.setUrl((String)rsg.get("url"));
                }
                index++;
            }
            attachList.add(pma);
        }
        List<ProductMediaAttach> mediaAttachList = this.dao().query(ProductMediaAttach.class, Cnd.where("loanId","=",loanId));
        if(mediaAttachList!=null&&mediaAttachList.size()>0){
            //更新
            Map<String,ProductMediaAttach> name2Map = mediaAttachList.stream().collect(Collectors.toMap(m->m.getItemName(),m->m));
            List<ProductMediaAttach> resultList = new ArrayList<>();
            attachList.forEach(attach->{
                if(name2Map.containsKey(attach.getItemName())){
                    ProductMediaAttach temp = name2Map.get(attach.getItemName());
                    if(StringUtils.isNotEmpty(attach.getUrl())){
                        temp.setUrl(attach.getUrl());
                    }
                    if(StringUtils.isNotEmpty(attach.getAttachName())){
                        temp.setAttachName(attach.getAttachName());
                    }
                    if(u!=null){
                        temp.setUpdateBy(u.getLogin());
                    }
                    temp.setUpdateTime(new Date());
                    resultList.add(temp);
                }
            });
            if(resultList.size()>0){
                this.dao().update(resultList);
            }
            return;
        }
        this.add(attachList);
    }
}
