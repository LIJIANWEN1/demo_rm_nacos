package com.example.demo1_nacos.dao;

import cn.amberdata.common.response.exception.BusinessException;
import cn.amberdata.dm.document.DocumentRepository;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.dm.sysobject.ObjectName;
import cn.amberdata.dm.sysobject.SysObjectDO;
import cn.amberdata.dm.sysobject.mapper.SysObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo1_nacos.pojo.Document;
import com.example.demo1_nacos.service.BusinessForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @Author: zhaohuaxia
 * @DateTime: 2021/10/16 11:09
 * @Description:
 */
@Repository
public class DocumentDaoImpl implements DocumentDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDaoImpl.class);

    @Resource
    private SysObjectMapper sysObjectMapper;

    @Resource
    private FolderRepository folderRepository;

    @Resource
    private DocumentRepository documentRepository;

    @Value("${cn.amberdata.get-business-forms}")
    private String getBusinessFormsStr;

    @Override
    public void save(List<Document> documents, String saveMode) {
        cn.amberdata.dm.document.Document fileDocument;
        for (Document document : documents) {
            Folder parentArchive = folderRepository.findByPath(document.getLinkPath());
            if (null == parentArchive) {
                throw new RuntimeException(String.format("文件保存失败，没有找到父文件夹，document对象=【%s】", document.toString()));
            }
            switch (saveMode) {
                case DocumentDao.MODE_REIMPORT_SKIP:
                    fileDocument = documentRepository.findByPath(parentArchive.getObjectPath() + "/" + document.getFile().getName());
                    if (null == fileDocument) {
                        constructDocument(document);
                    }
                    break;
                case DocumentDao.MODE_REIMPORT_WITHOUT_SKIP:
                    fileDocument = documentRepository.findByPath(parentArchive.getObjectPath() + "/" + document.getFile().getName());
                    if (null != fileDocument) {
                        fileDocument.destroy();
                    }
                    constructDocument(document);
                    break;
                case DocumentDao.MODE_NORMAL:
                    constructDocument(document);
                default:
                    break;
            }
        }
    }

    private void constructDocument(Document document) {
        File localFile = document.getFile();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(localFile);
            cn.amberdata.dm.document.Document fileDocument = documentRepository.newEntity(new ObjectName(localFile.getName()), fileInputStream);
            fileDocument.link(document.getLinkPath());
            documentRepository.store(fileDocument);
        } catch (Exception e) {
            LOGGER.error("挂接原文失败", e);
            throw new BusinessException(e);
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException ioException) {
                    LOGGER.error("关闭流失败", ioException);
                }
            }
        }
    }

    @Override
    public BusinessForm getBusinessForms(String itemFlag, String unitCode) {
        QueryWrapper<SysObjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(getBusinessFormsStr, itemFlag).eq("archive_type", "da_record").eq("unit_code", unitCode);
        IPage<SysObjectDO> sysObjectDOIPage = sysObjectMapper.findByWrapper(queryWrapper, new Page<>(1, 1));
        if (sysObjectDOIPage.getRecords().size() == 0) {
            throw new RuntimeException("实例化业务表单异常，根据item flag没有找到条目，itemFlag[" + itemFlag + "]");
        }
        Folder archive = folderRepository.find(sysObjectDOIPage.getRecords().get(0).getId());
        BusinessForm object = new BusinessForm();
        object.setObjectId(archive.getString("s_object_id"));
        object.setObjectPath(archive.getString("s_object_path"));
        object.setJsonMetadata(archive.getString("attr_metadata_json"));
        object.setItemFlag(itemFlag);
        return object;
    }

}
