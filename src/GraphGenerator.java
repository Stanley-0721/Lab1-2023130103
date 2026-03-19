import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphGenerator {

    // 给主类调用
    public static void generateGraph() {
        String tempPath = "text/temp.txt";
        String content = "";

        try (BufferedReader br = new BufferedReader(new FileReader(tempPath))) {
            content = br.readLine();
        } catch (IOException e) {
            System.err.println("读取 temp.txt 失败");
            return;
        }

        if (content.isEmpty()) {
            System.err.println("temp.txt 为空");
            return;
        }

        String[] words = content.split(" ");
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) {
            String from = words[i];
            String to = words[i + 1];
            graph.computeIfAbsent(from, k -> new HashMap<>());
            graph.get(from).put(to, graph.get(from).getOrDefault(to, 0) + 1);
        }

        try (FileWriter writer = new FileWriter("text/graph.txt")) {
            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int w = graph.get(from).get(to);
                    writer.write(from + " -> " + to + " : " + w + "\n");
                }
            }
            System.out.println("✅ 有向图生成完成 → text/graph.txt");
        } catch (IOException e) {
            System.err.println("写入 graph 失败");
        }
    }
}