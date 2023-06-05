package com.example.demo1_nacos.pojo;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author leanderli
 * @see
 * @since 2020.08.18
 */
@Data
public class Document {

    public static final String LOCAL_PATH = "local_path";

    public static final String ITEM_FLAG = "item_flag";

    public static final String HANDLE_RESULT = "handle_result";

    /**
     * 主键
     */
    private String id;
    /**
     * 对应原文的item_flag标识
     */
    private String itemFlag;
    private List<String> localPath;
    private String linkPath;
    private int handleResult;
    private File file;

    public Document(Map<String, Object> map) {
        if (MapUtils.isNotEmpty(map)) {
            this.id = (String) map.get("id");
            this.itemFlag = String.valueOf(map.get(ITEM_FLAG));
            localPath = new GsonBuilder().create().fromJson((String) map.get(LOCAL_PATH), new TypeToken<List<String>>() {
            }.getType());
            if (null != map.get(HANDLE_RESULT)) {
                this.handleResult = (int) map.get(HANDLE_RESULT);
            }
        }
    }

    public Document() {

    }
}
