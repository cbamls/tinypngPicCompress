package com.hearglobal.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
public class Persist extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private BlockingQueue<String> writeQueue;

    private boolean isRunning = true;

    private long start;

    /**
     * Instantiates a new Persist.
     *
     * @param writeQueue the write queue
     * @param start      the start
     */
    public Persist(BlockingQueue writeQueue, long start) {
        this.writeQueue = writeQueue;
        this.start = start;
    }

    @Override
    public void run() {
        try {

            while(!shouldStop()) {
                Thread.sleep(10 * 1000);
                List<String> list = new LinkedList();
                while(!writeQueue.isEmpty()) {
                    list.add(writeQueue.poll(5, TimeUnit.SECONDS));
                }
                doPersist(list);
            }
            LOGGER.info("Persist thread work finished");
            LOGGER.info("耗时: {} ms",start - System.currentTimeMillis());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doPersist(List<String> messageIds) throws IOException {
        FileWriter writer = new FileWriter("log.pic", true);
        for(String str : messageIds) {
            writer.write(str + "\n");
        }
        writer.close();
    }

    /**
     * Is running boolean.
     *
     * @return the boolean
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Sets running.
     *
     * @param running the running
     */
    public void setRunning(boolean running) {
        isRunning = running;
    }
    //persist线程终止的条件是 所有消费线程已停止 && 当前 已消费消息队列为空
    private boolean shouldStop() {
        return isRunning == false && writeQueue.size() == 0;
    }
}
