package librec_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ExtractData {
	private static final String PARENT_PATH = "C:\\Users\\ljd\\Desktop\\论文相关--开题报告--梁继东\\data\\";

	public static void main(String[] args) throws IOException {
		// 打高分，低分为0.45，中间的分数为0.1
		int high = (int) (0.45 * 100000), low = (int) (0.45 * 100000), mid = (int) (0.1 * 100000);
		int a = 0, b = 0, c = 0;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(PARENT_PATH + "orginal_ratings.dat"), "UTF-8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PARENT_PATH + "ratings.dat"), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(" ");
				// System.out.println(strs[2]);
				double d = Double.parseDouble(strs[2]);
				if (d >= 4 && a < high) {
					bw.write(line);
					bw.newLine();
					a++;
				}
				if (d > 2 && d < 4 && b < mid) {
					bw.write(line);
					bw.newLine();
					b++;
				}
				if (d <= 2 && c < low) {
					bw.write(line);
					bw.newLine();
					c++;
				}
				if (a >= high && b >= mid && c >= low)
					break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			br.close();
			bw.close();
		}
	}
}
