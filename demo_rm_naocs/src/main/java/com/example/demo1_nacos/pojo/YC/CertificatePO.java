package com.example.demo1_nacos.pojo.YC;
import lombok.Data;

import java.util.List;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 19:21
 */
@Data
public class CertificatePO {

    private Integer certificateId;

    private String certificateWord;

    private String certificateDate;

    private String certificateCode;

    private String documentMaker;

    private String accountCode;

    private List<OrderInfo> orderInfoList;


}
