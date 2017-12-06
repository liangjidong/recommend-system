package librec_test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SplitData {
    public static void main(String[] args) throws IOException {
        String dir = "/home/ljd/testout/out_SML1M_2/";
        BufferedReader br = null;
        BufferedWriter trainWriter = null;
        BufferedWriter testWriter = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + "ratings.dat"), "UTF-8"));
            trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + "r1.train"), "UTF-8"));
            testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir + "r1.test"), "UTF-8"));
            String line = null;
            List<String> list = new ArrayList<>();
            int prev = 0;
            int now = 0;
            int count = 0, count1 = 0, count2 = 0;
            while ((line = br.readLine()) != null) {
                count++;
                now = Integer.parseInt(line.split(" ")[0]);
                if (prev == 0) {
                    prev = now;
                    list.add(line);
                } else {
                    if (prev == now) {
                        list.add(line);
                    } else {
                        // 先分1：4
                        int size1 = (int) Math.round(0.2 * list.size());
                        // System.out.println(size1 + ":" + (list.size() -
                        // size1));
                        for (int i = 0; i < size1; i++) {
                            count1++;
                            testWriter.write(list.get(i));
                            testWriter.newLine();
                        }
                        for (int i = size1; i < list.size(); i++) {
                            count2++;
                            trainWriter.write(list.get(i));
                            trainWriter.newLine();
                        }
                        list.clear();
                        list.add(line);
                        prev = now;
                    }
                }

            }
            if (!list.isEmpty()) {
                int size1 = (int) Math.round(0.2 * list.size());
                for (int i = 0; i < size1; i++) {
                    count1++;
                    testWriter.write(list.get(i));
                    testWriter.newLine();
                }
                for (int i = size1; i < list.size(); i++) {
                    count2++;
                    trainWriter.write(list.get(i));
                    trainWriter.newLine();
                }
            }
            System.out.println(count + ":" + count1 + ":" + count2);

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            br.close();
            trainWriter.close();
            testWriter.close();

        }
    }
}
