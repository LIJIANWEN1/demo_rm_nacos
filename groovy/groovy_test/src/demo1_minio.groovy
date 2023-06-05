// 使用@Grab注解加载依赖
@Grapes([
        @Grab(group = 'io.minio', module = 'minio', version = '6.0.12')
])
//@GrabResolver(name='restlet', root='http://maven.restlet.org/')
import io.minio.MinioClient


static void upload(String path, String localPath) throws Exception {
    print 'xxxxx'
    try {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        //初始化
        MinioClient minioClient = new MinioClient("http://hzent.amberdata.cn:9000", "admin", "Dctm@1234");
        //上传文件
        minioClient.putObject("666", path, localPath);
        throw new NullPointerException("yicha");
    } catch (Exception e) {
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)
        e.printStackTrace(pw)
    }
}

upload("666.pdf","C:\\Users\\AB_ZhangLei\\Desktop\\报销\\965.pdf")

