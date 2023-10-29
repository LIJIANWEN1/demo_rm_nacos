package com.example.demo1_nacos.service;
import cn.amberdata.rm.classification.mapper.SubCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/9 9:02
 */
//@Slf4j
@Service
public class MatchingRuleServiceImpl {
    @Resource
    private SubCategoryMapper subCategoryMapper;

//    @Value("${cn.amberdata.pagesize}")
//    private Integer pageSize;

    //    @SofaReference(uniqueId = "metadataColumnConfigFacade", binding = @SofaReferenceBinding(bindingType = "bolt"))
//    private MetadataColumnConfigFacade metadataColumnConfigFacade;
//
//    @SofaReference(uniqueId = "metadataSchemeFacade", binding = @SofaReferenceBinding(bindingType = "bolt"))
//    private MetadataSchemeFacade metadataSchemeFacade;
    @Resource
    private cn.amberdata.rm.classification.SubCategoryRepository subCategoryRepository;

    public void convertDate(String oldCode) {
//        //修改类目编号
//        SessionContext.setSession(SessionUtil.getAdminSession());
//        List<SubCategoryDO> treeByParentPath = subCategoryMapper.getTreeByParentPath("/富阳区档案馆/%");
//        for (int i = 0; i < treeByParentPath.size(); i++) {
//            SubCategory subCategory = subCategoryRepository.find(treeByParentPath.get(i).getId());
////            if(subCategory.getName().contains("年")||subCategory.getName().equals("永久")){
//            if(subCategory.getName().contains("初始类目")){
//                SubCategory subCategory1 = subCategoryRepository.find(subCategory.getParentId());
//                subCategory.updateClassificationCodePrefix(subCategory1.getCode());
//                subCategoryRepository.store(subCategory);
//                System.out.println("修改成功");
//            }
//        }
//        System.out.println("----------------");
//        QueryWrapper<MatchingRule> wrapper = new QueryWrapper<>();
//        wrapper.lambda().eq(MatchingRule::getCode, oldCode);
//        List<MatchingRule> matchingRules = matchingRuleMapper.findByWrapper(wrapper);
//        if (CollectionUtils.isEmpty(matchingRules)) {
//            log.info("------没有查到匹配规则 MatchingRule--------");
//        }
//        List<String> collect = matchingRules.stream().map(MatchingRule::getOldField).collect(Collectors.toList());
//        StringBuffer stringBuffer = new StringBuffer(" ");
//        for (int i = 0; i < collect.size(); i++) {
//            String column = collect.get(i);
//            stringBuffer.append(column);
//            if (i < collect.size() - 1) {
//                stringBuffer.append(",");
//            }
//        }
//        Map<String, Object> qzhMap = new HashMap<>();
//        qzhMap.put("qzh", "0056");
//        long count = collecter.count(oldCode, qzhMap);
//        log.info("总量：" + count);
//        long pages = pages(count, pageSize);
//        log.info("总分页：" + pages + "页");
//        String categoryCode = matchingRules.get(0).getNewCode();
//        String path = "unitName" + "/" + UnitFolderConstants.TOP_RM_FOLDER_PATH + "/" + UnitFolderConstants.ARCHIVES_LIBRARY;
//        String categoryId = categoryService.getCategoryIdByCodeAndParentPath(categoryCode, path);
//        CategoryDTO categoryDTO = categoryService.categoryDetails(categoryId);
//        Category category = categoryRepository.find(categoryId);
//        String metadataSchemeId;
//        if (categoryDTO.getCollectionWay().equals(CollectionWay.COLLECTION_WAY_RECORD.asValue())) {
//            metadataSchemeId = categoryDTO.getFileSchemeId();
//        } else {
//            metadataSchemeId = categoryDTO.getVolumeSchemeId();
//        }
//        MetadataSchemeInfo metadataSchemeInfo = metadataSchemeInfoRepository.getMetadataSchemeInfoByIdOrCode(metadataSchemeId, null);
//        TemplateMetadata templateMetadata = templateMetadataRepository.findTemplateByMetadataSchemeIdAndVersionNo(metadataSchemeInfo.getId(), null);
//        String jsonMetadataTemplate = templateMetadataRepository.getJsonTemplateById(templateMetadata.getId());
//        List<MetadataColumn> columns = metadataColumnRepository.getMetadataColumnByMetadataSchemeId(metadataSchemeInfo.getId());
//        DocumentContext documentContext = JsonPath.parse(jsonMetadataTemplate);
//        CountDownLatch latch = new CountDownLatch(Integer.parseInt(pages+""));
//        ThreadPoolUtil.createThread();
//        collecter.pageQuery(pageSize, stringBuffer.toString(), oldCode, qzhMap, list -> {
//            for (Map<String, Object> map : list) {
//                archiveServiceImpl.saveJsonData(documentContext, map, columns);
//                Record record = new Record(SessionContext.getSession(), metadataSchemeInfo.getTypeName(), categoryDTO.getId(), categoryDTO.getCollectionWay(), metadataSchemeId, Long.valueOf(templateMetadata.getVersionNo()), categoryCode, categoryId, "", documentContext.toString(), category);
//                String fileYear = record.getFileYear();
//                SubCategory subCategory = subCategoryRepository.findSubCategoryByNameAndParentId(fileYear + "年", categoryId);
//                if (null == subCategory) {
//                    throw new RuntimeException("分类中没有该年度：" + fileYear);
//                }
//                //导入到档案库做快速著录行为
//                User user = UserContext.getObject();
////                record.description(sortNumber, user.getObjectId().getId(), user.getUserLoginName(), volumeArchivesNum, UnitContext.getObject().getCode());
//                //指定分类
//                record.updateClassification(subCategory.getClassificationCode(), subCategory.getObjectId().getId());
//                //快速著录行为
//                record.descriptionFast(retentionStrategyRepository.find(subCategory.getRetentionPolicyId()));
//                //覆盖更新json表单数据
//                record.updateJsonForm(null, columns);
//                //持久化
//                recordRepository.store(record);
//
//            }
////            ThreadPoolUtil.runTask(() -> {
////                f
//////                List<DocumentsModel> models = toModels(list);
//////                updater.updateEsData(models);
////                list.clear();
////                latch.countDown();
////            });
//        });
//        MetadataSchemeInfoDTO metadataSchemeByCode = metadataSchemeFacade.getMetadataSchemeByCode(newCode);
//        System.out.println(matchingRules);
    }

    private long pages(long count, int pageSize) {
        return 0 == count % pageSize ? count / pageSize : count / pageSize + 1;
    }

    public static void main(String[] args) {

    }
}
