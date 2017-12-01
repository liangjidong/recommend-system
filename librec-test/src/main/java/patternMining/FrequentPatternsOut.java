package patternMining;

import common.PropertiesUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将mahout产生的频繁项集格式化
 * 将frequentpatterns.txt-->userArray.txt-->userArrayFinally.txt
 *
 * @author ljd
 */
public class FrequentPatternsOut {
    public static void main(String[] args) throws IOException {
        frequentpatternsToUserArray();
        userArrayToUserArrayFinally();
    }

    /**
     * 将频繁模式提取出来,一行一个
     * @throws FileNotFoundException
     */
    private static void frequentpatternsToUserArray()  throws FileNotFoundException{
        PrintStream out = System.out;
        List<String> ans = new ArrayList<>();
        String pattern = "\\(\\[.*?\\]\\,.*?\\)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = null;
        BufferedReader br = null;
        System.setOut(new PrintStream(
                new FileOutputStream(PropertiesUtils.testOutPath + "userArray.txt")));
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(PropertiesUtils.testOutPath + "frequentpatterns.txt"),
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
        System.setOut(out);
    }

    /**
     * 将每一行转为下面的格式
     * 23, 192, -1|3
     */
    private static void userArrayToUserArrayFinally() throws IOException {
        PrintStream out = System.out;
        arrayOut(PropertiesUtils.testOutPath + "userArray.txt", PropertiesUtils.testOutPath + "userArrayFianlly.txt");
        System.setOut(out);
    }

    private static void arrayOut(String filePath, String outPath) throws IOException {
        BufferedReader br = null;
        System.setOut(new PrintStream(
                new FileOutputStream(outPath)));
        br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.replace("[", "").replace("]", "");
            int start = line.lastIndexOf(',');
            System.out.print(line.substring(line.indexOf('(') + 1, start));
            System.out.print("|");
            System.out.println(line.substring(start + 1, line.indexOf(')')));
        }
        br.close();

    }
}
