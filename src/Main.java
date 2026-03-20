import java.io.*;
import java.util.*;
public class Main {
    // 有向图数据结构
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputFilePath;

        System.out.println("请选择输入文件：");
        System.out.println("1 - text/Easy Test.txt");
        System.out.println("2 - text/Cursed Be The Treasure.txt");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            inputFilePath = "text/Easy Test.txt";
        } else if (choice == 2) {
            inputFilePath = "text/Cursed Be The Treasure.txt";
        } else {
            System.out.println("无效选择，程序退出。");
            return;
        }

        // 确保 text 目录存在
        File textDir = new File("text");
        if (!textDir.exists()) textDir.mkdirs();

        // 读取并处理文本
        StringBuilder processedText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            int c;
            while ((c = reader.read()) != -1) {
                char ch = (char) c;
                if (Character.isLetter(ch)) {
                    processedText.append(Character.toLowerCase(ch));
                } else {
                    processedText.append(' ');
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件错误：" + e.getMessage());
            return;
        }

        // 简化文本
        String result = processedText.toString().replaceAll("\\s+", " ").trim();

        // 写入 temp.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("text/temp.txt"))) {
            writer.write(result);
            System.out.println("处理完成：text/temp.txt");
        } catch (IOException e) {
            System.err.println("写入失败：" + e.getMessage());
        }

        // 构建有向图
        buildGraph(result);

        // ======================
        // 生成有向图（Graphviz 专业版）
        // ======================
        showDirectedGraph();

        scanner.close();
    }

    // 构建单词有向图
    private static void buildGraph(String text) {
        String[] words = text.split(" ");
        if (words.length < 2) return;

        for (int i = 0; i < words.length - 1; i++) {
            String from = words[i];
            String to = words[i + 1];
            graph.computeIfAbsent(from, k -> new HashMap<>());
            graph.get(from).put(to, graph.get(from).getOrDefault(to, 0) + 1);
        }
    }

    // ==========================
    // 功能1：生成有向图 → graph.png
    // ==========================
    public static void showDirectedGraph() {
        try {
            // 确保 graph 目录存在
            File graphDir = new File("graph");
            if (!graphDir.exists()) graphDir.mkdirs();

            // 生成 DOT 语言
            StringBuilder dot = new StringBuilder();
            dot.append("digraph G {\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=circle, style=filled, color=lightblue];\n");

            // 写入边和权重
            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int weight = graph.get(from).get(to);
                    dot.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight));
                }
            }
            dot.append("}\n");

            // 写入 dot 文件
            File dotFile = new File("graph/graph.dot");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFile))) {
                writer.write(dot.toString());
            }

            // 调用 Graphviz 生成 PNG
            String[] cmd = {"dot", "-Tpng", "graph/graph.dot", "-o", "graph/graph.png"};
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();

            System.out.println("✅ 有向图生成成功！");
            System.out.println("位置：graph/graph.png");

        } catch (Exception e) {
            System.err.println("❌ 生成图失败，请确认 Graphviz 已安装并加入 PATH");
            e.printStackTrace();
        }
    }
}