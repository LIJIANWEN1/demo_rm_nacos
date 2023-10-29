package com.example.demo1_nacos.controller;

import cn.amberdata.afc.api.id.AfID;
import cn.amberdata.afc.api.object.AfPersistentObject;
import cn.amberdata.afc.api.operation.AfBulk;
import cn.amberdata.afc.api.session.IAfSession;
import cn.amberdata.afc.common.exception.AfException;
import cn.amberdata.dm.common.context.session.SessionContext;
import cn.amberdata.dm.common.permit.FolderPermitHandler;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.dm.organization.unit.Unit;
import cn.amberdata.dm.organization.unit.UnitDO;
import cn.amberdata.dm.organization.unit.UnitRepository;
import cn.amberdata.dm.organization.unit.mapper.UnitMapper;
import cn.amberdata.dm.session.SessionUtil;
import cn.amberdata.dm.sysobject.ObjectName;
import cn.amberdata.rm.archive.AbstractArchive;
import cn.amberdata.rm.archive.AbstractArchiveRepository;
import cn.amberdata.rm.archive.record.Record;
import cn.amberdata.rm.archive.record.RecordRepository;
import cn.amberdata.rm.classification.Category;
import cn.amberdata.rm.classification.CategoryRepository;
import cn.amberdata.rm.common.domain.TypeClassConstant;
import cn.amberdata.rm.common.domain.UnitFolderConstants;
import cn.amberdata.rm.common.exception.ExceptionCode;
import cn.amberdata.rm.common.log.LogUtil;
import cn.amberdata.rm.common.util.excel.sax.ExcelUtils;
import cn.amberdata.rm.common.util.excel.sax.NoModelDataListener;
import cn.amberdata.rm.fondvolume.FondVolumeFolder;
import cn.amberdata.rm.fondvolume.FondVolumeFolderDO;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumn;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumnRepository;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1_nacos.mapper.tdr.AbstractArchiveMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Description:
 * <p>
 *
 * @author Created by juxt
 * @since 2022/8/3 21:47
 */
@Api(tags = "初始化接口")
@RestController
@RequestMapping("/init")
public class InitController {

    @Resource
    private UnitMapper unitMapper;

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private RecordRepository recordRepository;

    @Resource
    private MetadataColumnRepository metadataColumnRepository;

    @Resource
    private AbstractArchiveMapper archiveMapper;

    @Resource
    private UnitRepository unitRepository;

    @Resource
    private AbstractArchiveRepository abstractArchiveRepository;

    @Resource
    private CategoryRepository categoryRepository;

    @ApiOperation(value = "修复形成日期", notes = "基于系统字段形成日期，生成新的业务形成日期（八位字符串类型）,提供兰溪tdr使用。")
    @GetMapping("/fix_business_doc_date")
    public void fixBusinessDocDate(@RequestParam String unitCode, @RequestParam String categoryId) {
        IAfSession afSession = SessionUtil.getAdminSession().getAfSession();
        SessionContext.setSession(SessionUtil.getAdminSession());

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        //查询该单位下fix_doc_data_state字段为空的案件和卷内，修复完会将该字段赋值，保证程序中断掉可以继续修复。
        queryWrapper.eq("archive_type", TypeClassConstant.RECORD_TYPE).eq("da_category_id", categoryId).eq("unit_code", unitCode).isNull("fix_doc_data_state");
        Page page = new Page<>();
        page.setCurrent(1);
        page.setSize(500);
        LogUtil.info("fix_business_doc_date 开始  ------>  单页五百条进行。");

        while (true) {
            List<String> archiveIdList = archiveMapper.findByWrapper(queryWrapper, page);
            if (CollectionUtils.isEmpty(archiveIdList)) {
                LogUtil.info("查询不到未修复的档案数据，程序退出");
                break;
            }
            for (String id : archiveIdList) {
                try {
                    LogUtil.info(String.format("该档案id --------> %s  开始修复", id));
                    AfPersistentObject object = new AfPersistentObject(new AfID(id), afSession);
                    //根据旧的形成日期生成新的业务形成日期
                    String oldDocDate = object.getString("doc_date");
//                    if (Objects.nonNull(oldDocDate)) {
//                        businessDocDate = format.format(oldDocDate);
//                    }
                    //填充待add到json中的数据
                    Map<String, String> map = new HashMap<>();
                    map.put("name", "xcsj");
                    map.put("title", "档案形成时间");
                    map.put("content", oldDocDate);

                    //填充到json中
                    String metadataJson = object.getString("attr_metadata_json");
                    DocumentContext documentContext = JsonPath.parse(metadataJson);
                    documentContext.add("$..block[?(@.name == '业务信息')].property", map);

                    //更新档案信息
                    object.setString("xcsj", oldDocDate);
                    object.setString("attr_metadata_json", documentContext.jsonString());
                    //标记该档案已经修复
                    object.setString("fix_doc_data_state", "1");
                    object.save();
                    LogUtil.info(String.format("该档案id --------> %s  修复完成", id));
                } catch (Exception e) {
                    LogUtil.error(String.format("该档案id【%s】修复失败。", id), e);
                }
            }
            page.setCurrent(page.getCurrent() + 1);
        }
    }



