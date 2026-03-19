import java.io.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("===== 文本处理 + 有向图生成 一键启动 =====");

        // ========== 第一步：调用文本清洗，生成 temp.txt ==========
        TextPreprocessor.process();

        // ========== 第二步：读取 temp.txt 生成有向图 graph.txt ==========
        GraphGenerator.generateGraph();

        System.out.println("\n✅ 全部完成！");
        System.out.println("📄 清洗结果：text/temp.txt");
        System.out.println("📊 有向图结果：text/graph.txt");
    }
}