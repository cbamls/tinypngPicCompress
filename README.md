# tinypngPicCompress

## 线程模型


 1. 生产者`Provider`线程为一，主要进行深搜目录文件；、
 2. 消费者`Consumer`线程多个， 因为RPC服务调用时延较长， 启用多个线程请求服务。
 3. 持久化线程`Persist` 将已经消费的消息存放在`writeQueue`， 启用一个线程从`writeQueue`取数据进行持久化到`log.pic`，这样每次启动压缩的时候，可以避免重复消费。 进而避免同一目录进行多次压缩
 4. `api_key.properties` 为申请的https://tinypng.com/ 的`key`  ，每个`key`一个月可以压缩500张， 采用线程名的`hashCode`对`key`的个数取模运算，选择所要使用的`key`。 
## 可扩展性
` doCompress`方法可以进行任意业务逻辑。只是我的实现是用来压缩图片了

源码已打入jar包

##如何使用
1、下载`Compress.jar`包

2、maven手动安装jar

`mvn install:install-file -DgroupId=com.hearglobal -DartifactId=multi -Dversion=2.0 -Dpackaging=jar -Dfile=C:/Users/cbam/Desktop/Compress.jar`

3、公共接口说明：

    compress.setApi_key_location("/api_key.properties"); // 指定api_key文件位置 key需要申请 可选设置 默认为项目根路径 如E:/work/{project}/api_key.properties或/work/{project}/api_key.properties

    compress.setPic_log_location("/log.pic");  //指定log.pic路径  可选配置 默认为项目根路径 如/work/{project}/log.pic 或 /work/{project}/log.pic

    compress.compress("E:/upan/test", "E:/upan/test_bak", 3);//压缩调用  第一个参数为要压缩的目录  第二个参数为 压缩输出目录 第三个参数 启动的线程数 第三个参数可选 默认CPU核心数
3、程序调用jar示例一

建议指定绝对路径

    public static void main(String[] args) throws Exception {
        Starter compress = new Starter();
        compress.setApi_key_location("/api_key.properties");
        compress.setPic_log_location("/log.pic");
        compress.compress("E:/upan/test", "E:/upan/test_bak", 3);
    }

4、程序调用jar示例二

打开win控制台或 在Linux shell下jar包当前目录 运行如下命令

    java -jar Compress.jar E:/upan/test E:/upan/test_bak 3   //压缩调用  第一个参数为要压缩的目录  第二个参数为 压缩输出目录 第三个参数 启动的线程数

注意：此方式无法设置配置文件位置， 故请将文件`api_key.properties`默认放置在 jar包所在目录