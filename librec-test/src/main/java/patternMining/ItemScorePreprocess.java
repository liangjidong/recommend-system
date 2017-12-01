package patternMining;

import common.PropertiesUtils;
import net.librec.common.LibrecException;
import net.librec.data.DataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by author on 17-11-30.
 */
public class ItemScorePreprocess {
    public static void main(String[] args) throws FileNotFoundException, LibrecException {

        DataModel dataModel = PropertiesUtils.dataModel;

        List<int[]> list = getListOfInterestingItems(dataModel);
        //输出到文件
        System.setOut(new PrintStream(
                new FileOutputStream(PropertiesUtils.testOutPath + "data.txt")));
        StringBuilder sb = new StringBuilder();
        for (int[] data : list) {
            for (int i = 0; i < data.length - 1; i++) {
                sb.append(data[i]).append(",");
            }
            sb.append(data[data.length - 1]);
            System.out.println(sb.toString());
            sb = new StringBuilder();
        }
    }

    /**
     * 评分预处理,并且需要在main方法中先调用一下,存入文件中,用于fpgrowth算法的使用
     *
     * @param dataModel
     * @return
     */
    public static List<int[]> getListOfInterestingItems(DataModel dataModel) {
        List<int[]> ans = new ArrayList<>();
        SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
        // 获取所有用户
        // trainData.
        int count = trainMatrix.numRows();
        int[] temp;
        for (int i = 0; i < count; i++) {
            SparseVector interest = trainMatrix.row(i);
            if (interest.getCount() == 0)
                continue;
            temp = new int[interest.getCount()];
            int index = 0;
            for (Integer idx : interest.getIndex()) {
                if (interest.get(idx) >= 3) {
                    temp[index++] = (idx + 1);
                } else {
                    temp[index++] = -(idx + 1);
                }
            }
            Arrays.sort(temp);
            ans.add(temp);
        }
        return ans;
    }
}
