package com.zy.alg.infoextra.htmlanalysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.json.JSONArray;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangycqupt@163.com
 * @date 2019/03/06 17:21
 */
public class TableParser {
    /*private static Logger logger = LoggerFactory.getLogger(TableParser.class);*/
    private static Log logger = LogFactory.getLog(TableParser.class);

    /**
     * 主函数1：仅提取html文件表格信息，不进行列<行，列，值，单位>的对应解析，输出含表格矩阵的json格式
     *
     * @param file html file
     * @return
     */
    public static String tableAnalysys(File file) {
        try {
            Document doc = Jsoup.parse(file, "utf-8");
            Elements elements = doc.getElementsByTag("table");
            List<TableInfo> list = new ArrayList<>();
            // 表格有效性判断
            Boolean isInvalid = true;
            for (int i = 0; i < elements.size(); i++) {
                int index = i;
                Element tableTag = elements.get(i);
                // search table header
                String describe = "";
                if (isInvalid) {
                    describe = searchTableDecribe(tableTag);
                } else {
                    Element tableTagOld = tableTag;
                    while (true) {
                        Element tableTagNew = tableTagOld.previousElementSibling();
                        if (tableTagNew == null) {
                            break;
                        } else {
                            describe = tableTagNew.text();
                            if (!describe.equals("")) {
                                break;
                            } else {
                                tableTagOld = tableTagNew;
                            }
                        }
                    }
                    isInvalid = true;
                }
                // search table col and table row
                String tableColRow = searchTableBaseInfo(tableTag);
                // judge table invalid
                String[] seg = tableColRow.split("#");
                if (seg.length != 3) {
                    continue;
                }
                int tableCol = Integer.parseInt(seg[0]);
                int tableRow = Integer.parseInt(seg[1]);
                int invalid = Integer.parseInt(seg[2]);
                String[][] strMatrix;
                if (invalid == 0) {
                    logger.info("find a invalid table tag, continue...");
                    isInvalid = false;
                    continue;
                } else {
                    strMatrix = generateTableMatrix(tableTag, tableCol, tableRow);
                    if (strMatrix == null) {
                        continue;
                    }
                }
                // reload table info
                TableInfo table = new TableInfo();
                table.setDescribe(describe);
                table.setTableIndex(index);
                table.setMatrix(strMatrix);
                list.add(table);
            }
            // build string json data and return
            JSONArray jsonArray = JSONArray.fromObject(list);
            String json = "{" + "\n" + "\"data\":" + "\n" + jsonArray.toString() + "\n" + "}";
            return json;
        } catch (IOException e) {
            logger.error("table parser failed. " + e);
            return null;
        }
    }

