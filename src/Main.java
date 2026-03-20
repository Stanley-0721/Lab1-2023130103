import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;

public class Main {
    // 全局图结构（所有功能共用）
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();
    private static JTextArea resultArea;  // 结果显示
    private static JLabel graphLabel;     // 图图片显示

    public static void main(String[] args) {
        // 创建GUI主窗口
        JFrame frame = new JFrame("单词有向图生成器");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // ========== 顶部功能面板 ==========
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnLoad = new JButton("1. 加载并处理文本");
        JButton btnGraph = new JButton("2. 生成有向图");
        JButton btnBridge = new JButton("3. 查询桥接词");

        JTextField word1Field = new JTextField(6);
        JTextField word2Field = new JTextField(6);
        word1Field.setToolTipText("单词1");
        word2Field.setToolTipText("单词2");

        panel.add(btnLoad);
        panel.add(btnGraph);
        panel.add(new JLabel("单词1:"));
        panel.add(word1Field);
        panel.add(new JLabel("单词2:"));
        panel.add(word2Field);
        panel.add(btnBridge);

        // ========== 中部：结果区域 ==========
        resultArea = new JTextArea(6, 0);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // ========== 底部：图片显示 ==========
        graphLabel = new JLabel();
        graphLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScroll = new JScrollPane(graphLabel);

        // ========== 组装窗口 ==========
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(imageScroll, BorderLayout.SOUTH);

        // ======================
        // 按钮 1：加载文本
        // ======================
        btnLoad.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser("text");
            int option = chooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                processFile(file.getAbsolutePath());
                resultArea.append("✅ 已加载并处理：" + file.getName() + "\n");
            }
        });

        // ======================
        // 按钮 2：生成有向图
        // ======================
        btnGraph.addActionListener((ActionEvent e) -> {
            if (graph.isEmpty()) {
                resultArea.append("❌ 请先加载文本！\n");
                return;
            }
            showDirectedGraph();
            resultArea.append("✅ 有向图已生成：graph/graph.png\n");
            // 显示图片
            ImageIcon icon = new ImageIcon("graph/graph.png");
            graphLabel.setIcon(icon);
        });

        // ======================
        // 按钮 3：查询桥接词
        // ======================
        btnBridge.addActionListener((ActionEvent e) -> {
            String w1 = word1Field.getText().trim();
            String w2 = word2Field.getText().trim();
            if (w1.isEmpty() || w2.isEmpty()) {
                resultArea.append("❌ 请输入两个单词！\n");
                return;
            }
            String res = queryBridgeWords(w1, w2);
            resultArea.append("🔍 " + res + "\n");
        });

        frame.setVisible(true);
    }

    // ==========================
    // 文本处理（生成temp.txt）
    // ==========================
    private static void processFile(String inputFilePath) {
        try {
            File textDir = new File("text");
            if (!textDir.exists()) textDir.mkdirs();

            StringBuilder processedText = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
                int c;
                while ((c = reader.read()) != -1) {
                    char ch = (char) c;
                    if (Character.isLetter(ch))
                        processedText.append(Character.toLowerCase(ch));
                    else
                        processedText.append(' ');
                }
            }

            String result = processedText.toString().replaceAll("\\s+", " ").trim();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("text/temp.txt"))) {
                writer.write(result);
            }

            buildGraph(result);

        } catch (Exception ex) {
            resultArea.append("❌ 处理文件失败：" + ex.getMessage() + "\n");
        }
    }

    // ==========================
    // 构建有向图
    // ==========================
    private static void buildGraph(String text) {
        graph.clear();
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
    // 功能1：生成有向图（Graphviz）
    // ==========================
    public static void showDirectedGraph() {
        try {
            File graphDir = new File("graph");
            if (!graphDir.exists()) graphDir.mkdirs();

            StringBuilder dot = new StringBuilder();
            dot.append("digraph G {\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=circle, style=filled, color=lightblue];\n");

            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int weight = graph.get(from).get(to);
                    dot.append(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight));
                }
            }
            dot.append("}\n");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("graph/graph.dot"))) {
                writer.write(dot.toString());
            }

            String[] cmd = {"dot", "-Tpng", "graph/graph.dot", "-o", "graph/graph.png"};
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();

        } catch (Exception e) {
            resultArea.append("❌ 生成图片失败：" + e.getMessage() + "\n");
        }
    }

    // ==========================
    // 功能2：查询桥接词
    // ==========================
    public static String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        boolean has1 = graph.containsKey(word1);
        boolean has2 = graph.containsKey(word2);

        if (!has1 && !has2)
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        if (!has1)
            return "No \"" + word1 + "\" in the graph!";
        if (!has2)
            return "No \"" + word2 + "\" in the graph!";

        java.util.List<String> bridges = new java.util.ArrayList<>();

        for (String w3 : graph.get(word1).keySet()) {
            if (graph.containsKey(w3) && graph.get(w3).containsKey(word2)) {
                bridges.add(w3);
            }
        }

        if (bridges.isEmpty())
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";

        StringBuilder sb = new StringBuilder();
        sb.append("The bridge words from \"").append(word1).append("\" to \"").append(word2).append("\" are: ");

        for (int i = 0; i < bridges.size(); i++) {
            if (i > 0) sb.append(", ");
            // 给桥接词也加上双引号
            sb.append("\"").append(bridges.get(i)).append("\"");
        }
        sb.append(".");
        return sb.toString();
    }
}