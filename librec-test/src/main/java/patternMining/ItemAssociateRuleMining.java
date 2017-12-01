package patternMining;

import common.PropertiesUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ItemAssociateRuleMining {

    /**
     * 频繁项最小长度,小于这个长度的频繁项,丢弃
     */
    private int len = 2;

    public ItemAssociateRuleMining(int len) {
        this.len = len;
    }

    /**
     * 获取所有item对应的set 如对1号物品，<br/>
     * 1表示用户喜欢该物品1，则关联的项目集合为A=｛。。。｝<br/>
     * -1表是用户不喜欢物品1,则关联的项目集合为B=｛。。。｝
     *
     * @param filePath  mahout使用关联规则算法挖掘出来的频繁项
     * @return
     */
    public Map<Integer, HashMap<Integer, Integer>> getItemSet(String filePath) {
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
                if (split.length < len)
                    continue;
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


    public static void main(String[] args) throws Exception {
        // String s = "[asdf[[[[asdf]";
        //System.out.println(s.replace("[", "").replace("]", ""));
        Map<Integer, HashMap<Integer, Integer>> itemSet = new ItemAssociateRuleMining(2)
                .getItemSet(PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        System.out.println(itemSet.get(1));
        System.out.println(itemSet.get(-1));
    }
}
