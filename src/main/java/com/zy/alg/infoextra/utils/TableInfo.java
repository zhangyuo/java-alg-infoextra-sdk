package com.zy.alg.infoextra.utils;

/**
 * @author zhangycqupt@163.com
 * @date 2019/03/06 17:48
 */
public class TableInfo {
    /**
     * table sequence number
     */
    int tableIndex;
    /**
     * table header
     */
    String describe;
    /**
     * table matrix
     */
    String[][] matrix;

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getDescribe() {
        return describe;
    }

    public void setMatrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public String[][] getMatrix() {
        return matrix;
    }

}
