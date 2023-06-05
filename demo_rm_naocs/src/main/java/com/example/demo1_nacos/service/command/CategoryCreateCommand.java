package com.example.demo1_nacos.service.command;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description: 门类创建命令
 *
 * @author lyf
 * @since 2022/1/12 14:19
 */
@Data
public class CategoryCreateCommand {

    @ApiModelProperty(value = "排序号", name = "sort")
    private Integer sort;

    @ApiModelProperty(value = "元数据门类id", name = "idFromMetadata")
    private String idFromMetadata;

    @ApiModelProperty(value = "整理方式  按件整理,按卷整理", name = "collectionWay")
    private String collectionWay;

    @ApiModelProperty(value = "绑定的库房编号", name = "warehouseNo")
    private String warehouseNo;

    @ApiModelProperty(value = "", name = "archivePath",required = true)
    private String archivePath;

    @ApiModelProperty(value = "", name = "archivePath",required = true)
    private String unitId;

    @ApiModelProperty(value = "", name = "archivePath",required = true)
    private String retentionPolicyPath;

    @ApiModelProperty(value = "", name = "archivePath",required = true)
    private String code;
}
