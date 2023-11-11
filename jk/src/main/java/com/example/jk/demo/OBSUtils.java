package com.example.jk.demo;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
public class OBSUtils {
    @Resource
    private ObsBucketServiceImpl obsBucketService;

    /**
     * 操作桶
     */
    @GetMapping("/operation_bucket")
    public void create(@RequestParam String operation,@RequestParam String bucketName,Integer n) {
        ObsClient obsClient = null;
        try{
            obsClient  = obsBucketService.newObsClient();
            switch (operation)
            {
                case "create":
                    //创建桶
                    obsBucketService.createObsBucket(bucketName);
                    break;
                case "get_location":
                    //获取桶位置信息
                    obsBucketService.getObsBucketLocation(bucketName);
                    break;
                case "get_storage_info":
                    //获取桶存储信息
                    obsBucketService.getObsBucketStorageInfo(bucketName);
                    break;
                case "set_storage":
                    //设置存储大小
                    obsBucketService.doBucketQuotaOperation(bucketName,n);
                    break;
                case "set_acl":
                    //设置权限公共读写
                    obsBucketService.doObsBucketAclOperation(bucketName);
                    break;
                case "set_cors":
                    //设置桶的访问权限
                    obsBucketService.doObsBucketCorsOperation(bucketName);
                    break;
                case "delete":
                    //删除桶
                    obsBucketService.deleteObsBucket(bucketName);
                    break;
                default:
                    break;
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