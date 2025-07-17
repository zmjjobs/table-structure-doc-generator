package com.zmj.springboot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class MyFileUtils {
    /**
     * 打印内容到文件中
     * @param buffer 打印内容
     * @param filePath  文件路径
     * @param needOpen 是否需要用记事本打开文件
     * @throws IOException
     */
    public static void print2File(StringBuffer buffer, String filePath,boolean needOpen) throws IOException {
        File logFile = new File(filePath);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        PrintStream ps = new PrintStream(logFile);
        System.setOut(ps);
        if (buffer != null && buffer.length() > 0) {
            System.out.print(buffer.toString());
        }
        ps.close();
        //用记事本打开文件
        if (needOpen) {
            Process p = Runtime.getRuntime().exec( "notepad.exe " +filePath);
        }
    }
}
