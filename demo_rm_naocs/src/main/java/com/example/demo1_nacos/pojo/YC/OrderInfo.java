package com.example.demo1_nacos.pojo.YC;
import lombok.Data;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 19:25
 */
@Data
public class OrderInfo {

    private String orderCode;

    private String orderName;

    private String orderType;

    private Double submitAmount;

    private String department;

    private String submitorName;
}
