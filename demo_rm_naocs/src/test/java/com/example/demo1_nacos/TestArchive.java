package com.example.demo1_nacos;
import cn.amberdata.afc.common.util.Md5Utils;
import cn.amberdata.dm.organization.role.Role;
import cn.amberdata.dm.organization.role.RoleRepository;
import cn.amberdata.dm.organization.unit.UnitRepository;
import cn.amberdata.rm.common.util.MD5Utils;
import com.example.demo1_nacos.service.ArchiveServiceImpl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhangLei
 * @version 1.0
 * @date 2022/9/17 9:46
 */

public class TestArchive {



    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        File file  = new File("C:\\Users\\AB_ZhangLei\\Desktop\\J103-2021-00001.zip");
        System.out.println(MD5Utils.getmd5(file));
    }
}