    @ApiOperation(value = "初始化单个单位全宗卷9大类文件夹")
    @GetMapping("/init_fond_volume_type_folder")
    @AfBulk
    public void initFondVolumeTypeFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unit.getId());
        String unitInfo = "/" + unit.getDisplayName() + "-" + unit.getCode();
        List<FondVolumeFolderDO> folderInfoList = new ArrayList<>();
        buildParam(folderInfoList, unitInfo);
        folderInfoList.forEach(info -> {
            String path = info.getPath();
            String classNumber = info.getClassNumber();
            Folder folder = folderRepository.findByPath(path);
            if (folder == null) {
                String name = path.substring(path.lastIndexOf("/") + 1);
                createFondVolumeFolder(name, classNumber, unitInfo + "/RM.档案管理/全宗卷", folderPermitHandler);
            }
        });
    }


    /**
     * 构造九大类文件夹信息
     */
    private void buildParam(List<FondVolumeFolderDO> folderInfoList, String unitInfo) {
        FondVolumeFolderDO folderDO1 = new FondVolumeFolderDO();
        folderDO1.setClassNumber("QZJ.1");
        folderDO1.setPath(unitInfo + "/RM.档案管理/全宗卷/全宗介绍类");
        FondVolumeFolderDO folderDO2 = new FondVolumeFolderDO();
        folderDO2.setClassNumber("QZJ.2");
        folderDO2.setPath(unitInfo + "/RM.档案管理/全宗卷/档案收集类");
        FondVolumeFolderDO folderDO3 = new FondVolumeFolderDO();
        folderDO3.setClassNumber("QZJ.3");
        folderDO3.setPath(unitInfo + "/RM.档案管理/全宗卷/档案整理类");
        FondVolumeFolderDO folderDO4 = new FondVolumeFolderDO();
        folderDO4.setClassNumber("QZJ.4");
        folderDO4.setPath(unitInfo + "/RM.档案管理/全宗卷/档案鉴定类");
        FondVolumeFolderDO folderDO5 = new FondVolumeFolderDO();
        folderDO5.setClassNumber("QZJ.5");
        folderDO5.setPath(unitInfo + "/RM.档案管理/全宗卷/档案保管类");
        FondVolumeFolderDO folderDO6 = new FondVolumeFolderDO();
        folderDO6.setClassNumber("QZJ.6");
        folderDO6.setPath(unitInfo + "/RM.档案管理/全宗卷/档案统计类");
        FondVolumeFolderDO folderDO7 = new FondVolumeFolderDO();
        folderDO7.setClassNumber("QZJ.7");
        folderDO7.setPath(unitInfo + "/RM.档案管理/全宗卷/档案利用类");
        FondVolumeFolderDO folderDO8 = new FondVolumeFolderDO();
        folderDO8.setClassNumber("QZJ.8");
        folderDO8.setPath(unitInfo + "/RM.档案管理/全宗卷/新技术应用类");
        FondVolumeFolderDO folderDO9 = new FondVolumeFolderDO();
        folderDO9.setClassNumber("QZJ.9");
        folderDO9.setPath(unitInfo + "/RM.档案管理/全宗卷/其他");
        folderInfoList.add(folderDO1);
        folderInfoList.add(folderDO2);
        folderInfoList.add(folderDO3);
        folderInfoList.add(folderDO4);
        folderInfoList.add(folderDO5);
        folderInfoList.add(folderDO6);
        folderInfoList.add(folderDO7);
        folderInfoList.add(folderDO8);
        folderInfoList.add(folderDO9);
    }

    /**
     * 初始化全宗卷九大类文件夹
     */
    private void createFondVolumeFolder(String name, String classNumber, String parentPath, FolderPermitHandler folderPermitHandler) {
        FondVolumeFolder fondVolumeFolder = new FondVolumeFolder(SessionContext.getSession(), name, classNumber);
        if (org.apache.commons.lang.StringUtils.isNotBlank(parentPath)) {
            fondVolumeFolder.link(parentPath);
        }
        fondVolumeFolder.save();
        folderPermitHandler.applyPermit(fondVolumeFolder);
    }


    @ApiOperation(value = "初始化单个单位全宗卷文件夹")
    @GetMapping("/init_one_fond_volume_folder")
    @AfBulk
    public void initOneFondVolumeFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        String unitInfo = "/" + unit.getDisplayName() + "-" + unit.getCode();
        String folderPath = unitInfo + "/RM.档案管理/全宗卷";
        Folder folder = folderRepository.findByPath(folderPath);
        if (folder == null) {
            createFolderAndGrant("全宗卷", unitInfo + "/RM.档案管理", unit.getId());
            LogUtil.info(String.format("[%s-%s]初始化全宗卷成功", unit.getDisplayName(), unit.getCode()));
        }
    }

    /**
     * 创建文件夹
     *
     * @param name       名称
     * @param parentPath 父路径
     * @return 创建出来的文件夹路径
     */
    private String createFolderAndGrant(String name, String parentPath, String unitId) {
        Folder folder = new Folder(new ObjectName(name));
        if (org.apache.commons.lang.StringUtils.isNotBlank(parentPath)) {
            folder.link(parentPath);
        }
        folderRepository.store(folder);
        FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unitId);
        folderPermitHandler.applyPermit(folder);
        return folder.getObjectPath();
    }



    @ApiOperation(value = "初始化单个单位编研记录和编研成果文件夹")
    @GetMapping("/init_one_compiling_folder")
    public void initOneCompilingFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unit.getId());
        try {
            String rmPath = "/" + unit.getDisplayName() + "-" + unit.getCode() + "/RM.档案管理";
            String achievementsPath = rmPath + "/编研成果";
            Folder achievementsFolder = folderRepository.findByPath(achievementsPath);
            if (Objects.isNull(achievementsFolder)) {
                LogUtil.info(String.format("创建编研成果文件夹：【%s】", achievementsPath));
                //创建文件夹
                Folder newFolder = new Folder(new ObjectName(UnitFolderConstants.COMPILING_ACHIEVEMENTS));
                LogUtil.info(String.format("链接父路径：【%s】", rmPath));
                newFolder.link(rmPath);
                folderRepository.store(newFolder);
                LogUtil.info(String.format("文件夹赋权：【%s】", achievementsPath));
                folderPermitHandler.applyPermit(newFolder);
            }
            String recordPath = rmPath + "/编研记录";
            Folder recordFolder = folderRepository.findByPath(recordPath);
            if (Objects.isNull(recordFolder)) {
                LogUtil.info(String.format("创建编研成果文件夹：【%s】", recordPath));
                //创建文件夹
                Folder newFolder = new Folder(new ObjectName(UnitFolderConstants.COMPILING_TASK));
                LogUtil.info(String.format("链接父路径：【%s】", rmPath));
                newFolder.link(rmPath);
                folderRepository.store(newFolder);
                LogUtil.info(String.format("文件夹赋权：【%s】", recordPath));
                folderPermitHandler.applyPermit(newFolder);
            }
        } catch (AfException e) {
            LogUtil.error(String.format("单位【%s】初始化资料库文件夹失败，失败原因:%s", unit.getDisplayName() + "-" + unit.getCode(), e.getMessage()));
        }
    }

    @ApiOperation(value = "初始化单个单位资料库文件夹")
    @GetMapping("/init_one_database_folder")
    public void initOneDatabaseFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        try {
            String dmPath = "/" + unit.getDisplayName() + "-" + unit.getCode() + "/DM.文件管理";
            String databaseFolderPath = dmPath + "/资料库";
            Folder databaseFolder = folderRepository.findByPath(databaseFolderPath);
            if (Objects.isNull(databaseFolder)) {
                LogUtil.info(String.format("创建资料库文件夹：【%s】", databaseFolderPath));
                //创建文件夹
                Folder newFolder = new Folder(new ObjectName(UnitFolderConstants.DATABASE_FOLDER));
                LogUtil.info(String.format("链接父路径：【%s】", dmPath));
                newFolder.link(dmPath);
                folderRepository.store(newFolder);
                FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unit.getId());
                LogUtil.info(String.format("文件夹赋权：【%s】", databaseFolderPath));
                folderPermitHandler.applyPermit(newFolder);
            } else {
                FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unit.getId());
                LogUtil.info(String.format("文件夹赋权：【%s】", databaseFolderPath));
                folderPermitHandler.applyPermit(databaseFolder);
            }
        } catch (AfException e) {
            LogUtil.error(String.format("单位【%s】初始化资料库文件夹失败，失败原因:%s", unit.getDisplayName() + "-" + unit.getCode(), e.getMessage()));
        }
    }

    @ApiOperation(value = "初始化所有单位的资料库文件夹")
    @GetMapping("/init_all_unit_database_folder")
    public void initAllUnitDatabaseFolder() {
        SessionContext.setSession(SessionUtil.getAdminSession());
        int current = 1;
        while (true) {
            List<UnitDO> unitDOS = unitMapper.list(new Page<>(current, 200));
            if (CollectionUtils.isEmpty(unitDOS)) {
                break;
            }
            for (UnitDO unitDO : unitDOS) {
                try {
                    String dmPath = "/" + unitDO.getDisplayName() + "-" + unitDO.getCode() + "/DM.文件管理";
                    String databaseFolderPath = dmPath + "/资料库";
                    Folder databaseFolder = folderRepository.findByPath(databaseFolderPath);
                    if (Objects.isNull(databaseFolder)) {
                        LogUtil.info(String.format("创建资料库文件夹：【%s】", databaseFolderPath));
                        //创建文件夹
                        Folder newFolder = new Folder(new ObjectName(UnitFolderConstants.DATABASE_FOLDER));
                        LogUtil.info(String.format("链接父路径：【%s】", dmPath));
                        newFolder.link(dmPath);
                        folderRepository.store(newFolder);
                        FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unitDO.getId());
                        LogUtil.info(String.format("文件夹赋权：【%s】", databaseFolderPath));
                        folderPermitHandler.applyPermit(newFolder);
                    } else {
                        FolderPermitHandler folderPermitHandler = new FolderPermitHandler(unitDO.getId());
                        LogUtil.info(String.format("文件夹赋权：【%s】", databaseFolderPath));
                        folderPermitHandler.applyPermit(databaseFolder);
                    }
                } catch (AfException e) {
                    LogUtil.error(String.format("单位【%s】初始化资料库文件夹失败，失败原因:%s", unitDO.getDisplayName() + "-" + unitDO.getCode(), e.getMessage()));
                }
            }
            current++;
        }
    }

    @ApiOperation(value = "初始化单个单位进馆接收文件夹")
    @GetMapping("/init_one_register_folder")
    @AfBulk
    public void initOneRegisterFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        String unitInfo = "/" + unit.getDisplayName() + "-" + unit.getCode();
        String folderPath = unitInfo + "/RM.档案管理/进馆接收";
        Folder folder = folderRepository.findByPath(folderPath);
        if (folder == null) {
            createFolderAndGrant("进馆接收", unitInfo + "/RM.档案管理", unit.getId());
            LogUtil.info(String.format("[%s-%s]初始化进馆接收成功", unit.getDisplayName(), unit.getCode()));
        }
    }

    @ApiOperation(value = "初始化纠错记录附件文件夹")
    @GetMapping("init_error_correction_annex_folder")
    public void initErrorCorrectionAnnexFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        Unit unit = unitRepository.findByCode(unitCode);
        //父路径
        String parentPath = "/" + unit.getDisplayName() + "-" + unit.getCode() + "/RM.档案管理";
        //纠错记录附件路径
        String annexFolderPath = parentPath + "/" + UnitFolderConstants.ERROR_CORRECTION_ANNEX_FOLDER;
        Folder annexFolder = folderRepository.findByPath(annexFolderPath);
        if (Objects.isNull(annexFolder)) {
            LogUtil.info(String.format("初始化纠错记录附件文件夹【%s】", annexFolderPath));
            //创建纠错记录附件文件夹
            createFolderAndGrant(UnitFolderConstants.ERROR_CORRECTION_ANNEX_FOLDER, parentPath, unit.getObjectId().getId());
        }
    }

    @ApiOperation(value = "初始化单个单位密级处置批次文件夹")
    @GetMapping("/init_one_security_dispose_folder")
    @AfBulk
    public void initOneSecurityDisposeFolder(@RequestParam String unitCode) {
        SessionContext.setSession(SessionUtil.getAdminSession());
        UnitDO unit = unitMapper.findByCode(unitCode);
        String unitInfo = "/" + unit.getDisplayName() + "-" + unit.getCode();
        String folderPath = unitInfo + "/RM.档案管理/表单/密级处置批次";
        Folder folder = folderRepository.findByPath(folderPath);
        if (folder == null) {
            createFolderAndGrant("密级处置批次", unitInfo + "/RM.档案管理/表单", unit.getId());
            LogUtil.info(String.format("[%s-%s]初始化文件夹密级处置批次成功", unit.getDisplayName(), unit.getCode()));
        }
    }
}
