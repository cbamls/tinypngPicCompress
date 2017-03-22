package com.hearglobal.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
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
public class Provider implements Callable{

	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

	//消息队列
	private BlockingQueue<File> messageQueue;

	private static LinkedList<String> logList ;

	//生产东西来源
	private static File src;

	/**
	 * Instantiates a new Provider.
	 *
	 * @param messageQueue the message queue
	 * @param src          the src
	 */
	public Provider(BlockingQueue messageQueue, File src){
		this.messageQueue = messageQueue;
		this.src = src;
	}


	public Object call() throws Exception {
		try {
			load(src);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} finally {
			return true;
		}
	}

	private void load(File src) throws InterruptedException {
		LOGGER.info("Provider load src:{}", src.getAbsolutePath());
		// 当找到目录时，创建目录
		if (src.isDirectory()) {
			if(!logList.contains(src.getAbsolutePath())) {
				if(!this.messageQueue.offer(src, 2, TimeUnit.SECONDS)) {
					System.out.println("目录提交队列失败....");
				};
			}
			File[] files = src.listFiles();
			for(File file : files) {
				load(file);
			}
			//当找到文件时
		} else if (src.isFile()) {
			if(validatePic(src) && !logList.contains(src.getAbsolutePath())) {
				if(!this.messageQueue.offer(src, 2, TimeUnit.SECONDS)) {
					System.out.println("文件提交队列失败....");
				}
			}
		}
	}

	private boolean validatePic(File file) {
		int loc = file.getAbsolutePath().lastIndexOf(".");
		String suffix = file.getAbsolutePath().substring(++loc);
		return suffix.equals("jpg") || suffix.equals("png");
	}

	/**
	 * Sets log list.
	 *
	 * @param logList the log list
	 */
	public static void setLogList(LinkedList<String> logList) {
		Provider.logList = logList;
	}
}
