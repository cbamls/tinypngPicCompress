package com.hearglobal.single; /**
 * CopyRright (c)2014-2016 Haerbin Hearglobal Co.,Ltd
 * Project: idoc
 * Comments:
 * Author:cbam
 * Create Date:2016/10/19
 * Modified By:
 * Modified Date:
 * Modified Reason:
 */

import com.tinify.Options;
import com.tinify.Source;
import com.tinify.Tinify;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;


/**
 * toCompress接口src dest 两者可以同时为相同或着不同的目录路径 意味着A目录的图片会压缩到B目录中 不同目录间的复制会过滤非png jpg图片
 *                       两者可以是相同文件路径 意味着A图片压缩后会替换B图片
 *
 */
public class SingleCompress {

    private static final Logger LOGGER = Logger.getLogger(SingleCompress.class);
    private final LinkedList<String> API_key = new LinkedList<String>();
    private static boolean flag = true;
    private int key_no = 0;
    private int count = 0;

    public boolean checkDuplicate(String dest) {
        File file = new File(dest);
        return file.exists();
    }

    public void copyFile(File src, File dest) throws IOException {
        Files.copy(src.toPath(), dest.toPath());
    }


    public void getAPI_keyFromProperties () throws IOException {
        //在window中 并且在IDE中运行的时候/api_key.properties  和 api_key.properties 都是target 的classes路径下
        //但是在控制台窗口中却没有尝试出相对路径可行
        InputStreamReader isr=new InputStreamReader(SingleCompress.class.getResourceAsStream("/META-INF/api_key.properties"), "UTF-8");

        //在IDE中 能够获取target下的配置文件
        //但是在控制台窗口中能够获取项目的根目录下面的配置文件
       //  InputStreamReader isr=new InputStreamReader(new FileInputStream("api_key.properties"), "UTF-8");

        BufferedReader buff = new BufferedReader(isr);

        String line = "";

        try {
            while ((line=buff.readLine())!=null) {
                System.out.println("API_key => " + line);
                API_key.add(line);
            }

            buff.close();
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dfs(File src, File dest) {
        // 当找到目录时，创建目录
        if (src.isDirectory()) {
            dest.mkdir();
            File[] files = src.listFiles();
            for(File file3 : files) {
                try {
                    dfs(file3, new File(dest, file3.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //当找到文件时
        } else if (src.isFile()) {

            int loc = dest.getAbsolutePath().lastIndexOf(".");
            String suffix = dest.getAbsolutePath().substring(++loc);
            LOGGER.debug("Compressing =>" + src.getAbsolutePath());
            LOGGER.debug("Amount =>" + ++count);
            if(suffix.equals("png") || suffix.equals("jpg")) {
                //判断是否压缩过 flag 表示是否src 和 dest相同
                if(flag && checkDuplicate(dest.getAbsolutePath())) {
                    LOGGER.debug("completed~!");
                    return ;
                }
                if(++key_no == API_key.size()) {
                    key_no = 0;
                }
                Tinify.setKey(API_key.get(key_no));
                try {

                    Source source = Tinify.fromFile(src.getAbsolutePath());
                    BufferedImage bufferedImage = ImageIO.read(src);
                    if(bufferedImage.getWidth() > 800) {
                        LOGGER.debug("\nbefore = W:" + bufferedImage.getWidth() + "H:" + bufferedImage.getHeight());
                        Options options = new Options()
                                .with("method", "scale")
                                .with("width", 800);
                        Source resized = source.resize(options);
                        resized.toFile(dest.getAbsolutePath());
                        BufferedImage bufferedImageDest = ImageIO.read(dest);
                        LOGGER.debug("\nafter = W:" + bufferedImageDest.getWidth() + "H:" + bufferedImageDest.getHeight());
                    } else {
                        source.toFile(dest.getAbsolutePath());
                    }
                } catch (Exception e) {
                    try {
                        LOGGER.error("\nerror ! current_API_key=============>" + API_key.get(key_no));
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw, true);
                        e.printStackTrace(pw);
                        String msg=sw.toString();
                        LOGGER.error("\noutput excetion info===> " + msg + "\nrecovery from it . \ncontinue....\n" + src.getAbsolutePath() + "\n");
                        copyFile(src, dest);
                    } catch (IOException e1) {
                        count--;
                        e1.printStackTrace();
                        return ;
                    }
                }
            } else {
                LOGGER.error("无法处理的文件类型！跳过");
            }
        }
    }


    public boolean checkPicture(String src, String dest) {
        if(src.equals(dest) && new File(src).isDirectory()) {
            flag = false;
        }
        if( src.equals(dest) && !new File(src).isDirectory() ) {
            flag = false;
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
        }else if(! new File(src).exists() || ! new File(dest).exists()) {
            LOGGER.error("src 路径未找到！");
            return false;
        } else {
            return true;
        }
    }

    public void toCompress(String src, String dest) throws IOException {
        getAPI_keyFromProperties();
        LOGGER.debug("get_api_key...\n start...");
        if(checkPicture(src, dest)) {
            dfs(new File(src), new File(dest));
            LOGGER.debug("压缩完成");
        }
    }
}