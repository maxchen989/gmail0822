package com.max.gmall0822.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {

    @Value("${fileserver.url}")
    String fileServerUrl; //讀取application.properties的值

    @PostMapping("fileUpload")
    @CrossOrigin
    //返回值是路徑=>String
    public String fileUpLoad(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        //1 獲取配置
        String confPath = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(confPath);//將文件訊息加載至內存中
        //客戶端連tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        //ClientGlobal 想連 StorageClient 但她不知道在哪, 所以交給trackerServe處理
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //得到副檔名
        String originalFilename = file.getOriginalFilename();
        String extName = StringUtils.substringAfterLast(originalFilename, ".");

        //上傳檔案
        String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);

        //得到路徑
        String fileUrl = fileServerUrl;

        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            fileUrl += "/" + s;
        }
        return fileUrl;
    }
}
