package com.shentu.dht.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by styb on 2019/3/19.
 */
public class FileUtil {

    private static String filePath="infohash.txt";

    private static String nodePath="node.txt";

    private static String dataPath="data.txt";

    @SneakyThrows
    public static void wirte(String value) {
        if(value.contains("10653ce58fbeec7d4c056e0e0613fc9588ce5023")){
            return;
        }
        FileUtils.write(new File("/usr/local/src/log/"+filePath),value,"utf-8",true);
    }

    @SneakyThrows
    public static void wirteData(String value) {
        FileUtils.write(new File("/usr/local/src/log/"+dataPath),value,"utf-8",true);
    }

    @SneakyThrows
    public static void wirteNode(String value) {
        FileUtils.write(new File("/usr/local/src/log/"+nodePath),value,"utf-8",true);
    }

    @SneakyThrows
    public static List<String> getInfoHashPeer(){
        @Cleanup BufferedReader bufferedReader=new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(
                                new File("/usr/local/src/log/"+filePath))));
        List<String> result=new ArrayList<String>();
        String line=null;
        while((line=bufferedReader.readLine())!=null) {
            line=line.replace("peers==","");
            if (line.contains(",")) {
                String[] split = line.split(",");
                String[] address = split[1].replace(";", "").split(":");
                result.add(split[0] + "," + address[0] + "," + address[1]+","+address[2]);
            } else if (line.contains("&")) {
                String[] split = line.split("&");
                String[] address = split[1].split(";");
                if (ArrayUtils.isNotEmpty(address)) {
                    for (String add : address) {
                        String[] split1 = add.split(":");
                        result.add(split[0] + "," + split1[0] + "," + split1[1]+"," + split1[2]);
                    }
                }

            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> infoHashPeer = getInfoHashPeer();
        Set<String> ss=new HashSet<>();
        for(String s:infoHashPeer){
            String val=s.split(",")[1]+","+s.split(",")[2];
            ss.add(val);
        }


    }
}
