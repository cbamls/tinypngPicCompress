package com.hearglobal.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * CopyRright (c)2014-2016 Haerbin Hearglobal Co.,Ltd
 * Project: tinypngThread
 * Comments:
 * Author:cbam
 * Create Date:2017/3/21
 * Modified By:
 * Modified Date:
 * Modified Reason:
 */
public class Starter {

    private static CountDownLatch cdl = new CountDownLatch(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);

    private static int coreNumber = Runtime.getRuntime().availableProcessors();

    private static List<String> API_key = new CopyOnWriteArrayList<>();

    private static LinkedList<String> logList = new LinkedList<>() ;

    private static String src;

    private static String dest;

    private static int threshold;

    private static long start;

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(String[] args){
        start = System.currentTimeMillis();
        String src = args[0];
        String dest = args[1];
        threshold = args.length < 3 || args[2] == null || args[2].equals("") ? coreNumber : Integer.parseInt(args[2]);
        try {
            new Starter().compress(src, dest, threshold);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Compress.
     *
     * @param src       the src
     * @param dest      the dest
     * @param threshold the threshold
     * @throws Exception the exception
     */
    public void compress(String src, String dest, int threshold) throws Exception {
        if(!this.checkParam(src, dest)) {
            throw  new IllegalArgumentException("参数不正确！");
        }
        this.src = src;
        this.dest = dest;
        this.threshold = threshold <= 0 ? coreNumber : threshold;
        LOGGER.info("运行参数：{}, {}, {}", src, dest, threshold);
        try {
            //消息队列
            BlockingQueue<File> messageQueue = new LinkedBlockingQueue<>();

            //已消费队列 用于已消费消息的持久化 使得单线程对文件读写
            final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();

            ExecutorService cachePool = Executors.newCachedThreadPool();

            //生产者提交 最多启动一个

            Provider p = new Provider(messageQueue, new File(src));
            initProvider();
            p.setLogList(logList);
            Future<Boolean> future = cachePool.submit(p);
            //阻塞到provide 执行完成
            while(!future.get()) {
                future = cachePool.submit(p);
            }


            initConsumer();
            //消费者提交
            for(int i = 0; i < threshold; i++) {
                Consumer c = new Consumer(messageQueue, writeQueue, new File(dest), src, dest);
                c.setAPI_key(API_key);
                cachePool.execute(c);
            }

            //启动持久化消息线程工作 消费writeQueue
            Persist pressit =  new Persist(writeQueue, start);
            pressit.start();

            cachePool.shutdown();
            while(true) {
                if(cachePool.isTerminated()) {
                    pressit.setRunning(false);
                    LOGGER.info("compress has finished !");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("压缩失败");
        }
    }

    /**
     * Check param boolean.
     *
     * @param src  the src
     * @param dest the dest
     * @return the boolean
     */
    public boolean checkParam(String src, String dest) {
      if(src == null || dest == null || src.equals("") || dest.equals("")) {
          LOGGER.error("参数为空！");
          return false;
      }
        int loc_src = src.lastIndexOf(".");
        String suffix_src = src.substring(++loc_src);
        int loc_dest = dest.lastIndexOf(".");
        String suffix_dest = src.substring(++loc_dest);
        if(suffix_dest.equals("png") && suffix_src.equals("jpg")) {
            LOGGER.error("错误图片类型转换！");
            return false;
        } else if(suffix_dest.equals("jpg") && suffix_src.equals("png")) {
            LOGGER.error("错误图片类型转换！");
            return false;
        } else if(! new File(src).exists()) {
            LOGGER.error("src 路径未找到！");
            return false;
        } else {
            return true;
        }
    }

    public void initConsumer() {

        String line = "";

        try {
            //BufferedReader buff = new BufferedReader(new FileReader("E:\\workplace\\tinypngThread\\target\\api_key.properties"));
            BufferedReader buff = new BufferedReader(new FileReader("api_key.properties"));
            while ((line=buff.readLine())!=null) {
                System.out.println("API_key => " + line);
                API_key.add(line);
            }

            buff.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Consumer initConsumer error:{}", e.getMessage());
        }
    }

    /**
     * Init provider.
     *
     * @throws IOException the io exception
     */
    public static void initProvider() throws IOException {
        LOGGER.info("Provider initProvider invoked");
        File file = new File("log.pic");
        if(!file.exists()) {
            file.createNewFile();
        }
        FileReader reader = new FileReader("log.pic");
        BufferedReader br = new BufferedReader(reader);
        String str;
        while((str = br.readLine()) != null) {
            logList.add(str);
        }
    }

}
