package librec_test;

import java.util.Arrays;

import common.PropertiesUtils;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;

public class SearchDataModelDistribution {
	public static void main(String[] args) throws LibrecException {
		String dirName = "Data_EXTRACT";
		Configuration conf = new Configuration();
		conf.set("dfs.data.dir", PropertiesUtils.mainDir);
		conf.set("data.input.path", dirName);
		conf.set("data.model.splitter", "testset");
		// 预留的测试数据集应该在训练数据集的路径之下
		conf.set("data.testset.path", dirName + "/testData");
		conf.set("data.model.format", "text");
		conf.set("data.column.format", "UIRT");
		conf.set("data.convert.binarize.threshold", "-1.0");
		Randoms.seed(1);
		TextDataModel dataModel = new TextDataModel(conf);
		dataModel.buildDataModel();
		// 打印出dataModel
		// 获取训练集合
		int low = 0, high = 0, mid = 0, total = 0;
		SparseMatrix trainMatrix = dataModel.getDataSplitter().getTrainData();
		int count = trainMatrix.numRows();
		for (int i = 0; i < count; i++) {
			SparseVector interest = trainMatrix.row(i);
			if (interest.getCount() == 0)
				continue;
			for (Integer idx : interest.getIndex()) {
				total++;
				if (interest.get(idx) >= 4) {
					high++;
				} else if (interest.get(idx) > 2 && interest.get(idx) < 4) {
					mid++;
				} else if (interest.get(idx) > 0 && interest.get(idx) <= 2) {
					low++;
				}
			}
		}
		System.out.println("用户数量：" + trainMatrix.numRows);
		System.out.println("项目数量：" + trainMatrix.numColumns);
		System.out.println("low:" + low * 1.0 / total);
		System.out.println("mid:" + mid * 1.0 / total);
		System.out.println("high:" + high * 1.0 / total);
	}
}
