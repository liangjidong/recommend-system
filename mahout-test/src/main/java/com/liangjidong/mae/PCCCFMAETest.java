package com.liangjidong.mae;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;

public class PCCCFMAETest extends AbstractMAETest {

	public static void main(String[] args) throws IOException {
		String projectDir = System.getProperty("user.dir");
		DataModel trainingData = new FileDataModel(new File(projectDir + "/src/main/u1.base"));
		DataModel testData = new FileDataModel(new File(projectDir + "/src/main/u1.test"));
		PCCCFMAETest test = new PCCCFMAETest(trainingData, testData);

		System.out.println(test.getMAE(10));
	}

	public PCCCFMAETest(DataModel trainingData, DataModel testData) {
		super(trainingData, testData);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected UserSimilarity setSimilarityByYourself() throws TasteException {
		// TODO Auto-generated method stub
		// UserSimilarity similarity = new PCCSimiliarity(getTrainingData());
		UserSimilarity similarity = new PearsonCorrelationSimilarity(getTrainingData());
		this.setSimilarity(similarity);
		return similarity;
	}

	@Override
	protected UserNeighborhood setNeighborhoodByYourself(int neiSize) throws TasteException {
		// TODO Auto-generated method stub
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(neiSize, getSimilarity(), getTrainingData());
		this.setNeighborhood(neighborhood);
		return neighborhood;
	}

}
