package com.zy.alg.infoextra.demo;

import com.zy.alg.infoextra.htmlanalysis.TableParser;

import java.io.*;

/**
 * @author zhangycqupt@163.com
 * @date 2019/03/06 17:21
 */
public class DemoTableParser {
    public static void main(String[] args) {
        String htmlPath = "";
        String outputJsonPath = "";
        PrintWriter pw;
        // several html file
        File basePath = new File(htmlPath);
        File[] files = basePath.listFiles();
        int num = 0;
        assert files != null;
        for (File file : files) {
            if (file.toString().endsWith("html")) {
                String fileName = file.toString().replace(htmlPath + "\\", "");
                System.out.println(++num + "#\t" + fileName);
                String json = TableParser.tableAnalysys(file);
                try {
                    pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream
                            (outputJsonPath + "\\" + fileName.replace(".html", "") + ".json"), "utf-8"));
                    pw.println(json);
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
