package com.example.demo1_nacos.service;
import cn.amberdata.afc.api.object.IAfPersistentObject;
import cn.amberdata.afc.api.object.MetadataDataCacheManager;
import cn.amberdata.afc.common.exception.AfErrorCodeEnum;
import cn.amberdata.afc.common.exception.AfNotSupportNodeTypeException;
import cn.amberdata.afc.common.util.DataTypeCalibration;
import cn.amberdata.afc.repository.RepositoryConfig;
import cn.amberdata.dm.common.context.session.SessionContext;
import cn.amberdata.dm.folder.Folder;
import cn.amberdata.dm.folder.FolderRepository;
import cn.amberdata.dm.session.SessionUtil;
import cn.amberdata.dm.user.User;
import cn.amberdata.dm.user.UserRepository;
import cn.amberdata.metadata.facade.dto.PropertyDTO;
import cn.amberdata.rm.archive.AbstractArchive;
import cn.amberdata.rm.archive.record.Record;
import cn.amberdata.rm.archive.record.RecordRepository;
import cn.amberdata.rm.archive.record.mapper.RecordMapper;
import cn.amberdata.rm.archive.valueobject.CollectionWay;
import cn.amberdata.rm.archive.volume.Volume;
import cn.amberdata.rm.archive.volume.VolumeRepository;
import cn.amberdata.rm.archive.volume.mapper.VolumeMapper;
import cn.amberdata.rm.archive.volume.valueobject.BindingMethodStatus;
import cn.amberdata.rm.classification.Category;
import cn.amberdata.rm.classification.CategoryRepository;
import cn.amberdata.rm.classification.SubCategory;
import cn.amberdata.rm.classification.SubCategoryRepository;
import cn.amberdata.rm.common.log.LogUtil;
import cn.amberdata.rm.common.util.importdata.SelectUtil;
import cn.amberdata.rm.metadata.MetadataService;
import cn.amberdata.rm.metadata.info.MetadataSchemeInfoRepository;
import cn.amberdata.rm.metadata.itemcode.MetadataCodeItemRepository;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumn;
import cn.amberdata.rm.metadata.metadatacolumn.MetadataColumnRepository;
import cn.amberdata.rm.metadata.template.TemplateMetadata;
import cn.amberdata.rm.metadata.template.TemplateMetadataRepository;
import cn.amberdata.rm.settings.strategy.RetentionStrategyRepository;
import com.example.demo1_nacos.dao.DocumentDao;
import com.example.demo1_nacos.vo.ImportArchivePackageVO;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/16 17:50
 */
@Service
public class BusinessFormServiceImpl {

}
