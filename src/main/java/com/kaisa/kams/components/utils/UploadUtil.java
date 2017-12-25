package com.kaisa.kams.components.utils;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.impl.AdaptorErrorContext;
import org.nutz.mvc.upload.FieldMeta;
import org.nutz.mvc.upload.TempFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangjun on 2016/11/18.
 */
public class UploadUtil {
    private static final Log log = Logs.get();

    /**
     * 上传附件
     */
    public static Map upload(TempFile tf, AdaptorErrorContext aec, HttpServletRequest hsr) {
        String realPath = hsr.getSession().getServletContext().getRealPath("/upload");//文件地址
        HashMap result = new HashMap();
        String msg = null;
        if (aec != null && aec.getAdaptorErr() != null) {
            msg = "文件大小不符合规定";
        } else if (tf == null) {
            msg = "空文件";
        } else {
            try {
                File file = tf.getFile(); // 这个是保存的临时文件
                FieldMeta fieldMeta = tf.getMeta(); // 这个原本的文件信息
                String oldName = fieldMeta.getFileLocalName(); // 这个时原本的文件名称
                if (oldName.indexOf(".") == -1) {
                    result.put("msg", "文件无后缀");
                    result.put("success", false);
                    return result;
                }
                File savePath = new File(realPath);
                if (!savePath.exists()) {
                    savePath.mkdirs();
                }
                // 给图片名加上时间戳
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
                oldName = timeStamp + oldName;
                File fullPath = new File(savePath, oldName);

                InputStream in = new FileInputStream(file);
                byte b[] = new byte[(int) file.length()];
                in.read(b);
                in.close();

                FileOutputStream fops = new FileOutputStream(fullPath);
                fops.write(b);
                fops.close();

                String fileName = fullPath.getName();
                String head = fileName.substring(0, fileName.lastIndexOf("."));
                String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
                result.put("success", true);
                result.put("url", fullPath.getPath());
                result.put("name", fileName);
                result.put("head", head);
                result.put("headMini", head.substring(14));
                result.put("prefix", prefix);
                //存入Session
                ArrayList fileArray = new ArrayList();
                if (null != (ArrayList) Mvcs.getHttpSession().getAttribute(
                        "uploadFiles")) {
                    fileArray = (ArrayList) Mvcs.getHttpSession().getAttribute(
                            "uploadFiles");
                    result.put("id", fileArray.size());
                } else
                    result.put("id", 0);
                fileArray.add(result);
                Mvcs.getHttpSession().setAttribute("uploadFiles", fileArray);
            } catch (Exception e) {
                log.error("uploadFile failed:"+e.getMessage());
                msg = "程序错误";
            }
        }
        if (msg != null) {
            result.put("msg", msg);
            result.put("success", false);
        }
        return result;
    }

    /**
     * 删除附件
     */
    public static boolean deleteById(int id, String head, String prefix, HttpServletRequest hsr) {
        String realPath = hsr.getSession().getServletContext().getRealPath("/upload");//文件地址
        int idx = -1;
        String fileName = head + "." + prefix;
        File file = new File(realPath, fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                // 更新Session
                ArrayList<HashMap> fileArray = new ArrayList<HashMap>();
                if (null != Mvcs.getHttpSession().getAttribute(
                        "uploadFiles"))
                    fileArray = (ArrayList) Mvcs.getHttpSession().getAttribute(
                            "uploadFiles");
                for (int i = 0; i < fileArray.size(); i++) {
                    if (id == (int) fileArray.get(i).get("id")) {
                        idx = i;
                        break;
                    }
                }
                if (idx >= 0) {
                    fileArray.remove(idx);
                    Mvcs.getHttpSession().setAttribute("uploadFiles",
                            fileArray);
                }
                log.info("deleteFile succeed!");
                return true;
            } else {
                log.error("deleteFile failed!");
                return false;
            }
        } else {
            log.error("deleteFile failed：file does not exist！");
            return false;
        }
    }

}
