package librec_test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 设法找最小支持度的合理值，这里取平均值
 * 
 * @author ljd
 *
 */
public class FindMeanValue {
	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(new FileInputStream(MiningTest.PATHDIR + "userArray.txt"), "UTF-8"));
			String line = null;
			int index = 0;
			double mean = 0;
			int temp = 0;
			int size = 0;
			int max = 0;
			// int[] times = new int[];
			List<Integer> list = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				index = line.lastIndexOf(',');
				temp = Integer.parseInt(line.substring(index + 1, line.length() - 1));
				mean += temp;
				size++;
				list.add(temp);
			}
			if (size != 0) {
				mean /= size;
				System.out.println("中值为：" + mean);
				Map<Integer, Integer> notRepeat = new HashMap<>();
				for (Integer integer : list) {
					if (notRepeat.containsKey(integer)) {
						notRepeat.put(integer, notRepeat.get(integer) + 1);
					} else {
						notRepeat.put(integer, 1);
					}
				}
				for (Entry<Integer, Integer> integer : notRepeat.entrySet()) {
					System.out.println(integer.getKey() + ":" + integer.getValue());
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
