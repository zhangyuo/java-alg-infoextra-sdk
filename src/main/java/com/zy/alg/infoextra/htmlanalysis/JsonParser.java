package com.zy.alg.infoextra.htmlanalysis;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangyuo@zbj.com
 * @date 2019-06-27 20:57
 */
public class JsonParser {

    private static Log logger = LogFactory.getLog(JsonParser.class);

    /**
     * 对表格json进行解析并数据提取
     *
     * @param input 表格矩阵及属性已存为json字符串
     * @return 结构化表格数据
     */
    public static String getTableData(String input) {
        String data = "";
        try {
            if (input == null || input.equals("")) {
                logger.info("please input json string");
                return data;
            }
            JSONObject jsonObject = JSONObject.fromObject(input);
//            logger.info("json对象化成功");
            if (jsonObject == null) {
                logger.info("json object is empty");
            } else {
                // 获取表格数组
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                List<String> colRowValueInfo = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    // 获取第i个表格
                    JSONObject currentTable = jsonArray.getJSONObject(i);
                    String tableName = currentTable.getString("describe");
                    JSONArray tableMatrix = currentTable.getJSONArray("matrix");
                    // 表格信息格式化提取
                    colRowValueInfo = getTableDataInfo(tableMatrix, tableName);
                }
                if (colRowValueInfo != null) {
                    for (String a : colRowValueInfo) {
                        data += a + "\n";
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return data;
    }

    /**
     * 表格数据信息格式化提取
     *
     * @param tableMatrix
     * @param tableName
     * @return <行表头，列表头，value，unit> 格式
     */
    private static List<String> getTableDataInfo(JSONArray tableMatrix, String tableName) {
        List<String> dataList = new ArrayList<>();
        // 判断表格是否含有数值，无数值表格略过
        Boolean hasValue = findValue(tableMatrix);
        if (!hasValue) {
            return null;
        }
        // 单位提取
        String unit = extractUnit(tableName);
        // 表格类型判断
        Integer type = tableTypeAnalysis(tableMatrix);
        String[] rowHeader;
        String[] colHeader;
        switch (type) {
            case 1:
                // 获取表头信息
                rowHeader = extractRowHeader(tableMatrix);
                colHeader = extractColHeader(tableMatrix);
                // 提取表格数据值
                dataList = extractData(tableMatrix, rowHeader, colHeader, unit);
                break;
            case 2:
                // 合并单元格（前两行）
                tableMatrix = mergeCells(tableMatrix);
                // 获取表头信息
                rowHeader = extractRowHeader(tableMatrix);
                colHeader = extractColHeader(tableMatrix);
                // 提取表格数据值
                dataList = extractData(tableMatrix, rowHeader, colHeader, unit);
                break;
            case 3:
                // 规整表格
                int standLine = 1;
                List<JSONArray> matrix = standSpCol(tableMatrix, standLine);
                for (JSONArray ma : matrix) {
                    // 合并单元格（前两行）
                    tableMatrix = mergeCells(ma);
                    // 获取表头信息
                    rowHeader = extractRowHeader(tableMatrix);
                    colHeader = extractColHeader(tableMatrix);
                    // 提取表格数据值
                    List<String> dataNew = extractData(tableMatrix, rowHeader, colHeader, unit);
                    for (String data : dataNew) {
                        dataList.add(data);
                    }
                }
                break;
            case 4:
                // 判断去除特殊序号列
                tableMatrix = removeSpCol(tableMatrix);
                // 规整表格
                standLine = 0;
                matrix = standSpCol(tableMatrix, standLine);
                // 获取表头信息
                for (JSONArray ma : matrix) {
                    // 获取表头信息
                    rowHeader = extractRowHeader(ma);
                    colHeader = extractColHeader(ma);
                    // 提取表格数据值
                    List<String> dataNew = extractData(ma, rowHeader, colHeader, unit);
                    for (String data : dataNew) {
                        dataList.add(data);
                    }
                }
                break;
            default:
                logger.warn("位置表格数据类型");
        }
        return dataList;
    }

    /**
     * 去除特殊序号列
     *
     * @param tableMatrix
     * @return
     */
    private static JSONArray removeSpCol(JSONArray tableMatrix) {
        List<String> rovColStr = new ArrayList<String>() {{
            add("排名");
            add("序号");
        }};
        // 判断第一行去除列数
        int colNum = 0;
        for (int j = 0; j < tableMatrix.getJSONArray(0).size(); j++) {
            String tmpStr = spValueProcess(tableMatrix.getJSONArray(0).get(j).toString());
            if (rovColStr.contains(tmpStr)) {
                colNum++;
            }
        }
        // 去除特殊列，更新矩阵
        String[][] matrix = new String[tableMatrix.size()][tableMatrix.getJSONArray(0).size() - colNum];
        for (int i = 0; i < tableMatrix.size(); i++) {
            colNum = 0;
            for (int j = 0; j < tableMatrix.getJSONArray(i).size(); j++) {
                String tmpStr = spValueProcess(tableMatrix.getJSONArray(0).get(j).toString());
                if (rovColStr.contains(tmpStr)) {
                    colNum++;
                    continue;
                }
                matrix[i][j - colNum] = spValueProcess(tableMatrix.getJSONArray(i).get(j).toString());
            }
        }
        return JSONArray.fromObject(matrix);
    }

    /**
     * 规整表格
     *
     * @param tableMatrix
     * @param standLine   标准行
     * @return
     */
    private static List<JSONArray> standSpCol(JSONArray tableMatrix, int standLine) {
        List<JSONArray> list = new ArrayList<>();
        // 取标准行第一个值
        String standWord = tableMatrix.getJSONArray(standLine).get(0).toString();
        standWord = spValueProcess(standWord);
        // 切分列
        int colNum = 1;
        for (int j = 1; j < tableMatrix.getJSONArray(standLine).size(); j++) {
            int currentCol = j;
            if (standWord.equals(spValueProcess(tableMatrix.getJSONArray(standLine).get(j).toString()))) {
                JSONArray matrix = splitTableCol(tableMatrix, colNum, currentCol);
                list.add(matrix);
                colNum = 1;
            } else {
                colNum++;
            }
            if (j == tableMatrix.getJSONArray(standLine).size() - 1) {
                JSONArray matrix = splitTableCol(tableMatrix, colNum, currentCol + 1);
                list.add(matrix);
            }
        }
        return list;
    }

    /**
     * 切分表格
     *
     * @param tableMatrix
     * @param colNum      新表格列数
     * @param currentCol  原始表格当前起始列
     * @return
     */
    private static JSONArray splitTableCol(JSONArray tableMatrix, int colNum, int currentCol) {
        String[][] matrix = new String[tableMatrix.size()][colNum];
        int currnetNum = currentCol - colNum;
        for (int i = 0; i < tableMatrix.size(); i++) {
            for (int j = currnetNum; j < currentCol; j++) {
                matrix[i][j - currnetNum] = tableMatrix.getJSONArray(i).get(j).toString();
                if (i == 0 && j == currnetNum) {
                    // 特殊处理
                    // 第一行第一个值为空取第二个值
                    if (spValueProcess(tableMatrix.getJSONArray(i).get(j).toString()).equals("")) {
                        matrix[i][j - currnetNum] = tableMatrix.getJSONArray(i).get(j + 1).toString();
                    }
                    // 第一行第一个值与前一个值相同取后一个值，后一个值为空取再后一个值
                    if (currnetNum > 0) {
                        if (spValueProcess(tableMatrix.getJSONArray(i).get(j).toString()).equals(spValueProcess(tableMatrix.getJSONArray(i).get(j - 1).toString()))) {
                            matrix[i][j - currnetNum] = tableMatrix.getJSONArray(i).get(j + 1).toString();
                            if (j + 2 < tableMatrix.getJSONArray(i).size() && spValueProcess(tableMatrix.getJSONArray(i).get(j + 1).toString()).equals("")) {
                                matrix[i][j - currnetNum] = tableMatrix.getJSONArray(i).get(j + 2).toString();
                            }
                        }
                    }
                }
            }
        }
        return JSONArray.fromObject(matrix);
    }

    /**
     * 合并单元格（前两行）
     *
     * @param tableMatrix
     * @return
     */
    private static JSONArray mergeCells(JSONArray tableMatrix) {
        // 判断合并几行
        int mergeNum = 0;
        for (int r = 1; r < tableMatrix.size(); r++) {
            boolean flag = judgeRowHasNum(tableMatrix, r);
            if (!flag) {
                mergeNum++;
            } else {
                break;
            }
        }
        String[][] matrix = new String[tableMatrix.size() - mergeNum][tableMatrix.getJSONArray(0).size()];
        // 当前合并行
        int curMergeRow = 0;
        for (int i = 0; i < tableMatrix.size(); i++) {
            for (int j = 0; j < tableMatrix.getJSONArray(i).size(); j++) {
                if (i <= mergeNum && i >= 1) {
                    //合并到第一行
                    matrix[0][j] += "-" + tableMatrix.getJSONArray(curMergeRow).get(j).toString();
                } else if (i >= mergeNum + 1) {
                    // 正常存储
                    matrix[i - mergeNum][j] = tableMatrix.getJSONArray(i).get(j).toString();
                } else {
                    // i == 0
                    matrix[i][j] = tableMatrix.getJSONArray(i).get(j).toString();
                    // 第一行最后列空值处理
                    if (j == tableMatrix.getJSONArray(i).size() - 1) {
                        if (tableMatrix.getJSONArray(i).get(j).toString().equals("")) {
                            matrix[0][j] = matrix[0][j - 1];
                        }
                    }
                }
            }
            curMergeRow++;
        }
        return JSONArray.fromObject(matrix);
    }

    /**
     * 表格类型判断
     *
     * @param tableMatrix
     * @return
     */
    private static Integer tableTypeAnalysis(JSONArray tableMatrix) {
        Integer type = 0;
        /*表格类型：
         * 类型1:行等-列等表格
         * 项目, 决算数, 预算数
         * 一般, 20111, 2012
         * 税收, 20101, 2039
         *
         * 类型2:行等-列等表格、前两行合并
         * 地区, 收入数, | 支出数
         * 地区, 决算数, | 预算数
         * 一般, 20111, | 2012
         * 税收, 20101, | 2039
         *
         * 类型3: 行等-列等表格、前两行合并、重复多列
         * 收入, 收入  , 收入 | 支出, 支出 ,  支出
         * ___________________________________
         * 科目, 决算数, 预算数| 科目, 决算数, 预算数
         * ___________________________________
         * 一般, 20111, 20111| 非税, 2012, 2012
         * 税收, 20101, 20111| 税收, 2012, 2012
         *
         * 类型4: 行等-列等表格、重复多列
         * 序号, 项目, 决算数, 预算数 | 序号, 项目, 决算数, 预算数
         * 1  , 一般, 20111, 2012  | 3  , 一般, 20111, 2012
         * 2  , 税收, 20101, 2039  | 4  , 税收, 20101, 2039
         */
        // 前两行判断，是否合并；类型1和类型2
        String value11 = spValueProcess(tableMatrix.getJSONArray(0).get(0).toString());
        String value21 = spValueProcess(tableMatrix.getJSONArray(1).get(0).toString());
        if (!value11.equals("") && !value21.equals("")) {
            if (value11.equals(value21)) {
                type = 2;
            } else {
                // 判断第二行是否不存在数值
                int rowNum = 1;
                boolean isNum = judgeRowHasNum(tableMatrix, rowNum);
                if (isNum) {
                    // 第二行存在数值，不需合并单元格
                    type = 1;
                } else {
                    // 第二行不存在数值需合并单元格
                    // 判断第二行是否存在重复取列
                    rowNum = 1;
                    if (isRepCol(tableMatrix, rowNum)) {
                        type = 3;
                    } else {
                        type = 2;
                    }
                }
            }
        } else {
            // 判断第二行是否存在数值
            int rowNum = 2;
            if (!judgeRowHasNum(tableMatrix, rowNum)) {
                type = 2;
            } else {
                type = 1;
            }
        }

        // 判断第一行是否存在多列重复
        if (type == 1) {
            int rowNum = 0;
            if (isRepCol(tableMatrix, rowNum)) {
                type = 4;
            }
        }
        return type;
    }

    /**
     * 判断行是否存在数值
     * @param tableMatrix
     * @param rowNum
     * @return
     */
    private static boolean judgeRowHasNum(JSONArray tableMatrix, int rowNum) {
        boolean isNum = false;
        for (int j = 0; j < tableMatrix.getJSONArray(rowNum).size(); j++) {
            String string = spValueProcess(tableMatrix.getJSONArray(rowNum).get(j).toString());
            if (string.matches("^[-]{0,1}[0-9]+[.]{0,1}[0-9]*$")) {
                isNum = true;
            }
        }
        return isNum;
    }

    /**
     * 判断重复列，以给定行号为准
     *
     * @param tableMatrix
     * @param rowNum      行号
     */
    private static boolean isRepCol(JSONArray tableMatrix, int rowNum) {
        boolean isRep = false;
        List<String> tmpList1 = new ArrayList<>();
        List<String> tmpList2 = new ArrayList<>();
        String firstWord = tableMatrix.getJSONArray(rowNum).get(0).toString();
        firstWord = spValueProcess(firstWord);
        tmpList1.add(firstWord);
        boolean flag = false;
        for (int j = 1; j < tableMatrix.getJSONArray(rowNum).size(); j++) {
            String string = tableMatrix.getJSONArray(rowNum).get(j).toString();
            string = spValueProcess(string);
            //  切换存列信息
            if (!string.equals(firstWord)) {
                if (!flag) {
                    tmpList1.add(string);
                }
            } else {
                flag = true;
            }
            if (flag) {
                tmpList2.add(string);
            }
        }
        // 判断重复
        if (tmpList1.size() >= 2 && tmpList1.size() <= tmpList2.size()) {
            if (tmpList1.get(0).equals(tmpList2.get(0)) && tmpList1.get(1).equals(tmpList2.get(1))) {
                isRep = true;
            }
            if (tmpList2.size() >= 3) {
                // 重复列第二个为异常空值判断
                if (tmpList1.get(0).equals(tmpList2.get(0)) && tmpList1.get(1).equals(tmpList2.get(2))) {
                    isRep = true;
                }
            }
        }
        return isRep;
    }

    /**
     * 表格单位提取
     *
     * @param tableName
     * @return
     */
    private static String extractUnit(String tableName) {
        String unit = "";
        if (!"".equals(tableName) && tableName.contains("单位")) {
            String[] word = tableName.split("单位");
            unit = word[word.length - 1].substring(1).trim();
            unit = unit.split("元")[0] + "元";
        }
        return unit;
    }

    /**
     * 提取标准表格数据值
     *
     * @param tableMatrix
     * @param rowHeader
     * @param colHeader
     * @param unit
     * @return
     */
    private static List<String> extractData(JSONArray tableMatrix, String[] rowHeader, String[] colHeader, String unit) {
        List<String> dataList = new ArrayList<>();
        for (int i = 1; i < tableMatrix.size(); i++) {
            for (int j = 1; j < tableMatrix.getJSONArray(i).size(); j++) {
                String data = tableMatrix.getJSONArray(i).get(j).toString();
                String col = colHeader[j - 1];
                String row = rowHeader[i - 1];
                String unitNew = updateUnit(col, row);
                String string;
                if (!unitNew.equals("")) {
                    string = col + "," + row + "," + data + "," + unitNew;
                } else {
                    string = col + "," + row + "," + data + "," + unit;
                }

                dataList.add(string);
            }
        }
        return dataList;
    }

    /**
     * 异常字符处理
     *
     * @param str
     * @return
     */
    public static String spValueProcess(String str) {
        /*特殊字符*/
        str = str.trim().replaceAll("[ \t,\n ]", "");
        return str;
    }

    /**
     * 更新字段具体单位
     *
     * @param col
     * @param row
     * @return
     */
    private static String updateUnit(String col, String row) {
        String string = col + "," + row;
        String unit = "";
        if (!"".equals(string)) {
            String regex = "[(（]+.+[)）]+";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(string);
            String tmp;
            while (m.find()) {
                tmp = m.group().replaceAll("[()（）]", "");
                if (tmp.contains("%") || tmp.contains("元")) {
                    unit = tmp;
                }
            }
            if (string.contains("%")) {
                unit = "%";
            }
            if (string.contains("序号")
                    ||string.contains("排名")
                    || string.contains("排序")) {
                unit = "";
            }
        }
        return unit;
    }

    /**
     * 针对表格信息查找是否存在数据值
     *
     * @param tableMatrix
     * @return
     */
    private static Boolean findValue(JSONArray tableMatrix) {
        // 默认第一行第一列为行头和列头，故排除第一行第一列查找
        for (int i = 1; i < tableMatrix.size(); i++) {
            for (int j = 1; j < tableMatrix.getJSONArray(i).size(); j++) {
                String string = String.valueOf(tableMatrix.getJSONArray(i).get(j));
                if (isNumber(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断字符是否为数值
     *
     * @param string
     * @return
     */
    private static Boolean isNumber(String string) {
        string = string.trim();
        if (null == string) {
            return false;
        }
        String regex = "[-]{0,1}[0-9]+[.]{0,1}[0-9]*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        return m.find();
    }

    /**
     * 获取行表头信息
     *
     * @param tableMatrix
     * @return
     */
    private static String[] extractRowHeader(JSONArray tableMatrix) {
        // 判断首列是否为空值
        int rowNum = 0;
        if (tableMatrix.getJSONArray(1).get(0).toString().equals("")
                && tableMatrix.getJSONArray(2).get(0).toString().equals("")) {
            rowNum = 1;
        }
        String[] rowHeader = new String[tableMatrix.size()];
        for (int i = 1; i < tableMatrix.size(); i++) {
            String data = tableMatrix.getJSONArray(i).get(rowNum).toString();
            rowHeader[i - 1] = data.replaceAll("\n", "");
        }
        return rowHeader;
    }

    /**
     * 获取列表头信息
     *
     * @param tableMatrix
     * @return
     */
    private static String[] extractColHeader(JSONArray tableMatrix) {
        JSONArray colArray = tableMatrix.getJSONArray(0);
        String[] colHeader = new String[colArray.size()];
        for (int i = 1; i < colArray.size(); i++) {
            String data = colArray.get(i).toString();
            colHeader[i - 1] = data.replaceAll("\n", "");
        }
        return colHeader;
    }
}
