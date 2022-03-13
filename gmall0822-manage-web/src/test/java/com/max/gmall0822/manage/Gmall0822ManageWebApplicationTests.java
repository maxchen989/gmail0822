package com.max.gmall0822.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class Gmall0822ManageWebApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void uploadFile() throws IOException, MyException {
        //1 獲取配置
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);//將文件訊息加載至內存中
        //客戶端連tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        //ClientGlobal 想連 StorageClient 但她不知道在哪, 所以交給trackerServe處理
        StorageClient storageClient = new StorageClient(trackerServer, null);

        String[] upload_file = storageClient.upload_file("d://VMwareShare/cat.jpg", "jpg", null);
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println(s);
        }
    }
}
