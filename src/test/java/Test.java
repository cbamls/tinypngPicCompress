import com.hearglobal.multi.Starter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * CopyRright (c)2014-2016 Haerbin Hearglobal Co.,Ltd
 * Project: tinypngThread
 * Comments:
 * Author:cbam
 * Create Date:2017/1/4
 * Modified By:
 * Modified Date:
 * Modified Reason:
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Starter compress = new Starter();
        compress.compress("E:\\upan\\test", "E:\\upan\\test_bak", 3);
    }
}
