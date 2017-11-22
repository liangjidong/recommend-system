package test;

import common.PropertiesUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将mahout产生的频繁项集格式化
 *
 * @author ljd
 */
public class FrequentPatternsOut {
    public static void main(String[] args) throws FileNotFoundException {
        List<String> ans = new ArrayList<>();
        String pattern = "\\(\\[.*?\\]\\,.*?\\)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = null;
        BufferedReader br = null;
        System.setOut(new PrintStream(
                new FileOutputStream(PropertiesUtils.testOutPath + "userArray.txt")));
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(PropertiesUtils.testOutPath + "out2frequentpatterns.txt"),
                    "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                m = p.matcher(line);
                while (m.find()) {
                    System.out.println(m.group());
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
