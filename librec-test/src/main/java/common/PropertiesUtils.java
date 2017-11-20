package common;

public class PropertiesUtils {
    @Deprecated
    public static final String projectDir = System.getProperty("user.dir");
    @Deprecated
    public static final String mainDir = projectDir + "/src/main/";

    public static final String resourcesDir = PropertiesUtils.class.getResource("/").toString();

    /**
     * mahout关联规则挖掘的结果存放的路径+相关中间结果存放的路径：testout
     */
    public static final String testOutPath = "C:\\Users\\ljd\\Desktop\\testout\\";

    public static void main(String[] args) {
        System.out.println(resourcesDir);
    }
}
