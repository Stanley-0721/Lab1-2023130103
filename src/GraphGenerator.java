import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphGenerator {

    public static void main(String[] args) {
        // 直接读取上一步生成的 temp.txt
        String tempFilePath = "text/temp.txt";

        // 1. 读取清洗好的文本
        String cleanedText = "";
        try (BufferedReader br = new BufferedReader(new FileReader(tempFilePath))) {
            cleanedText = br.readLine(); // 因为 temp.txt 只有一行（清洗后的结果）
        } catch (IOException e) {
            System.err.println("读取 temp.txt 失败：" + e.getMessage());
            return;
        }

        if (cleanedText.isEmpty()) {
            System.err.println("temp.txt 内容为空！");
            return;
        }

        // 2. 分割成单词数组
        String[] words = cleanedText.split(" ");

        // 3. 构建有向图
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) {
            String from = words[i];
            String to = words[i + 1];

            graph.computeIfAbsent(from, k -> new HashMap<>());
            graph.get(from).put(to, graph.get(from).getOrDefault(to, 0) + 1);
        }

        // 4. 输出图到 text/graph.txt
        String outputPath = "text/graph.txt";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write("有向图边（from -> to : weight）\n");
            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int weight = graph.get(from).get(to);
                    writer.write(from + " -> " + to + " : " + weight + "\n");
                }
            }
            System.out.println("✅ 有向图生成完成！已保存到：" + outputPath);
        } catch (IOException e) {
            System.err.println("写入 graph.txt 失败：" + e.getMessage());
        }
    }
}