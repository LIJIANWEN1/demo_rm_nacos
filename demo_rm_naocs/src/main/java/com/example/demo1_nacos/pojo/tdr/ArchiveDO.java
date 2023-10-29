package com.example.demo1_nacos.pojo.tdr;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Description: 档案公共父对象
 *
 * @author Created by wangyf
 * @since 2022/01/07
 */
@Data
@TableName(autoResultMap = true)
public class ArchiveDO {
    
    
    @TableId("s_object_id")
    private String id;

    /**
     * 创建时间
     */
    @TableField("s_create_date")
    private Date createDate;

    /**
     * 整理方式
     */
    @TableField("collection_way")
    private String collectionWay;

    /**
     * 由于每种元数据方案对应一种s_object_type的档案，所以针对卷和件给出一个类型区分，暂定为【da_record 和 da_volume】
     */
    @TableField("archive_type")
    private String archiveType;

    /**
     * 档案门类编码
     */
    @TableField("category_code")
    private String categoryCode;

    /**
     * 一级门类编码
     */
    @TableField("archive_1st_category")
    private String firstCategoryCode;

    /**
     * 题名
     */
    @TableField("title")
    private String title;

    /**
     * 文件容量
     */
    @TableField("file_capacity")
    private Double filesCapacity;


    /**
     * 单位组织结构编码
     */
    @TableField("unit_code")
    private String unitCode;

    /**
     * 单位组织结构编码
     */
    private String unitName;

    /**
     * 是否已归档
     */
    @TableField("whether_filed")
    private Boolean isFiled;

    /**
     * 排序号
     */
    @TableField("sort_number")
    private Long sortNumber;
    /**
     * 流水号
     */
    @TableField("serial_number")
    private Long serialNumber;


    /**
     * 载体类型  "01" 实体  "02" 电子
     */
    @TableField("carrier_type")
    private String carrierTypeStatus;

    /**
     * 归档时间
     */
    @TableField("filed_date")
    private Date filedDate;

    /**
     * 档号
     */
    @TableField("archival_id")
    private String archivalId;

    /**
     * 年度
     */
    @TableField("file_year")
    private String fileYear;

    /**
     * 形成日期
     */
    @TableField("doc_date")
    private Date docDate;

    /**
     * 密级
     */
    @TableField("security_class")
    private String securityClass;

    /**
     * 开放等级
     */
    @TableField("open_class")
    private String openClass;

    /**
     * 保管期限
     */
    @TableField("retention_period")
    private String retentionPeriod;

    /**
     * 发布网段
     */
    @TableField("released_network")
    private String releasedNetwork;

    /**
     * 全宗号
     */
    @TableField("fonds_id")
    private String fondsId;

    /**
     * 全宗名称
     */
    @TableField("fonds_name")
    private String fondsName;

    /**
     * 是否已分类
     */
    @TableField("whether_classified")
    private Boolean whetherClassified;

    /**
     * 分类号
     */
    @TableField("classification")
    private String classification;


    /**
     * 备注
     */
    @TableField("da_remark")
    private String remark;

    /**
     * 责任者
     */
    @TableField("author")
    private String author;

    /**
     * 文号
     */
    @TableField("article_number")
    private String articleNumber;

    /**
     * 是否存在文件
     */
    @TableField("exist_document")
    private Boolean existDocument;

    /**
     * 门类id
     */
    @TableField("da_category_id")
    protected String categoryId;

    @TableField("s_object_path")
    private String path;

    // 总的数量
    @TableField("count")
    private Integer count;

    @TableField("category_name")
    private String categoryName;
}
