package patternMining;

import com.google.common.collect.BiMap;
import common.PropertiesUtils;
import net.librec.data.DataModel;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ItemAssociateRuleMining {
    /**
     * 获取所有item对应的set 如对1号物品，<br/>
     * 1表示用户喜欢该物品1，则关联的项目集合为A=｛。。。｝<br/>
     * -1表是用户不喜欢物品1,则关联的项目集合为B=｛。。。｝
     *
     * @param filePath  mahout使用关联规则算法挖掘出来的频繁项
     * @param dataModel 数据集，包含了外部id和内部id的映射
     * @return
     */
    public Map<Integer, HashMap<Integer, Integer>> getItemSet(String filePath, DataModel dataModel) {
        if (dataModel == null) return null;
        BufferedReader br = null;
        Map<Integer, HashMap<Integer, Integer>> ans = new HashMap<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line = null;
            String[] split;
            Integer[] keys;
            Integer key;
            HashMap<Integer, Integer> tempMap;
            while ((line = br.readLine()) != null) {
                int indexOf = line.indexOf('|');
                Integer times = Integer.parseInt(line.substring(indexOf + 1));
                split = line.substring(0, indexOf).split(",");
                keys = new Integer[split.length];
                for (int i = 0; i < split.length; i++) {
                    keys[i] = Integer.parseInt(split[i].trim());
                }
                for (int i = 0; i < keys.length; i++) {
                    key = keys[i];
                    key = changeOutIdToInnerId(key, dataModel);
                    tempMap = ans.get(key);
                    if (tempMap == null) {
                        tempMap = new HashMap<>();
                        ans.put(key, tempMap);
                    }
                    for (int j = 0; j < keys.length; j++) {
                        key = keys[j];
                        key = changeOutIdToInnerId(key, dataModel);
                        if (tempMap.get(key) == null || tempMap.get(key) < times) {
                            tempMap.put(key, times);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            // TODO: handle finally clause
            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("流关闭失败");
                e.printStackTrace();
            }
        }
        return ans;
    }

    /**
     * 没有进行外部id转换为内部id的方法
     *
     * @param filePath
     * @return
     */
    @Deprecated
    private Map<Integer, HashMap<Integer, Integer>> getItemSet(String filePath) {
        BufferedReader br = null;
        Map<Integer, HashMap<Integer, Integer>> ans = new HashMap<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line = null;
            String[] split;
            Integer[] keys;
            Integer key;
            HashMap<Integer, Integer> tempMap;
            while ((line = br.readLine()) != null) {
                int indexOf = line.indexOf('|');
                Integer times = Integer.parseInt(line.substring(indexOf + 1));
                split = line.substring(0, indexOf).split(",");
                keys = new Integer[split.length];
                for (int i = 0; i < split.length; i++) {
                    keys[i] = Integer.parseInt(split[i].trim());
                }
                for (int i = 0; i < keys.length; i++) {
                    key = keys[i];
                    tempMap = ans.get(key);
                    if (tempMap == null) {
                        tempMap = new HashMap<>();
                        ans.put(key, tempMap);
                    }
                    for (int j = 0; j < keys.length; j++) {
                        key = keys[j];
                        if (tempMap.get(key) == null) {
                            tempMap.put(key, times);
                        } else {
                            if (tempMap.get(key) < times)
                                tempMap.put(key, times);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            // TODO: handle finally clause
            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("流关闭失败");
                e.printStackTrace();
            }
        }
        return ans;
    }

    private static void arrayOut(String filePath, String outPath) throws Exception {
        BufferedReader br = null;
        System.setOut(new PrintStream(
                new FileOutputStream(outPath)));
        // br = new BufferedReader(new InputStreamReader(
        // new
        // FileInputStream("C:\\Users\\ljd\\Desktop\\论文相关--开题报告--梁继东\\testout\\out2frequentpatterns.txt"),
        // "UTF-8"));
        br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            int start = line.indexOf('[');
            int end = line.indexOf(']');
            System.out.print(line.substring(start + 1, end));
            System.out.print("|");
            start = line.lastIndexOf(',');
            end = line.indexOf(')');
            System.out.println(line.substring(start + 1, end));
        }
        br.close();

    }

    /**
     * 外部id转为内部id
     */
    public int changeOutIdToInnerId(int id, DataModel dataModel) {
        BiMap<String, Integer> itemMappingData = dataModel.getItemMappingData();
        id = itemMappingData.get(id + "");
        return id;
    }

    public static void main(String[] args) throws Exception {
        PrintStream out = System.out;
        arrayOut(PropertiesUtils.testOutPath + "userArray.txt", PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        System.setOut(out);
        Map<Integer, HashMap<Integer, Integer>> itemSet = new ItemAssociateRuleMining()
                .getItemSet(PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        // System.setOut(new PrintStream(
        // new
        // FileOutputStream("C:\\Users\\ljd\\Desktop\\论文相关--开题报告--梁继东\\testout\\itemSet.txt")));
        System.out.println(itemSet.get(1));
        System.out.println(itemSet.get(-1));
    }
}
