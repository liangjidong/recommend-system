package librec_test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserItemCount {
	public static void main(String[] args) throws IOException {
		String dir = "C:\\Users\\ljd\\Desktop\\论文相关--开题报告--梁继东\\data\\";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + "ratings.dat"), "UTF-8"));
			String line = null;
			Set<Integer> users = new HashSet<>();
			Set<Integer> items = new HashSet<>();
			while ((line = br.readLine()) != null) {
				String[] split = line.split(" ");
				users.add(Integer.parseInt(split[0]));
				items.add(Integer.parseInt(split[1]));
			}
			System.out.println(users.size() + ":" + items.size());

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			br.close();
		}
	}
}
