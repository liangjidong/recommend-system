package test;

import common.PropertiesUtils;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;
import patternMining.NeighborMining;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class MiningTest {
    public static final String PATHDIR = PropertiesUtils.testOutPath;

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", PropertiesUtils.resourcesDir);
        conf.set("data.input.path", "trainData");
        conf.set("data.model.splitter", "testset");
        // 预留的测试数据集应该在训练数据集的路径之下
        conf.set("data.testset.path", "trainData/testData");
        conf.set("data.model.format", "text");
        conf.set("data.column.format", "UIRT");
        conf.set("data.convert.binarize.threshold", "-1.0");
        Randoms.seed(1);
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();
        NeighborMining nm = new NeighborMining(dataModel);
        System.setOut(
                new PrintStream(new FileOutputStream(PropertiesUtils.testOutPath + "out1.txt")));
        nm.command();

    }
}
