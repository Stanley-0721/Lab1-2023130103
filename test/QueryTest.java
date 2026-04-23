import graph.Main;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * queryBridgeWords 函数的单元测试类
 * 覆盖以下场景：
 * 1. 单词1不在图中
 * 2. 单词2不在图中
 * 3. 单词1和单词2都不在图中
 * 4. 两个单词在图中，但无桥接词
 * 5. 有1个桥接词
 * 6. 有多个桥接词
 */
public class QueryTest {

    /**
     * 测试场景1：单词1不存在于图中
     * 输入：apple, cat
     * 期望输出：No word1: "apple" in the graph!
     */
    @Test
    public void testcase1() {
        Main.resetGraph();
        Main.putEdge("cat", "banana");
        String result1 = Main.queryBridgeWords("apple", "banana");
        String expectedResult1 = "No word1: \"apple\" in the graph!";
        assertEquals(expectedResult1, result1);
        System.out.println("testcase1: Pass");
    }

    /**
     * 测试场景2：单词2不存在于图中
     * 输入：apple, cat
     * 期望输出：No word2: "cat" in the graph!
     */
    @Test
    public void testcase2() {
        Main.resetGraph();
        Main.putEdge("apple", "banana");
        String result2 = Main.queryBridgeWords("apple", "cat");
        String expectedResult2 = "No word2: \"cat\" in the graph!";
        assertEquals(expectedResult2, result2);
        System.out.println("testcase2: Pass");
    }

    /**
     * 测试场景3：单词1和单词2都不在图中
     * 输入：apple, cat
     * 期望输出：No "apple" and "cat" in the graph!
     */
    @Test
    public void testcase3() {
        Main.resetGraph();
        Main.putEdge("mouse", "banana");
        String result3 = Main.queryBridgeWords("apple", "cat");
        String expectedResult3 = "No \"apple\" and \"cat\" in the graph!";
        assertEquals(expectedResult3, result3);
        System.out.println("testcase3: Pass");
    }

    /**
     * 测试场景4：单词1，2都在图中，但无桥接词
     * 输入：apple, banana
     * 期望输出：No bridge words from "apple" to "banana"!
     */
    @Test
    public void testcase4() {
        Main.resetGraph();
        Main.putEdge("apple", "banana");
        String result4 = Main.queryBridgeWords("apple", "banana");
        String expectedResult4 = "No bridge words from \"apple\" to \"banana\"!";
        assertEquals(expectedResult4, result4);
        System.out.println("testcase4: Pass");
    }

    /**
     * 测试场景5：单词1，2之间有1个桥接词
     * 输入：apple, banana
     * 期望输出：The bridge word from "apple" to "banana" is: "cat".
     */
    @Test
    public void testcase5() {
        Main.resetGraph();
        Main.putEdge("apple", "cat");
        Main.putEdge("cat", "banana");
        String result5 = Main.queryBridgeWords("apple", "banana");
        String expectedResult5 = "The bridge word from \"apple\" to \"banana\" is: \"cat\".";
        assertEquals(expectedResult5, result5);
        System.out.println("testcase5: Pass");
    }

    /**
     * 测试场景6：单词1，2之间有多个桥接词
     * 输入：apple, banana
     * 期望输出：The bridge words from "apple" to "banana" are: "cat", "dog" and "pig".
     */
    @Test
    public void testcase6() {
        Main.resetGraph();
        Main.putEdge("apple", "cat");
        Main.putEdge("apple", "dog");
        Main.putEdge("apple", "pig");
        Main.putEdge("cat", "banana");
        Main.putEdge("dog", "banana");
        Main.putEdge("pig", "banana");
        String result6 = Main.queryBridgeWords("apple", "banana");
        String expectedResult6 = "The bridge words from \"apple\" to \"banana\" are: \"cat\", \"dog\" and \"pig\".";
        assertEquals(expectedResult6, result6);
        System.out.println("testcase6: Pass");
    }
}