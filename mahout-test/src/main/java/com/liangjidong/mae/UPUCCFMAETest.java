package com.liangjidong.mae;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.liangjidong.common.UserClustering;
import com.liangjidong.neighborhood.OPNNearestNUserNeighborhood;
import com.liangjidong.similarity.PCCSimiliarity;
import com.liangjidong.similarity.UPSSimiliarity;

public class UPUCCFMAETest extends AbstractMAETest {
	public static void main(String[] args) throws IOException {
		String projectDir = System.getProperty("user.dir");
		DataModel trainingData = new FileDataModel(new File(projectDir + "/src/main/u1.base"));
		DataModel testData = new FileDataModel(new File(projectDir + "/src/main/u1.test"));
		UPUCCFMAETest test = new UPUCCFMAETest(trainingData, testData);
		// 分组为10时 MAE=0.7849556792604714
		System.out.println(test.getMAE(10));
	}

	public UPUCCFMAETest(DataModel trainingData, DataModel testData) {
		super(trainingData, testData);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected UserSimilarity setSimilarityByYourself() {
		// TODO Auto-generated method stub
		UserSimilarity similarity = new UPSSimiliarity(getTrainingData());
		this.setSimilarity(similarity);
		return similarity;
	}

	@Override
	protected UserNeighborhood setNeighborhoodByYourself(int neiSize) throws TasteException {
		// TODO Auto-generated method stub
		UserClustering.invokeClustering(getTrainingData(), (UPSSimiliarity) getSimilarity());
		System.out.println(Arrays.toString(UserClustering.Cluster_n));
		System.out.println(Arrays.toString(UserClustering.Cluster_o));
		System.out.println(Arrays.toString(UserClustering.Cluster_p));
		// neiSize 表示邻居用户的个数
		// neighborhood = new OPNNearestNUserNeighborhood(neiSize,
		// similarity, trainingData);
		UserNeighborhood neighborhood = new OPNNearestNUserNeighborhood(neiSize, getSimilarity(), getTrainingData());
		this.setNeighborhood(neighborhood);
		return neighborhood;
	}

}
