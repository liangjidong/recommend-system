package librec_test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于从ML1M中筛选出特定比例数据集Data_EXTRACT
 */
public class ExtractData {
    private static final String PARENT_PATH = "/home/ljd/testout/out_SML1M_2/";

    public static void main(String[] args) throws IOException {
        // 打高分，低分为0.45，中间的分数为0.1
        int high = (int) (0.4 * 100000), low = (int) (0.4 * 100000), mid = (int) (0.2 * 100000);
        int total = 100000;
        int a = 0, b = 0, c = 0;
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(PARENT_PATH + "orginal_ratings.dat"), "UTF-8"));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PARENT_PATH + "ratings.dat"), "UTF-8"));
            String line = null;
            String[] data = new String[1000209];
            //存放已经打分的user,item编号
            Set<Integer> users = new HashSet<>(2048);
            Set<Integer> items = new HashSet<>(2048);
            int index = 0;
            while ((line = br.readLine()) != null) {
                data[index++] = line;
            }
            br.close();
            index = 0;
            int randomNum;
            while (index < total) {
                randomNum = randomNum(0, 1000209);
                line = data[randomNum];
                String[] strs = line.split(" ");
                // System.out.println(strs[2]);
                int userId = Integer.parseInt(strs[0]);
                int itemId = Integer.parseInt(strs[1]);
                if (users.size() >= 1355 && !users.contains(userId) || items.size() >= 2248 && !items.contains(itemId)) {
                    continue;
                }
                double d = Double.parseDouble(strs[2]);
                if (d >= 4 && a < high) {
                    bw.write(line);
                    bw.newLine();
                    a++;
                    users.add(userId);
                    items.add(itemId);
                } else if (d > 2 && d < 4 && b < mid) {
                    bw.write(line);
                    bw.newLine();
                    b++;
                    users.add(userId);
                    items.add(itemId);
                } else if (d <= 2 && c < low) {
                    bw.write(line);
                    bw.newLine();
                    c++;
                    users.add(userId);
                    items.add(itemId);
                }
                if (a >= high && b >= mid && c >= low) {
                    break;
                }
            }


        } catch (
                Exception e)

        {
            // TODO: handle exception
        } finally

        {
            br.close();
            bw.close();
        }

    }

    private static Set<Integer> randomSet = new HashSet<>();

    private static int randomNum(int min, int max) {
        int num = (int) (Math.random() * (max - min) + min);
        while (randomSet.contains(num)) {
            num = (int) (Math.random() * (max - min) + min);
        }
        randomSet.add(num);
        return num;
    }
}
