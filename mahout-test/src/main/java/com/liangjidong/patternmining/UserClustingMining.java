package com.liangjidong.patternmining;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;

import com.mysql.fabric.xmlrpc.base.Array;

/**
 * 根据给定的数据集，对用户进行分组挖掘
 * 
 * @author ljd
 *
 */
public class UserClustingMining {
	// 最终的分组结果
	private List<List<Long>> users;
	// 初始的数据集
	private DataModel dataModel;
	// 用户id数组，便于后续的使用
	private long[] userIDArray;

	private double avgRatingItems;

	public UserClustingMining(DataModel dataModel) {
		super();
		this.dataModel = dataModel;

	}

	private void generateUserIDs() throws TasteException {
		LongPrimitiveIterator userIDs = dataModel.getUserIDs();
		avgRatingItems = 0;
		userIDArray = new long[dataModel.getNumUsers()];
		int index = 0;
		long nextLong;
		while (userIDs.hasNext()) {
			nextLong = userIDs.nextLong();
			userIDArray[index++] = nextLong;
			// avgRatingItems += dataModel.getItemIDsFromUser(nextLong).size();
		}
		// avgRatingItems = avgRatingItems / index;
	}

	public List<List<Long>> getUsers() {
		return users;
	}

	/**
	 * 1,获取用户感兴趣的物品 评分1,2 --> -i 评分3,4,5--->i <br/>
	 * 并且后续求交集以及用户分组都需要用到该步骤的结果
	 * 
	 * @return
	 * @throws TasteException
	 */
	private List<long[]> getListOfInterestingItems() throws TasteException {
		List<long[]> ans = new ArrayList<>();
		// 获取所有用户
		LongPrimitiveIterator userIDs = dataModel.getUserIDs();
		FastIDSet set = null;
		long currentUser = 0;
		while (userIDs.hasNext()) {
			currentUser = userIDs.next();
			// 获取当前用户的打分项，并遍历得出转换后的结果
			set = dataModel.getItemIDsFromUser(currentUser);
			long[] interests = new long[set.size()];
			Iterator<Long> iterator = set.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				long type = (long) iterator.next();
				float pref = dataModel.getPreferenceValue(currentUser, type);
				if (pref >= 3)
					interests[i++] = type;
				else
					interests[i++] = -type;
			}
			// 将数组从小到大排序
			Arrays.sort(interests);
			ans.add(interests);
		}
		return ans;
	}

	/**
	 * 2，求交集并将交集进行求包
	 * 
	 * @param interests
	 * @return
	 */
	private List<List<Long>> findSubspaceAndDetect(List<long[]> interests) {
		List<List<Long>> beforeDetect = new ArrayList<>();
		List<Long> temp;
		// 求交集
		for (int i = 1; i < interests.size(); i++) {
			for (int j = 0; j < i; j++) {
				temp = getSubspace(interests.get(i), interests.get(j));
				if (temp.size() != 0) {
					beforeDetect.add(temp);
					System.out.println(temp);
				}
			}
		}
		// detect，进行子集合并，只保留最大范围的集合
		// 首先进行list长度从大到小排序
		Collections.sort(beforeDetect, new Comparator<List<Long>>() {
			@Override
			public int compare(List<Long> o1, List<Long> o2) {
				// TODO Auto-generated method stub
				return o2.size() - o1.size();
			}
		});
		// 子集合并
		List<List<Long>> afterDetect = new ArrayList<>();
		boolean isContain = false;
		for (List<Long> list : beforeDetect) {
			isContain = false;
			for (List<Long> list2 : afterDetect) {
				if (subspaceDetact(list2, list) == 1) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				afterDetect.add(list);
				System.out.println("afterDetect增加：" + list);
			}

		}
		return afterDetect;
	}

	private void getUsers(List<long[]> interests, List<List<Long>> afterDetect) throws TasteException {
		// 对每个long[]，对比是否包含afterDetect,可以肯定的是，结果的大小与afterDetect的大小相同
		users = new ArrayList<>();
		List<Long> temp;
		long[] ls;
		// 先产生用户id保存到数组中,以便后续使用
		generateUserIDs();
		for (List<Long> list : afterDetect) {
			temp = new ArrayList<>();
			for (int i = 0; i < interests.size(); i++) {
				ls = interests.get(i);
				if (isContains(ls, list)) {
					temp.add(userIDArray[i]);
				}
			}
			users.add(temp);
		}
	}

	/**
	 * 获取子集
	 * 
	 * @param u1
	 * @param u2
	 * @return
	 */
	private List<Long> getSubspace(long[] u1, long[] u2) {
		List<Long> ans = new ArrayList<>();
		int i = 0, j = 0;
		while (i < u1.length && j < u2.length) {
			if (u1[i] == u2[j]) {
				ans.add(u1[i]);
				i++;
				j++;
			} else if (u1[i] < u2[j]) {
				i++;
			} else {
				j++;
			}
		}
		return ans;
	}

	/**
	 * 判断两个集合是否具有包含关系<br/>
	 * 返回1，表示s1包含s2，<br/>
	 * 返回0，表示不具有包含关系 <br/>
	 * 返回-1，s2包含s1
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int subspaceDetact(List<Long> s1, List<Long> s2) {
		boolean isChange = false;
		int ans = 0;
		if (s1.size() < s2.size()) {
			isChange = true;
			List<Long> temp = s1;
			s1 = s2;
			s2 = temp;
		}
		for (Long long1 : s2) {
			if (!s1.contains(long1))
				return 0;
		}
		return isChange ? -1 : 1;
	}

	/**
	 * 判断用户是否包含该兴趣
	 * 
	 * @param userInterest
	 * @param part
	 * @return
	 */
	private boolean isContains(long[] userInterest, List<Long> part) {
		if (userInterest.length < part.size())
			return false;
		int i = 0, j = 0;
		while (i < userInterest.length && j < part.size()) {
			if (userInterest[i] != part.get(j)) {
				i++;
			} else {
				i++;
				j++;
			}
		}
		if (j != part.size())
			return false;
		return true;
	}

	/**
	 * 使用一条命令获取所有的用户模式挖掘结果<br/>
	 * 该步骤完成后，调用 getUsers就可以获取最终挖掘结果
	 * 
	 * @throws TasteException
	 */
	public void command() throws TasteException {
		List<long[]> listOfInterestingItems = getListOfInterestingItems();
		for (long[] ls : listOfInterestingItems) {
			System.out.println(Arrays.toString(ls));
		}
		System.out.println("----------------------");
		List<List<Long>> subspaceAndDetect = findSubspaceAndDetect(listOfInterestingItems);
		for (List<Long> list : subspaceAndDetect) {
			System.out.println(list);
		}
		System.out.println("----------------------");
		getUsers(listOfInterestingItems, subspaceAndDetect);
		for (List<Long> list : users) {
			System.out.println(list);
		}
	}

	public static void main(String[] args) throws IOException, TasteException {
		String projectDir = System.getProperty("user.dir");
		DataModel trainingData = new FileDataModel(new File(projectDir + "/src/main/u.data"));
		UserClustingMining ucm = new UserClustingMining(trainingData);
		List<long[]> listOfInterestingItems = ucm.getListOfInterestingItems();
		for (long[] ls : listOfInterestingItems) {
			System.out.println(Arrays.toString(ls));
		}
		System.out.println("----------------------");
		List<List<Long>> subspaceAndDetect = ucm.findSubspaceAndDetect(listOfInterestingItems);
		for (List<Long> list : subspaceAndDetect) {
			System.out.println(list);
		}
		System.out.println("----------------------");
		ucm.getUsers(listOfInterestingItems, subspaceAndDetect);
		for (List<Long> list : ucm.users) {
			System.out.println(list);
		}
	}
}
