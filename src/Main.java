import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Main {
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();
    private static JTextArea resultArea;
    private static JLabel imageLabel;       // 图片放在结果区域里
    private static JPanel centerPanel;      // 中间统一容器

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lab1");
        frame.setSize(950, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // 自动全屏

        // ========== 顶部按钮 ==========
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnLoadGen = new JButton("1. 加载文本并生成有向图");
        JButton btnShowGraph = new JButton("2. 展示有向图");
        JButton btnQuery = new JButton("3. 查询桥接词");
        JTextField word1Field = new JTextField(8);
        JTextField word2Field = new JTextField(8);
        panel.add(btnLoadGen);
        panel.add(btnShowGraph);
        panel.add(new JLabel("单词1:"));
        panel.add(word1Field);
        panel.add(new JLabel("单词2:"));
        panel.add(word2Field);
        panel.add(btnQuery);

        // ========== 中间区域：结果 + 图片 二合一 ==========
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        resultArea = new JTextArea(8, 0);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScroll = new JScrollPane(imageLabel);

        centerPanel.add(scrollPane);
        centerPanel.add(imageScroll);

        // ========== 组装窗口 ==========
        frame.add(panel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        // ======================
        // 功能1
        // ======================
        btnLoadGen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("text");
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                processFile(file.getAbsolutePath());
                resultArea.append("✅ 已加载：" + file.getName() + "，有向图已生成！\n");
            }
        });

        // ======================
        // 功能2：展示有向图（显示在结果区）
        // ======================
        btnShowGraph.addActionListener(e -> {
            File imgFile = new File("graph/graph.png");
            if (!imgFile.exists()) {
                resultArea.append("❌ 请先执行功能1！\n");
                return;
            }
            try {
                BufferedImage img = ImageIO.read(imgFile);
                int w = Math.min(img.getWidth(), 1200);
                int h = Math.min(img.getHeight(), 400);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                resultArea.append("✅ 有向图已显示在结果区域！\n");
            } catch (Exception ex) {
                resultArea.append("❌ 图片加载失败！\n");
            }
        });

        // ======================
        // 功能3
        // ======================
        btnQuery.addActionListener(e -> {
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

    // ======================
    // 以下是你的原有逻辑，完全不动
    // ======================
    private static void processFile(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                int c;
                while ((c = br.read()) != -1) {
                    char ch = (char) c;
                    // 只保留字母，其他全部变成空格（真正的干净清理）
                    if (Character.isLetter(ch)) {
                        sb.append(Character.toLowerCase(ch));
                    } else {
                        sb.append(' ');
                    }
                }
            }

            // 🔥 关键修复：把连续空格变成单个空格，并且确保不会丢失单词
            String text = sb.toString().trim().replaceAll("\\s+", " ");

            // 写入temp.txt
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("text/temp.txt"))) {
                bw.write(text);
            }

            buildGraph(text);
            showDirectedGraph();
            Thread.sleep(800);
        } catch (Exception ex) {
            resultArea.append("❌ 处理失败：" + ex.getMessage() + "\n");
        }
    }

    private static void buildGraph(String text) {
        graph.clear();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            String f = words[i], t = words[i + 1];
            graph.computeIfAbsent(f, k -> new HashMap<>()).put(t, graph.get(f).getOrDefault(t, 0) + 1);
        }
    }

    public static void showDirectedGraph() {
        try {
            File dir = new File("graph");
            if (!dir.exists()) dir.mkdirs();
            StringBuilder dot = new StringBuilder("digraph G {\n    rankdir=LR;\n    node [shape=circle, style=filled, color=lightblue];\n");
            for (String from : graph.keySet())
                for (String to : graph.get(from).keySet())
                    dot.append("    \"").append(from).append("\" -> \"").append(to).append("\" [label=\"").append(graph.get(from).get(to)).append("\"];\n");
            dot.append("}\n");
            try (BufferedWriter w = new BufferedWriter(new FileWriter("graph/graph.dot"))) {
                w.write(dot.toString());
            }
            Process p = Runtime.getRuntime().exec("dot -Tpng graph/graph.dot -o graph/graph.png");
            p.waitFor();
        } catch (Exception e) {
            resultArea.append("❌ 生成图失败：" + e.getMessage() + "\n");
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        // 终极清洗：统一小写 + 只留字母
        word1 = word1.toLowerCase().replaceAll("[^a-z]", "");
        word2 = word2.toLowerCase().replaceAll("[^a-z]", "");

        // 收集图里所有单词（包含终点词）
        java.util.Set<String> allWords = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            allWords.add(entry.getKey());
            allWords.addAll(entry.getValue().keySet());
        }

        boolean has1 = allWords.contains(word1);
        boolean has2 = allWords.contains(word2);

        if (!has1 && !has2)
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        if (!has1)
            return "No \"" + word1 + "\" in the graph!";
        if (!has2)
            return "No \"" + word2 + "\" in the graph!";

        // 🔥 修复歧义：全部写完整名字 java.util.List
        java.util.List<String> bridges = new java.util.ArrayList<>();

        if (graph.containsKey(word1)) {
            for (String w3 : graph.get(word1).keySet()) {
                if (graph.containsKey(w3) && graph.get(w3).containsKey(word2)) {
                    bridges.add(w3);
                }
            }
        }

        if (bridges.isEmpty())
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";

        StringBuilder sb = new StringBuilder();
        sb.append("The bridge words from \"").append(word1).append("\" to \"").append(word2).append("\" are: ");
        for (int i = 0; i < bridges.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(bridges.get(i)).append("\"");
        }
        sb.append(".");
        return sb.toString();
    }
}