    /**
     * 主函数2：html文件提取table，并解析table<行，列，值，单位>信息，输出txt
     *
     * @param filePath
     * @return
     */
    public static List<String> getTableTxt(String filePath) {
        List<String> tableList = new ArrayList<>();
        File file = new File(filePath);
        try {
            Document doc = Jsoup.parse(file, "utf-8");
            Elements elements = doc.getElementsByTag("table");
            int tableIndex = 0;
            boolean isInvalid = true;
            String lastTableName = "";
            for (Element element : elements) {
                tableIndex++;
                TableInfo tableInfo = getTableInfo(element, tableIndex, isInvalid);
                isInvalid = tableInfo.getLastInvalid();
                if (tableInfo.getMatrix() == null) {
                    continue;
                }
                if ("连续表".equals(tableInfo.getDescribe())) {
                    tableInfo.setDescribe(lastTableName);
                } else {
                    lastTableName = tableInfo.getDescribe();
                }
                List<TableInfo> list = new ArrayList<>();
                list.add(tableInfo);
                JSONArray jsonArray = JSONArray.fromObject(list);
                // 此情况仅一个表格
                String json = "{" + "\n" + "\"data\":" + "\n" + jsonArray.toString() + "\n" + "}";
                // 横表、竖表判断，提取字段<行，列，值，单位>
                String result = getTableParserInfo(json);
                result = tableIndex + "#表名：" + tableInfo.getDescribe() + "\n" + result;
                tableList.add(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableList;
    }

    /**
     * 获取表格矩阵
     *
     * @param tableTag
     * @param tableCol
     * @param tableRow
     * @return
     */
    private static String[][] generateTableMatrix(Element tableTag, int tableCol, int tableRow) {
        String[][] strMatrix;
        try {
            strMatrix = new String[tableRow][tableCol];
            Elements tr = tableTag.getElementsByTag("tr");
            for (int i = tr.size() - tableRow; i < tr.size(); i++) {
                int rowIndex = i - (tr.size() - tableRow);
                Elements td = getColElementsByTag(tr.get(i));
                for (int j = 0; j < td.size(); j++) {
                    int colIndex = j;
                    int wide = 0;
                    int height = 0;
                    String des = JsonParser.spValueProcess(td.get(j).text());
                    for (int k = 0; k < tableCol - colIndex; k++) {
                        if (colIndex + k >= tableCol) {
                            return null;
                        }
                        if (strMatrix[rowIndex][colIndex + k] == null) {
                            strMatrix[rowIndex][colIndex + k] = des;
                            // 横向重定位
                            colIndex = colIndex + k;
                            break;
                        }
                    }
                    if (td.get(j).getElementsByAttribute("rowspan").size() != 0) {
                        height = Integer.parseInt(td.get(j).getElementsByAttribute("rowspan").get(0).attr("rowspan"));
                    }
                    if (td.get(j).getElementsByAttribute("colspan").size() != 0) {
                        wide = Integer.parseInt(td.get(j).getElementsByAttribute("colspan").get(0).attr("colspan"));
                    }
                    if (wide != 0 && height != 0) {
                        for (int m = 0; m < height; m++) {
                            for (int n = 0; n < wide; n++) {
                                if (colIndex + n >= tableCol) {
                                    return null;
                                }
                                strMatrix[rowIndex + m][colIndex + n] = des;
                            }
                        }
                    } else if (wide != 0 || height != 0) {
                        if (wide != 0) {
                            for (int k = 0; k < wide; k++) {
                                if (colIndex + k >= tableCol) {
                                    return null;
                                }
                                strMatrix[rowIndex][colIndex + k] = des;
                            }
                        } else if (height != 0) {
                            for (int k = 0; k < height; k++) {
                                if (colIndex >= tableCol) {
                                    return null;
                                }
                                strMatrix[rowIndex + k][colIndex] = des;
                            }
                        }
                    }
                }
            }
            return strMatrix;
        } catch (Exception e) {
            logger.error("generate table matrix failed. " + e);
            return null;
        }
    }

    /**
     * 计算表格基础信息
     *
     * @param tableTag
     * @return
     */
    private static String searchTableBaseInfo(Element tableTag) {
        Elements trHead = tableTag.getElementsByTag("tr");
        // 表格行数
        int tableRow = trHead.size();
        // 表格列数
        int tableCol = 0;
        // 表格有效性
        int invalid = 1;
        // 表格格式判断
        if (tableRow <= 1) {
            /*无效表格：零行或一行表格*/
            return tableCol + "#" + tableRow + "#" + invalid;
        } else {
            // 判断表格第一行第一列字串长度，判断表名是否在表第一行
            boolean flag = tableNameIsInFirstRow(trHead);
            // 计算表格行数、列数
            int startRow = 0;
            if (flag) {
                startRow = 1;
            }
            float emptyHead = 0;
            for (int i = startRow; i < tableRow; i++) {
                Elements td = getColElementsByTag(trHead.get(i));
                for (int j = 0; j < td.size(); j++) {
                    String tdStr = JsonParser.spValueProcess(td.get(j).text());
                    if (tdStr.equals("")) {
                        emptyHead++;
                    }
                    Elements colElement = td.get(j).getElementsByAttribute("colspan");
                    if (colElement.size() != 0) {
                        int tmpNum = Integer.parseInt(colElement.get(0).attr("colspan"));
                        tableCol += tmpNum;
                    } else {
                        tableCol++;
                    }
                }
                //执行一次
                break;
            }
            // 排除第一行
            if (flag) {
                tableCol--;
            }
            // 判断表格有效性
            if (emptyHead / tableCol > 0.8 || tableCol < 1 || tableRow < 2) {
                invalid = 0;
            }
            return tableCol + "#" + tableRow + "#" + invalid;
        }
    }

    /**
     * 判断表格第一行是否包含表名
     * @param trHead
     * @return
     */
    private static boolean tableNameIsInFirstRow(Elements trHead) {
        boolean flag = false;
        Elements td = getColElementsByTag(trHead.get(0));
        if (td.size() > 0) {
            String firstWord = JsonParser.spValueProcess(td.get(0).text());
            // 首行列数限制
            int colMax = 30;
            // 首行首列字数限制
            int wordLen = 25;
            if (firstWord.length() > wordLen
                    || firstWord.contains("单位:")
                    || firstWord.contains("单位：")
                    || trHead.get(0).getElementsByTag("td").size() > colMax) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 搜索表格标签描述
     * 搜索策略：搜索text_align属性，有text_align属性搜索到非text_algin为止；
     * 如果为段落，进行分句，取最后一个或多个句子；需要判断tag是否有效
     *
     * @param tableTag
     * @return
     */
    private static String searchTableDecribe(Element tableTag) {
        String des = "";
        try {
            while (true) {
                Element tableTagNew = tableTag.previousElementSibling();
                Boolean isCenter = false;
                if (tableTagNew != null) {
                    String nodeName = tableTagNew.nodeName();
                    // 连续表判断
                    if (nodeName.equals("table")) {
                        des = "连续表";
                        break;
                    }
                    String alginValue = tableTagNew.attr("algin");
                    if (alginValue.equals("center")) {
                        isCenter = true;
                        try {
                            int tmp = Integer.parseInt(tableTagNew.text().trim());
                            isCenter = false;
                        } catch (Exception e) {
                            des = tableTagNew.text() + des;
                        }
                        tableTag = tableTagNew;
                        continue;
                    } else if (isCenter) {
                        break;
                    }
                    des = tableTagNew.text() + des;
                    // 分句检查
                    if (checkSentence(des)) {
                        String[] seg = des.split("[,，.。 ]");
                        des = seg[seg.length - 1];
                        break;
                    }
                } else {
                    break;
                }
                tableTag = tableTagNew;
            }
        } catch (Exception e) {
            logger.error("search table describe failed. " + e);
        }
        return des.replaceAll("\n", "");
    }

    /**
     * 判断句子完整性
     *
     * @param des
     * @return
     */
    private static boolean checkSentence(String des) {
        String[] seg = des.split("[、，。,.]");
        if (seg.length >= 2) {
            return true;
        }
        return false;
    }

    /**
     * 提取html文件表格信息，进行列<行，列，值，单位>的对应解析,输出txt
     *
     * @param json
     * @return
     */
    private static String getTableParserInfo(String json) {
        return JsonParser.getTableData(json);
    }

    /**
     * @param element
     * @param index
     * @param isInvalid
     * @return
     */
    private static TableInfo getTableInfo(Element element, int index, Boolean isInvalid) {
        TableInfo tableInfo = new TableInfo();
        // 表格描述提取
        /*表格前信息判断*/
        String describe = "";
        if (isInvalid) {
            describe = searchTableDecribe(element);
        } else {
            Element tableTag1 = element;
            while (true) {
                Element tableTagNew = tableTag1.previousElementSibling();
                if (tableTagNew == null) {
                    break;
                } else {
                    describe = tableTagNew.text();
                    if (!describe.equals("")) {
                        break;
                    } else {
                        tableTag1 = tableTagNew;
                    }
                }
            }
        }
        /*表格中信息判断*/
        describe = updateDescreInTable(element, describe);
        // 更新表格有效性
        isInvalid = true;
        // 表格基础信息提取
        String tableColRow = searchTableBaseInfo(element);
        String[] seg = tableColRow.split("#");
        int tableCol = Integer.parseInt(seg[0]);
        int tableRow = Integer.parseInt(seg[1]);
        int invalid = Integer.parseInt(seg[2]);
        String[][] strMatrix = null;
        if (invalid == 0) {
            logger.info(index + "# find a invalid table tag ...");
            isInvalid = false;
        } else {
            strMatrix = generateTableMatrix(element, tableCol, tableRow);
        }
        tableInfo.setDescribe(describe);
        tableInfo.setTableIndex(index);
        tableInfo.setMatrix(strMatrix);
        tableInfo.setLastInvalid(isInvalid);

        return tableInfo;
    }

    /**
     * 表格信息中提取描述
     *
     * @param element
     * @param describe
     * @return
     */
    private static String updateDescreInTable(Element element, String describe) {
        // 获取首行首列字段
        Element firstRow = element.getElementsByTag("tr").get(0);
        Elements tdHead = getColElementsByTag(firstRow);
        if (tdHead.size() > 0) {
            String word = tdHead.get(0).text().trim();
            if (word.contains("单位") && !describe.contains("单位")) {
                String[] tmp = word.split("单位");
                if (tmp[1].length() >= 5) {
                    word = tmp[0] + "单位" + tmp[1].substring(0, 5);
                }
                return word;
            }
        }
        return describe;
    }

    /**
     * 获取每行表格列元素：包括表格列标签-td、th
     * @param trElement
     * @return
     */
    private static Elements getColElementsByTag(Element trElement) {
        Elements tdElements = trElement.getElementsByTag("td");
        if (tdElements.size() == 0) {
            tdElements = trElement.getElementsByTag("th");
        }
        return tdElements;
    }
}
