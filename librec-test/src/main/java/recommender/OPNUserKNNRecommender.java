package recommender;

import cluster.UserClustering;
import net.librec.common.LibrecException;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseVector;
import net.librec.math.structure.SymmMatrix;
import net.librec.math.structure.VectorEntry;
import net.librec.recommender.AbstractRecommender;
import net.librec.util.Lists;

import java.util.*;
import java.util.Map.Entry;

public class OPNUserKNNRecommender extends AbstractRecommender {

	private int knn;
	private DenseVector userMeans;
	private SymmMatrix similarityMatrix;
	private List<Entry<Integer, Double>>[] userSimilarityList;

	/**
	 * (non-Javadoc)
	 *
	 * @see AbstractRecommender#setup()
	 */
	@Override
	protected void setup() throws LibrecException {
		super.setup();
		knn = conf.getInt("rec.neighbors.knn.number");
		similarityMatrix = context.getSimilarity().getSimilarityMatrix();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see AbstractRecommender#trainModel()
	 */
	@Override
	protected void trainModel() throws LibrecException {
		userMeans = new DenseVector(numUsers);
		for (int userIdx = 0; userIdx < numUsers; userIdx++) {
			SparseVector userRatingVector = trainMatrix.row(userIdx);
			userMeans.set(userIdx, userRatingVector.getCount() > 0 ? userRatingVector.mean() : globalMean);
		}
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see AbstractRecommender#predict(int, int)
	 */
	@Override
	public double predict(int userIdx, int itemIdx) throws LibrecException {
		// create userSimilarityList if not exists
		if (!(null != userSimilarityList && userSimilarityList.length > 0)) {
			createUserSimilarityList();
		}
		// find a number of similar users
		List<Entry<Integer, Double>> nns = new ArrayList<>();
		List<Entry<Integer, Double>> simList = userSimilarityList[userIdx];

		int count = 0;
		Set<Integer> userSet = trainMatrix.getRowsSet(itemIdx);
		for (Entry<Integer, Double> userRatingEntry : simList) {
			int similarUserIdx = userRatingEntry.getKey();
			if (!userSet.contains(similarUserIdx)) {
				continue;
			}
			double sim = userRatingEntry.getValue();
			if (isRanking) {
				nns.add(userRatingEntry);
				count++;
			} else if (sim > 0) {
				nns.add(userRatingEntry);
				count++;
			}
			if (count == knn) {
				break;
			}
		}
		if (nns.size() == 0) {
			return isRanking ? 0 : globalMean;
		}
		if (isRanking) {
			double sum = 0.0d;
			for (Entry<Integer, Double> userRatingEntry : nns) {
				sum += userRatingEntry.getValue();
			}
			return sum;
		} else {
			// for rating prediction
			double sum = 0, ws = 0;
			for (Entry<Integer, Double> userRatingEntry : nns) {
				int similarUserIdx = userRatingEntry.getKey();
				double sim = userRatingEntry.getValue();
				double rate = trainMatrix.get(similarUserIdx, itemIdx);
				sum += sim * (rate - userMeans.get(similarUserIdx));
				ws += Math.abs(sim);
			}
			return ws > 0 ? userMeans.get(userIdx) + sum / ws : globalMean;
		}
	}

	/**
	 * Create userSimilarityList.加上用户分组思想后，需要在该方法上面做修改,取得的是分组后的用户集合<br/>
	 * 使用该
	 */
	public void createUserSimilarityList() {
		userSimilarityList = new ArrayList[numUsers];
		for (int userIndex = 0; userIndex < numUsers; ++userIndex) {
			// 判断用户属于哪个分组
			// System.out.println("用户Id" + userIndex);
			boolean[] hash = UserClustering.getCluster(userIndex);
			SparseVector similarityVector = similarityMatrix.row(userIndex);
			userSimilarityList[userIndex] = new ArrayList<>(similarityVector.size());
			Iterator<VectorEntry> simItr = similarityVector.iterator();
			while (simItr.hasNext()) {
				VectorEntry simVectorEntry = simItr.next();
				if (hash[simVectorEntry.index()] == false)
					continue;
				// System.out.println(simVectorEntry.index());
				userSimilarityList[userIndex]
						.add(new AbstractMap.SimpleImmutableEntry<>(simVectorEntry.index(), simVectorEntry.get()));
			}
			Lists.sortList(userSimilarityList[userIndex], true);
		}
	}
}
