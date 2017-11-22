package common;

public class PropertiesUtils {
    @Deprecated
    public static final String projectDir = System.getProperty("user.dir");
    @Deprecated
    public static final String mainDir = projectDir + "/src/main/";

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String resourcesDir;

    /**
     * mahout关联规则挖掘的结果存放的路径+相关中间结果存放的路径：testout
     */
    public static final String TEST_OUT_PATH_LINUX = "/home/ljd/testout/";
    public static final String TEST_OUT_PATH_WINDOWS = "C:\\Users\\ljd\\Desktop\\testout\\";
    public static final String testOutPath;

    static {
        //针对Linux和Windows，使用不同的路径
        String s = PropertiesUtils.class.getResource("/").toString();
        if ("Linux".equals(OS_NAME)) {
            resourcesDir = s.substring(5, s.length() - 1);
            testOutPath = TEST_OUT_PATH_LINUX;
        } else {
            resourcesDir = s.substring(6,s.length() - 1);
            testOutPath = TEST_OUT_PATH_WINDOWS;
        }

    }

    public static void main(String[] args) {
        System.out.println(resourcesDir);
    }
}
