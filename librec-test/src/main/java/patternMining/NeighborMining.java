package patternMining;

import net.librec.data.model.TextDataModel;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;

import java.util.*;

/**
 * <strong>A pattern mining approach to enhance the accuracy of collaborative filtering in sparse data domains</strong>
 * <p>
 * 1,转换基础数据 <br/>
 * 2,求item交集<br/>
 * 3,求交集的最大集合 <br/>
 * 4,根据这些集合将用户分组<br/>
 *
 * @author ljd
 */
public class NeighborMining {
    // 该集合存放最终的用户分组集合
    private List<List<Integer>> users = new ArrayList<>();
    // 初始的数据集---以文本格式存储
    private TextDataModel dataModel;

    public NeighborMining(TextDataModel dataModel) {
        super();
        this.dataModel = dataModel;
        // dataModel.dataSplitter.getTrainData().getRows(col)
    }

    // 1，评分预处理
    public List<int[]> getListOfInterestingItems() {
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

    // 2,求交集并将交集进行求包
    private List<List<Integer>> findSubspaceAndDetect(List<int[]> interests) {
        List<List<Integer>> beforeDetect = new ArrayList<>();
        List<Integer> temp;
        // 求交集
        for (int i = 1; i < interests.size(); i++) {
            for (int j = 0; j < i; j++) {
                temp = getSubspace(interests.get(i), interests.get(j));
                if (temp.size() != 0) {
                    beforeDetect.add(temp);
                    // System.out.println(temp);
                }
            }
        }
        // detect，进行子集合并，只保留最大范围的集合
        // 首先进行list长度从大到小排序
        Collections.sort(beforeDetect, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                // TODO Auto-generated method stub
                return o2.size() - o1.size();
            }
        });
        // 子集合并
        List<List<Integer>> afterDetect = new ArrayList<>();
        boolean isContain = false;
        for (List<Integer> list : beforeDetect) {
            isContain = false;
            for (List<Integer> list2 : afterDetect) {
                if (subspaceDetact(list2, list) == 1) {
                    isContain = true;
                    break;
                }
            }
            if (!isContain) {
                afterDetect.add(list);
                // System.out.println("afterDetect增加：" + list);
            }

        }
        return afterDetect;
    }

    private void getUsers(List<int[]> interests, List<List<Integer>> afterDetect) {
        // 对每个long[]，对比是否包含afterDetect,可以肯定的是，结果的大小与afterDetect的大小相同
        users = new ArrayList<>();
        List<Integer> temp;
        int[] ls;
        // 先产生用户id保存到数组中,以便后续使用
        for (List<Integer> list : afterDetect) {
            temp = new ArrayList<>();
            for (int i = 0; i < interests.size(); i++) {
                ls = interests.get(i);
                if (isContains(ls, list)) {
                    temp.add(i);
                }
            }
            users.add(temp);
        }
    }

    /**
     * 获取子集
     *
     * @param u1
     * @param u2
     * @return
     */
    private List<Integer> getSubspace(int[] u1, int[] u2) {
        List<Integer> ans = new ArrayList<>();
        int i = 0, j = 0;
        while (i < u1.length && j < u2.length) {
            if (u1[i] == u2[j]) {
                ans.add(u1[i]);
                i++;
                j++;
            } else if (u1[i] < u2[j]) {
                i++;
            } else {
                j++;
            }
        }
        return ans;
    }

    /**
     * 判断两个集合是否具有包含关系<br/>
     * 返回1，表示s1包含s2，<br/>
     * 返回0，表示不具有包含关系 <br/>
     * 返回-1，s2包含s1
     *
     * @param s1
     * @param s2
     * @return
     */
    private int subspaceDetact(List<Integer> s1, List<Integer> s2) {
        boolean isChange = false;
        int ans = 0;
        if (s1.size() < s2.size()) {
            isChange = true;
            List<Integer> temp = s1;
            s1 = s2;
            s2 = temp;
        }
        for (Integer long1 : s2) {
            if (!s1.contains(long1))
                return 0;
        }
        return isChange ? -1 : 1;
    }

    /**
     * 判断用户是否包含该兴趣
     *
     * @param userInterest
     * @param part
     * @return
     */
    private boolean isContains(int[] userInterest, List<Integer> part) {
        if (userInterest.length < part.size())
            return false;
        int i = 0, j = 0;
        while (i < userInterest.length && j < part.size()) {
            if (userInterest[i] != part.get(j)) {
                i++;
            } else {
                i++;
                j++;
            }
        }
        if (j != part.size())
            return false;
        return true;
    }

    public void command() {
        List<int[]> listOfInterestingItems = getListOfInterestingItems();
        for (int[] ls : listOfInterestingItems) {
            System.out.println(Arrays.toString(ls));
        }
        // System.out.println("----------------------");
        // List<List<Integer>> subspaceAndDetect =
        // findSubspaceAndDetect(listOfInterestingItems);
        // for (List<Integer> list : subspaceAndDetect) {
        // System.out.println(list);
        // }
        // System.out.println("----------------------");
        // getUsers(listOfInterestingItems, subspaceAndDetect);
        // for (List<Integer> list : users) {
        // System.out.println(list);
        // }
    }
}
