package com.example.demo1_nacos.service.command;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Description: 类目创建命令
 *
 * @author lyf
 * @since 2022/1/12 14:19
 */
@Data
public class SubCategoryCreateCommand {

    @ApiModelProperty(value = "父文件夹ID", name = "parentId")
    private String parentId;

    @ApiModelProperty(value = "类目名称", name = "name")
    private String name;

    @ApiModelProperty(value = "保留处置策略Id", name = "retentionPolicyId")
    private String retentionPolicyId;

    @ApiModelProperty(value = "保管期限Id", name = "retentionPolicyId")
    private String retentionPeriodId;

    @ApiModelProperty(value = "类目描述", name = "description")
    private String description;

    @ApiModelProperty(value = "是否同步, true表示需要同步", name = "dataSync")
    private Boolean dataSync;

    @ApiModelProperty(value = "记录类目的门类id", name = "categoryId")
    private String categoryId;

    @ApiModelProperty(value = "类目分类号前缀", name = "classificationCodePrefix")
    private String classificationCodePrefix;

    @ApiModelProperty(value = "类目编号", name = "classNumber")
    private String classNumber;

}
