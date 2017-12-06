package common;

import net.librec.conf.Configuration;
import net.librec.data.DataModel;
import net.librec.data.model.TextDataModel;
import net.librec.math.algorithm.Randoms;

public class PropertiesUtils {
    @Deprecated
    public static final String projectDir = System.getProperty("user.dir");
    @Deprecated
    public static final String mainDir = projectDir + "/src/main/";

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String resourcesDir;

    /**
     * mahout关联规则挖掘的结果存放的路径+相关中间结果存放的路径：testout
     * out_new :SML1M
     * out_new1:ML100K
     * paper_example_test:简单的测试数据集(7*9)
     */
    /*public static final String TEST_OUT_PATH_LINUX = "/home/ljd/testout/out_new1/";
    public static final String TEST_OUT_PATH_WINDOWS = "C:\\Users\\ljd\\Desktop\\testout\\out_new1\\";*/
    //ML100K
    public static final String TEST_OUT_PATH_LINUX = "/home/ljd/testout/out_ML100K/";
    public static final String TEST_OUT_PATH_WINDOWS = "C:\\Users\\ljd\\Desktop\\testout\\out_ML100K\\";
    /*//SML1M
    public static final String TEST_OUT_PATH_LINUX = "/home/ljd/testout/out_SML1M/";
    public static final String TEST_OUT_PATH_WINDOWS = "C:\\Users\\ljd\\Desktop\\testout\\out_SML1M\\";*/
    /*//SML1M_2
    public static final String TEST_OUT_PATH_LINUX = "/home/ljd/testout/out_SML1M_2/";
    public static final String TEST_OUT_PATH_WINDOWS = "C:\\Users\\ljd\\Desktop\\testout\\out_SML1M_2\\";*/
    public static final String testOutPath;

    /**
     * 生成DataModel,在不同数据集切换的时候需要将该字段修改
     */
//    public static final String DATA_MODEL_NAME = "Data_SML1M_2";
    public static final String DATA_MODEL_NAME = "Data_ML100K";
    public static final DataModel dataModel;

    private static DataModel generateDataMode(String dataModelName) {
        //读取datamodel
        String dirName = dataModelName;
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", PropertiesUtils.resourcesDir);
        conf.set("data.input.path", dirName);
        conf.set("data.model.splitter", "testset");
        conf.set("data.testset.path", dirName + "/testData");
        conf.set("data.model.format", "text");
        conf.set("data.column.format", "UIRT");
        conf.set("data.convert.binarize.threshold", "-1.0");
        conf.set("rec.recommender.similarity.key", "user");
        Randoms.seed(1);
        TextDataModel dataModel = new TextDataModel(conf);
        try {
            dataModel.buildDataModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataModel;
    }

    static {
        //针对Linux和Windows，使用不同的路径
        String s = PropertiesUtils.class.getResource("/").toString();
        if ("Linux".equals(OS_NAME)) {
            resourcesDir = s.substring(5, s.length() - 1);
            testOutPath = TEST_OUT_PATH_LINUX;
        } else {
            resourcesDir = s.substring(6, s.length() - 1);
            testOutPath = TEST_OUT_PATH_WINDOWS;
        }

        //生成dataModel,方便使用
        dataModel = generateDataMode(DATA_MODEL_NAME);
    }

    public static void main(String[] args) {
        System.out.println(resourcesDir);
    }
}
