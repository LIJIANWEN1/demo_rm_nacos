package com.example.demo1_nacos.mapper.tdr;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.example.demo1_nacos.pojo.tdr.AchievementReviewDTO;
import com.example.demo1_nacos.pojo.tdr.ArchiveDO;
import com.example.demo1_nacos.pojo.tdr.HoldingStatisticsDTO;
import com.example.demo1_nacos.pojo.tdr.RecordVolumeNumberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author Created by xutingrong
 * @since 2022/06/29
 */
@Mapper
public interface AbstractArchiveMapper extends BaseMapper<ArchiveDO> {

    /**
     * 动态条件列表查询
     *
     * @param wrapper 动态条件
     * @param page    page条件
     * @return 档案主键
     */
    List<String> findByWrapper(@Param(Constants.WRAPPER) Wrapper<Object> wrapper, IPage page);

    /**
     * 动态条件列表查询
     *
     * @param wrapper 动态条件
     * @param page    page条件
     * @return 档案朱家、档案类型
     */
    List<Map<String, String>> findMapByWrapper(@Param(Constants.WRAPPER) Wrapper<Object> wrapper, IPage page);


    /**
     * 动态条件列表查询
     *
     * @param wrapper 动态条件
     * @param page    page条件
     * @return 档案实体
     */
    IPage<ArchiveDO> findDOByWrapper(@Param(Constants.WRAPPER) QueryWrapper<ArchiveDO> wrapper, IPage page);


    /**
     * 获取父id获取档案id集合
     *
     * @param parentId 父 Id
     * @return 档案id集合
     */
    List<String> getArchiveIdsByParentId(@Param("parentId") String parentId);

    /**
     * 通过档案id获取元数据方案id
     *
     * @param archiveId 档案id
     * @return 元数据方案id
     */
    String getMetadataIdByArchiveId(@Param("archiveId") String archiveId);

    /**
     * 通过门类id获取待移交档案
     *
     * @param parentPath 父路径
     * @return 档案id集合
     */
    List<String> getPendingHandoverArchiveIdsByParentPath(@Param("parentPath") String parentPath);

    /**
     * 通过name查询档案id
     *
     * @param objectName 名称
     * @return 档案id
     */
    String findIdByObjectName(@Param("objectName") String objectName);

    /**
     * 查询该单位下档案（案卷和案件不包含卷内件）的总数
     *
     * @param unitCodes 单位code集合
     * @return 数量
     */
    int getArchiveCountByUnitCodes(@Param("unitCodes") Collection<String> unitCodes);

    /**
     * 根据父id获取档案数量
     *
     * @param parentId 父id
     * @return 档案数量
     */
    Integer getArchiveCountByParentId(@Param("parentId") String parentId);

    /**
     * 根据父路径获取档案数量
     *
     * @param parentPath 父路径
     * @return 档案数量
     */
    Integer getArchiveCountByParentPath(@Param("parentPath") String parentPath);

    /**
     * 根据付路径获取档案对象集合
     *
     * @param parentPath
     * @return
     */
    List<ArchiveDO> getArchiveDOListByParentPath(@Param("parentPath") String parentPath);

    /**
     * 根据类目id获取档案数量
     *
     * @param classId 类目id
     * @return 档案数量
     */
    Integer getArchiveCountByClassId(@Param("classId") String classId);

