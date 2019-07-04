package com.zy.alg.infoextra.htmlanalysis;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * @author zhangycqupt@163.com
 * @date 2019/03/06 17:48
 */
public class TableInfo {
    /**
     * table sequence number
     */
    private int tableIndex;
    /**
     * table header
     */
    private String describe;
    /**
     * table matrix
     */
    private String[][] matrix;

    /**
     * table valid
     */
    private Boolean lastInvalid;

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

    public void setLastInvalid(Boolean lastInvalid) {
        this.lastInvalid = lastInvalid;
    }

    public Boolean getLastInvalid() {
        return lastInvalid;
    }

}
