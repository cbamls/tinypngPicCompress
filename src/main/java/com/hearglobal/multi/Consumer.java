package com.hearglobal.multi;

import com.tinify.Options;
import com.tinify.Source;
import com.tinify.Tinify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
public class Consumer implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    //多线程间是否启动变量，有强制从主内存中刷新的功能。即时返回线程的状态
    private volatile boolean isRunning = true;

    private static String srcBase;

    private static String destBase;

    //已消费队列
    private BlockingQueue<String> writeQueue;

    //消息队列
    private BlockingQueue<File> messageQueue;

    private static Lock lock = new ReentrantLock();

    private static List<String> API_key = new CopyOnWriteArrayList<>();

    private static File dest;

    /**
     * Instantiates a new Consumer.
     *
     * @param messageQueue the message queue
     * @param writeQueue   the write queue
     * @param dest         the dest
     * @param srcBase      the src base
     * @param destBase     the dest base
     */
    public Consumer(BlockingQueue messageQueue,BlockingQueue writeQueue, File dest, String srcBase, String destBase){
        this.messageQueue = messageQueue;
        this.writeQueue = writeQueue;
        this.dest = dest;
        this.srcBase = srcBase;
        this.destBase = destBase;
    }

    public void run() {
        while(isRunning){
            try {
                lock.lock();
                //获取数据
                File file = this.messageQueue.poll(2, TimeUnit.SECONDS);
                if(file == null) {
                    stop();
                    LOGGER.info("Current Consumer - {} - consume faild, messageQueue empty, thread is stopping...", Thread.currentThread().getName());
                    lock.unlock();
                    continue;
                }
                dest = new File(destBase + file.getAbsolutePath().substring(srcBase.length()));
                lock.unlock();
                //进行数据处理
                doCompress(file, dest);
                LOGGER.info("Current Consumer - {} - consume success, messageId : {} ", Thread.currentThread().getName(), file.getAbsolutePath());

                writeQueue.add(file.getAbsolutePath());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void doCompress(File src, File dest) {
        LOGGER.info("Current Consumer - {} -  Comsumer doCompress src:{}",Thread.currentThread().getName(), src.getAbsolutePath());
        if(src.isDirectory()) {
            dest.mkdirs();
        } else {
            Tinify.setKey(API_key.get(Thread.currentThread().getName().hashCode() % API_key.size()));
            try {
              Source source = Tinify.fromFile(src.getAbsolutePath());
               BufferedImage bufferedImage = ImageIO.read(src);
               if(bufferedImage.getWidth() > 800) {
                   Options options = new Options()
                           .with("method", "scale")
                           .with("width", 800);
                   Source resized = source.resize(options);
                   resized.toFile(dest.getAbsolutePath());
               } else {
                   source.toFile(dest.getAbsolutePath());
               }

           } catch (Exception e) {
               e.printStackTrace();
               LOGGER.error("Current Consumer - {} - Consumer doCompress exception error:{}, src.path:{}", Thread.currentThread().getName(), e.getMessage(), src.getAbsolutePath());
               copyFile(src, dest);
           }
        }
    }

    private void copyFile(File src, File dest) {

        try {
            LOGGER.info("Current Consumer - {} - src.path:{},  dest.path:{}", Thread.currentThread().getName(), src.getAbsolutePath(), dest.getAbsolutePath());
            while(!dest.getParentFile().exists()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
           // lock.lock();
            Files.copy(src.toPath(), dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Consumer copyFile  cause error:{}, currentThread.getName:{}", e.getMessage(), Thread.currentThread().getName());
        } finally {
           // lock.unlock();
        }
    }


    /**
     * Stop.
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * Gets src base.
     *
     * @return the src base
     */
    public static String getSrcBase() {
        return srcBase;
    }

    /**
     * Sets src base.
     *
     * @param srcBase the src base
     */
    public static void setSrcBase(String srcBase) {
        Consumer.srcBase = srcBase;
    }

    /**
     * Gets dest base.
     *
     * @return the dest base
     */
    public static String getDestBase() {
        return destBase;
    }

    /**
     * Sets dest base.
     *
     * @param destBase the dest base
     */
    public static void setDestBase(String destBase) {
        Consumer.destBase = destBase;
    }

    /**
     * Gets api key.
     *
     * @return the api key
     */
    public static List<String> getAPI_key() {
        return API_key;
    }

    /**
     * Sets api key.
     *
     * @param API_key the api key
     */
    public static void setAPI_key(List<String> API_key) {
        Consumer.API_key = API_key;
    }
}