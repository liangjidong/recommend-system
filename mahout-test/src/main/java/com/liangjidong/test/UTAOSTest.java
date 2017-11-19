package com.liangjidong.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

public class UTAOSTest {
	public static void main(String[] args) throws Exception {
		String projectDir = System.getProperty("user.dir");
		DataModel model = new FileDataModel(new File(projectDir + "/src/main/myMatrix.base"));
		FastByIDMap<PreferenceArray> preferences = new FastByIDMap<PreferenceArray>();
		LongPrimitiveIterator userIDs = model.getUserIDs();
		long userId;
		PreferenceArray preferencesFromUser;
		PreferenceArray prefs;
		int count = 0;
		while (userIDs.hasNext()) {
			userId = userIDs.nextLong();
			count = 0;

			preferencesFromUser = model.getPreferencesFromUser(userId);
			// 首次遍历，获取>3的数量
			Iterator<Preference> iterator = preferencesFromUser.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getValue() > 3)
					count++;
			}
			prefs = new GenericUserPreferenceArray(count);
			// 再遍历一次
			iterator = preferencesFromUser.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				Preference next = iterator.next();
				if (next.getValue() > 3) {
					prefs.setUserID(i, userId);
					prefs.setItemID(i, next.getItemID());
					prefs.setValue(i, 1);
				}
				i++;
			}
			preferences.put(userId, prefs);
		}
		DataModel newModel = new GenericDataModel(preferences);
		userIDs = newModel.getUserIDs();
		while (userIDs.hasNext()) {
			userId = userIDs.nextLong();
			PreferenceArray preferencesFromUser2 = newModel.getPreferencesFromUser(userId);
			Iterator<Preference> iterator = preferencesFromUser2.iterator();
			while (iterator.hasNext()) {
				Preference next = iterator.next();
				System.out.println(next.getUserID() + "  " + next.getItemID() + "  " + next.getValue());
			}
		}

		System.out.println(model.toString());
	}
}
