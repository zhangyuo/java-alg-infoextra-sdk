package com.zy.alg.infoextra.update.auto.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.zy.alg.infoextra.update.auto.ShopTrain;

import com.zy.alg.infoextra.update.util.TrainFileUtil;
import com.zy.alg.infoextra.service.BrandNameExtraction;
import com.zy.alg.infoextra.service.BrandNameExtractionEnhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * create by zhangyu on 2018/5/15
 */
public class ShopTrainImpl implements ShopTrain {

    private static final Logger logger = LoggerFactory.getLogger(ShopTrainImpl.class);

    private String resourcePath;
    private String rawDataPath;
    private String outputPath;

    private final String shopCorpus = "shopInfoCorpus";

    private double oldFileSize = 1700000.0;
    private double size_thre = 0.6;

    public void setSize_thre(double size_thre) {
        this.size_thre = size_thre;
    }

    public double getSize_thre() {
        return size_thre;
    }

    @Override
    public void init(String rawDataPath, String resourcePath, String outputPath) {
        if (null == resourcePath || null == rawDataPath) {
            logger.error("resource path or raw data path is not ready!");
            return;
        }
        if (!rawDataPath.endsWith(File.separator)) {
            rawDataPath = rawDataPath + File.separator;
        }
        if (!resourcePath.endsWith(File.separator)) {
            resourcePath = resourcePath + File.separator;
        }
        if (!outputPath.endsWith(File.separator)) {
            outputPath = outputPath + File.separator;
        }
        File outputPathDir = new File(outputPath);
        if (!outputPathDir.exists() && !outputPathDir.isDirectory()) {
            outputPathDir.mkdir();
        }
        this.rawDataPath = rawDataPath;
        this.resourcePath = resourcePath;
        this.outputPath = outputPath;
    }

    @Override
    public boolean train() {
        logger.info("start train shop model...");
        long start = System.currentTimeMillis();
        BrandNameExtraction bne = new BrandNameExtractionEnhancer(resourcePath);
        String shopInfo = rawDataPath + shopCorpus;
        String modelPath = outputPath + "BrandWord";
        boolean flag = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(shopInfo), "utf-8"));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(modelPath), "utf-8"), true);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] seg = line.split(",");
                String userId = "";
                String brandName = "";
                String realType = "0";
                double ability = 0.0;
                int len = seg.length;
                switch (len) {
                    case 2:
                        userId = seg[0];
                        brandName = seg[1];
                        break;
                    case 3:
                        userId = seg[0];
                        brandName = seg[1];
                        switch (seg[1]) {
                            case "":
                                realType = "0";
                                break;
                            case "个人":
                                realType = "1";
                                break;
                            case "企业":
                                realType = "2";
                                break;
                            case "个体经营":
                                realType = "3";
                                break;
                            case "事业单位团体":
                                realType = "4";
                                break;
                            case "政府":
                                realType = "5";
                                break;
                            case "学校":
                                realType = "6";
                                break;
                            case "媒体":
                                realType = "7";
                                break;
                        }
                        break;
                    case 4:
                        userId = seg[0];
                        brandName = seg[1];
                        switch (seg[1]) {
                            case "":
                                realType = "0";
                                break;
                            case "个人":
                                realType = "1";
                                break;
                            case "企业":
                                realType = "2";
                                break;
                            case "个体经营":
                                realType = "3";
                                break;
                            case "事业单位团体":
                                realType = "4";
                                break;
                            case "政府":
                                realType = "5";
                                break;
                            case "学校":
                                realType = "6";
                                break;
                            case "媒体":
                                realType = "7";
                                break;
                        }
                        ability = Double.parseDouble(seg[3]);
                        break;
                }
                String brandWord = bne.getBrandWord(brandName, realType, ability);
                if (brandWord == null) {
                    brandWord = "null";
                }
                pw.println(userId + "," + brandName + "," + brandWord);
            }
            br.close();
            pw.close();
            flag = true;
        } catch (Exception e) {
            logger.error("shop model train failed!", e);
        }
        long end = System.currentTimeMillis();
        if (flag) {
            logger.info("shop model train success! spend '{}' s", (end - start) / 1000);
        } else {
            logger.error("shop model train failed! spend {} s", (end - start) / 1000);
        }
        return flag;
    }

    @Override
    public boolean check() {
        //check file size
        logger.info("check model file size ...");
        logger.info("size thre is {}", size_thre);
        String shopInfo = rawDataPath + shopCorpus;
        long newFileSize = TrainFileUtil.GetFileSize(new File(shopInfo));
        if (newFileSize * 1.0 / oldFileSize >= size_thre) {
            logger.info("new shop brand words file size is acceptable,the ratio is {}", newFileSize * 1.0 / oldFileSize);
        } else {
            logger.error("new shop brand words file size is not acceptable,the ration is {}", newFileSize * 1.0 / oldFileSize);
            return false;
        }
        return true;
    }

}
