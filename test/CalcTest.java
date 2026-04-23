import org.junit.jupiter.api.Test;
import graph.Main;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * calcShortestPath 函数的单元测试类
 * 覆盖以下场景：
 * 1. 起点不在图中
 * 2. 终点不在图中
 * 3. 起点终点相同
 * 4. 终点不可达
 * 5. 终点可达
 */
public class CalcTest {
    /**
     * 测试场景1：起点不在图中
     * 输入：apple, banana
     * 期望输出：Error!不存在：apple
     */
    @Test
    public void testcase1() {
        Main.resetGraph();
        Main.putEdge("cat", "banana");
        String result1 = Main.calcShortestPath("apple", "banana");
        String expectedResult1 = "Error!不存在：apple";
        assertEquals(expectedResult1, result1);
        System.out.println("testcase1: Pass");
    }

    /**
     * 测试场景2：终点不在图中
     * 输入：apple, banana
     * 期望输出：Error!不存在：banana
     */
    @Test
    public void testcase2() {
        Main.resetGraph();
        Main.putEdge("apple", "cat");
        String result2 = Main.calcShortestPath("apple", "banana");
        String expectedResult2 = "Error!不存在：banana";
        assertEquals(expectedResult2, result2);
        System.out.println("testcase2: Pass");
    }

    /**
     * 测试场景3：起点终点相同
     * 输入：apple, apple
     * 期望输出：Error!起点终点相同
     */
    @Test
    public void testcase3() {
        Main.resetGraph();
        Main.putEdge("apple", "banana");
        String result3 = Main.calcShortestPath("apple", "apple");
        String expectedResult3 = "Error!起点终点相同";
        assertEquals(expectedResult3, result3);
        System.out.println("testcase3: Pass");
    }

    /**
     * 测试场景4：终点不可达
     * 输入：apple, dog
     * 期望输出：Error!两词之间不可达
     */
    @Test
    public void testcase4() {
        Main.resetGraph();
        Main.putEdge("apple", "banana");
        Main.putEdge("cat", "dog");
        String result4 = Main.calcShortestPath("apple", "dog");
        String expectedResult4 = "Error!两词之间不可达";
        assertEquals(expectedResult4, result4);
        System.out.println("testcase4: Pass");
    }

    /**
     * 测试场景5：终点可达
     * 输入：apple, mouse
     * 期望输出：加权最短路径（权重4）：apple → banana → dog → mouse
     */
    @Test
    public void testcase5() {
        Main.resetGraph();
        Main.putEdge("apple", "banana");
        Main.putEdge("apple", "banana");
        Main.putEdge("banana", "cat");
        Main.putEdge("banana", "cat");
        Main.putEdge("banana", "dog");
        Main.putEdge("cat", "mouse");
        Main.putEdge("dog", "mouse");
        String result5 = Main.calcShortestPath("apple", "mouse");
        String expectedResult5 = "加权最短路径（权重4）：apple → banana → dog → mouse";
        assertEquals(expectedResult5, result5);
        System.out.println("testcase5: Pass");
    }
}