    /**
     * 根据门类id获取档案数量
     *
     * @param categoryId 门类id
     * @return 档案数量
     */
    Integer getArchiveCountByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 查询该单位下档案（案卷和案件不包含卷内件）的档案信息
     *
     * @param unitCodes 单位code集合
     * @return 档案列表
     */
    List<ArchiveDO> getArchiveDOByUnitCodes(@Param("unitCodes") Collection<String> unitCodes, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 智治门户统计单位下归档的档案的数量
     * 有一个问题 对于group by 下的数据 如果某个条件为空 那么将会过滤掉这条数据
     *
     * @return
     */
    List<ArchiveDO> getArchiveNumByUnitCode();

    /**
     * 查询存在档案的单位的code集合
     *
     * @return
     */
    List<String> queryAllUnitExistArchival();

    /**
     * 获取档案库下题名包含"会议记录"的档案id
     * （用于提供给智治门户做统计）
     *
     * @param title 题名
     * @param path  路径
     * @return 档案id
     */
    String getArchiveIdByPathAndTitle(@Param("title") String title, @Param("path") String path);

    /**
     * 根据身份证查询档案列表
     *
     * @param idNumKey  身份证字段
     * @param idNameKey 身份证名字字段
     * @param idNum     身份证
     * @param idName    身份证名
     * @return 档案id列表
     */
    List<String> findByIdNumAndName(@Param("idNumKey") String idNumKey, @Param("idNameKey") String idNameKey, @Param("idNum") String idNum, @Param("idName") String idName);


    List<ArchiveDO> findByIds(@Param("ids") List<String> ids);

    /**
     * 根据ids获取档案的所有字段
     *
     * @param ids
     * @return 档案列表
     */
    List<ArchiveDO> findAllFieldsByIds(@Param("ids") List<String> ids);

    /**
     * 动态条件列表查询
     *
     * @param wrapper 动态条件
     * @param page    page条件
     * @return 档案实体
     */
    IPage<ArchiveDO> findBaseInfoByWrapper(@Param(Constants.WRAPPER) QueryWrapper<ArchiveDO> wrapper, IPage page);

    /**
     * 获取档案库下有档案的所有单位
     *
     * @return
     */
    int countUnitInArchiveLib();

    /**
     * 统计档案库下所有案卷数量
     *
     * @return
     */
    int countVolumesInArchiveLib();

    /**
     * 统计档案库下所有按件数量
     *
     * @return
     */
    int countRecordsInArchiveLib();

    /**
     * 获取档案库下有内容的单位code
     *
     * @return
     */
    List<String> getUnitCodeInArchiveLib();

    /**
     * 统计档案库下所有的馆藏数（按照年份接收） 根据 年度
     *
     * @return
     */
    List<HoldingStatisticsDTO> saveHoldingStatisticsGroupByYear();

    /**
     * 获取截止当年年度的所有的馆藏
     *
     * @param year
     * @return
     */
    Long getCurrentYearRecordSum(@Param("year") Integer year);

    Long getCurrentYearVolumeSum(@Param("year") Integer year);

    /**
     * 根据年份查询出所有年份的卷件数
     *
     * @return
     */
    List<RecordVolumeNumberDTO> getStartAndEndNumberOfRolls();

    /**
     * 根据父路径模糊查询下面所有的档案数量
     *
     * @param parentPath
     * @return
     */
    Long getRecordOrVolumeSumByParentPath(@Param("parentPath") String parentPath);

    /**
     * 获取所有的档案id，根据父路径
     *
     * @param parentPath
     * @param archiveType
     * @param collectionWay
     * @return
     */
    List<String> getArchiveIdsListByParentPathAndArchiveType(@Param("parentPath") String parentPath, @Param("archiveType") String archiveType, @Param("collectionWay") String collectionWay);


    /**
     * 全宗列表
     *
     * @return
     */
    List<String> fondsList();

    /**
     * 根据ids查询档案
     *
     * @param ids
     * @return
     */
    List<AchievementReviewDTO> selectSearchThesaurusArchives(@Param("ids") List<String> ids);

    /**
     * 查询档案处置密级处置批次的档案id
     * @return
     */
    List<String> getArchiveBySecurityBatchId(@Param("batchId") String batchId);

    /**
     * 查询档案处置密级处置批次的档案门类id
     * @param batchId
     * @return
     */
    List<String> getCategoryListBySecurityBatchId(@Param("batchId") String batchId);

    /**
     * 查询链接在某些父类id的档案id列表
     * @param ids
     * @return
     */
    List<String> findArchiveByLinkIds(@Param("parentIds")List<String>ids);
}
