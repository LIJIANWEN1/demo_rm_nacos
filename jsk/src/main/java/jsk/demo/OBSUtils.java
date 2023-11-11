package jsk.demo;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class OBSUtils {
    @Resource
    private com.example.jk.demo.ObsBucketServiceImpl obsBucketService;

    /**
     * 操作桶
     */
    @PostMapping("/operation_bucket")
    public void create(@RequestParam String operation,Integer n) {
        String bucketName ="";
        ObsClient obsClient = null;
        try{
            obsClient  = obsBucketService.newObsClient();
            switch (operation)
            {
                case "create":
                    //创建桶
                    obsBucketService.createObsBucket(bucketName);
                case "get_location":
                    //获取桶位置信息
                    obsBucketService.getObsBucketLocation(bucketName);
                case "get_storage_info":
                    //获取桶存储信息
                    obsBucketService.getObsBucketStorageInfo(bucketName);
                case "set_storage":
                    //设置存储大小
                    obsBucketService.doBucketQuotaOperation(bucketName,n);
                case "set_acl":
                    //设置权限公共读写
                    obsBucketService.doObsBucketAclOperation(bucketName);
                case "set_cors":
                    //设置桶的访问权限
                    obsBucketService.doObsBucketCorsOperation(bucketName);
                case "delete":
                    //删除桶
                    obsBucketService.deleteObsBucket(bucketName);
            }
        } catch (ObsException e)
        {
            System.out.println("Response Code: " + e.getResponseCode());
            System.out.println("Error Message: " + e.getErrorMessage());
            System.out.println("Error Code:       " + e.getErrorCode());
            System.out.println("Request ID:      " + e.getErrorRequestId());
            System.out.println("Host ID:           " + e.getErrorHostId());
        }
        finally
        {
            if (obsClient != null)
            {
                try
                {
                    /*
                     * Close obs client
                     */
                    obsClient.close();
                }
                catch (IOException e)
                {
                }
            }
        }

    }
}