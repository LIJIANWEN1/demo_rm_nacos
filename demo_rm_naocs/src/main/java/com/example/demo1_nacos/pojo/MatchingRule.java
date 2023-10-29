package com.example.demo1_nacos.pojo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 9:13
 */
@Data
@TableName(autoResultMap = true)
public class MatchingRule {

    @TableId("id")
    private String id;

    @TableField("old_field")
    private String oldField;

    @TableField("new_field")
    private String newField;

    @TableField("description")
    private String description;

    @TableField("code")
    private String code;

    @TableField("type")
    private String type;

    @TableField("new_code")
    private String newCode;
}
