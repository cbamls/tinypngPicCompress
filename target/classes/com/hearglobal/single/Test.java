package com.hearglobal.single;

import java.io.IOException;

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
    public static void main(String[] args) throws IOException {
        Compress compress = new Compress();
        compress.toCompress("d:/pic/20170104", "d:/pic/20170104");
    }
}
