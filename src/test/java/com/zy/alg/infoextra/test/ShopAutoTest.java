package com.zy.alg.infoextra.test;

import com.zy.alg.infoextra.update.auto.ShopTrain;
import com.zy.alg.infoextra.update.auto.impl.ShopTrainImpl;

/**
 * brand name auto train
 */
public class ShopAutoTest {

    public static void main(String[] args) {
        String rawDataPath = "E:\\项目\\猪八戒店铺品牌名标注抽取与自动更新机制\\autotrain\\rawData";
        String resourcePath = "E:\\项目\\猪八戒店铺品牌名标注抽取与自动更新机制\\autotrain\\resources";
        String outputPath = "E:\\项目\\猪八戒店铺品牌名标注抽取与自动更新机制\\autotrain\\model";
        ShopTrain shoptrain = new ShopTrainImpl();
        shoptrain.init(rawDataPath, resourcePath, outputPath);
        boolean trainFlag = shoptrain.train();
        if (trainFlag) {
            System.out.println("model train success!");
        } else {
            System.err.println("model train failed!");
            return;
        }
        boolean checkFlag = shoptrain.check();
        if (checkFlag) {
            System.out.println("model check success!");
        } else {
            System.err.println("model check failed!");
        }
    }

}
