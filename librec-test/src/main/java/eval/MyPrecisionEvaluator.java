package eval;

import net.librec.eval.AbstractRecommenderEvaluator;
import net.librec.math.structure.SparseMatrix;
import net.librec.recommender.item.ItemEntry;
import net.librec.recommender.item.RecommendedList;

import java.util.List;
import java.util.Set;

/**
 * Created by author on 17-12-4.
 */
public class MyPrecisionEvaluator extends AbstractRecommenderEvaluator {
    /**
     * Evaluate on the test set with the the list of recommended items.
     *
     * @param testMatrix      the given test set
     * @param recommendedList the list of recommended items
     * @return evaluate result
     */
    public double evaluate(SparseMatrix testMatrix, RecommendedList recommendedList) {
        double totalPrecision = 0.0;
        int numUsers = testMatrix.numRows();
        int nonZeroNumUsers = 0;
        for (int userID = 0; userID < numUsers; userID++) {
            Set<Integer> testSetByUser = testMatrix.getColumnsSet(userID);
            if (testSetByUser.size() > 0) {
                List<ItemEntry<Integer, Double>> recommendListByUser = recommendedList.getItemIdxListByUserIdx(userID);

                int numHits = 0;
                int topK = this.topN <= recommendListByUser.size() ? this.topN : recommendListByUser.size();
                for (int indexOfItem = 0; indexOfItem < topK; indexOfItem++) {
                    int itemID = recommendListByUser.get(indexOfItem).getKey();
                    if (testSetByUser.contains(itemID) && testMatrix.get(userID, itemID) >= 3) {
                        numHits++;
                    }
                }
                totalPrecision += numHits / (this.topN + 0.0);
                nonZeroNumUsers++;
            }
        }
        return nonZeroNumUsers > 0 ? totalPrecision / nonZeroNumUsers : 0.0d;
    }
}
