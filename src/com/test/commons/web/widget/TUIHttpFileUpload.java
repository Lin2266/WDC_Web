package com.test.commons.web.widget;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 協助接收來自網頁上傳的檔案的工具.
 * @deprecated
 */
@Deprecated
public class TUIHttpFileUpload {
    private static final Logger log = LoggerFactory.getLogger(TUIHttpFileUpload.class);
    
    /**
     * 接收上傳檔案.
     * @param request
     * @param saveDir 上傳檔案存放之實體路徑
     * @param savedFilePrefix 上傳檔案存檔時的檔名, 前端附加的字串, 如果為 null 則不附加.
     * @param savedFileSuffix 上傳檔案存檔時的檔名, 末端附加的字串, 如果為 null 則不附加.
     * @param fileSizeLimit
     * @return 已存檔後的 File 物件陣列.
     */
    public static File[] uploadFile(HttpServletRequest request, File saveDir, String savedFilePrefix, String savedFileSuffix, int fileSizeLimit) {
        //使用 Jakarta commons-fileupload, commons-io 套件

        try {
            if(!ServletFileUpload.isMultipartContent(request))
                throw new Exception("this request is not a multi-part request!");

            List<File> savedFiles = new ArrayList<File>();
            if(!saveDir.isDirectory()) {
                log.warn("file upload: " + saveDir + " doesn't exist, create it.");
                if(!saveDir.mkdirs())
                    throw new Exception("create dir '" + saveDir + "' failed.");
            }

            //第一參數: 記憶體中最多能保存檔案內容的緩衝區大小
            //第二參數: 暫存目錄
            DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, saveDir);
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(fileSizeLimit);
            Iterator<FileItem> i = upload.parseRequest(request).iterator();
            while(i.hasNext()) {
                FileItem item = i.next();
                if(item.isFormField()) //如果是一般的 form 參數
                    continue;

                String fileName = item.getName(); //可能含 client 端的路徑字串
                if("".equals(fileName.trim()))
                    continue;

                //取檔名
                fileName = new File(fileName).getName();
                if(fileName.indexOf("\\") != -1 && !fileName.endsWith("\\")) //server 為 Unix, client 為 Windows 時, 仍會把 local 路徑當成檔名的一部分
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                log.info("file upload: received uploaded file '" + fileName + "'");
                String tmp;
                if(savedFilePrefix != null && !((tmp = savedFilePrefix.trim()).equals("")))
                    fileName = tmp + fileName;
                if(savedFileSuffix != null && !((tmp = savedFileSuffix.trim()).equals("")))
                    fileName += tmp;

                //欲存成檔案的 File 物件, 如果已存在同檔名的檔案, 在檔名後加 .n
                File savedFile = new File(saveDir, fileName);
                for(int m = 1; savedFile.isFile();  m++)
                    savedFile = new File(saveDir, fileName + "." + m);

                //寫入檔案
                item.write(savedFile);
                savedFiles.add(savedFile);
                log.info("file upload: save as '" + savedFile.getPath() + "'");
            }

            File[] savedFileArray = new File[savedFiles.size()];
            for(int m = 0, n = savedFileArray.length; m < n; m++)
                savedFileArray[m] = savedFiles.get(m);
            return savedFileArray;
        } catch(FileUploadBase.SizeLimitExceededException se) {
            throw new RuntimeException("上傳檔案大小(" + se.getActualSize() + " bytes)超過限制(" + se.getPermittedSize() + " bytes)");
        } catch(Throwable t) {
            log.error(t.toString());
            throw new RuntimeException(t.getMessage(), t);
        }
    }
}
