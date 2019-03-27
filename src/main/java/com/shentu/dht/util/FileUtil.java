package com.shentu.dht.util;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by styb on 2019/3/19.
 */
public class FileUtil {

    private static String filePath="infohash.txt";

    private static String nodePath="node.txt";

    @SneakyThrows
    public static void wirte(String value) {
        FileUtils.write(new File("/usr/local/src/log/"+filePath),value,"utf-8",true);
    }

    @SneakyThrows
    public static void wirteNode(String value) {
        FileUtils.write(new File("/usr/local/src/log/"+nodePath),value,"utf-8",true);
    }

    public static void main(String[] args) {
        FileUtil f=new FileUtil();
        f.wirte("df");
    }
}
