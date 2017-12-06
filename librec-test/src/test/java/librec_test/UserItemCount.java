package librec_test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class UserItemCount {
    public static void main(String[] args) throws IOException {
        String dir = "/home/ljd/testout/out_SML1M_2/";
        BufferedReader br = null;
        int total = 100000;
        int a = 0, b = 0, c = 0;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + "ratings.dat"), "UTF-8"));
            String line = null;
            Set<Integer> users = new HashSet<>();
            Set<Integer> items = new HashSet<>();
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ");
                users.add(Integer.parseInt(split[0]));
                items.add(Integer.parseInt(split[1]));
                count++;
                double d = Double.parseDouble(split[2]);
                if (d >= 4) {
                    a++;
                } else if (d > 2 && d < 4) {
                    b++;
                } else if (d <= 2) {
                    c++;
                }

            }
            System.out.println(users.size() + ":" + items.size());
            System.out.println(count);
            System.out.println(a * 1.0 / total);
            System.out.println(b * 1.0 / total);
            System.out.println(c * 1.0 / total);

        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            br.close();
        }
    }
}
