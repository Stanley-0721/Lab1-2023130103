import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;

public class Main {
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();
    private static JTextPane resultArea;
    private static JLabel imageLabel;
    private static JPanel centerPanel;
    private static Random random = new Random();
    private static SimpleAttributeSet redAttr;

    public static void main(String[] args) {
        redAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(redAttr, Color.RED);

        JFrame frame = new JFrame("Lab1");
        frame.setSize(950, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnLoadGen = new JButton("1. 加载文本并生成有向图");
        JButton btnShowGraph = new JButton("2. 展示有向图");
        JButton btnQuery = new JButton("3. 查询桥接词");
        JButton btnGenerate = new JButton("4. 生成新文本");
        JButton btnShortestPath = new JButton("5. 查询最短路径");

        JTextField word1Field = new JTextField(6);
        JTextField word2Field = new JTextField(6);
        JTextField newTextField = new JTextField(30);

        panel.add(btnLoadGen);
        panel.add(btnShowGraph);
        panel.add(new JLabel("单词1:"));
        panel.add(word1Field);
        panel.add(new JLabel("单词2:"));
        panel.add(word2Field);
        panel.add(btnQuery);
        panel.add(btnGenerate);
        panel.add(btnShortestPath);
        panel.add(new JLabel("新文本:"));
        panel.add(newTextField);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        resultArea = new JTextPane();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScroll = new JScrollPane(imageLabel);

        centerPanel.add(scrollPane);
        centerPanel.add(imageScroll);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        btnLoadGen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("text");
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                processFile(file.getAbsolutePath());
                appendResult("✅ 已加载：" + file.getName() + "，有向图已生成！\n", Color.BLACK);
            }
        });

        btnShowGraph.addActionListener(e -> {
            File imgFile = new File("graph/graph.png");
            if (!imgFile.exists()) {
                appendResult("❌ 请先执行功能1！\n", Color.RED);
                return;
            }
            try {
                BufferedImage img = ImageIO.read(imgFile);
                int w = Math.min(img.getWidth(), 1200);
                int h = Math.min(img.getHeight(), 400);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                appendResult("✅ 有向图已显示在结果区域！\n", Color.BLACK);
            } catch (Exception ex) {
                appendResult("❌ 图片加载失败！\n", Color.RED);
            }
        });

        btnQuery.addActionListener(e -> {
            String w1 = word1Field.getText().trim();
            String w2 = word2Field.getText().trim();
            if (w1.isEmpty() || w2.isEmpty()) {
                appendResult("❌ 请输入两个单词！\n", Color.RED);
                return;
            }
            String res = queryBridgeWords(w1, w2);
            appendResult("🔍 " + res + "\n", Color.BLACK);
        });

        btnGenerate.addActionListener(e -> {
            String inputText = newTextField.getText().trim();
            if (inputText.isEmpty()) {
                appendResult("❌ 请输入新文本！\n", Color.RED);
                return;
            }
            if (graph.isEmpty()) {
                appendResult("❌ 请先执行功能1！\n", Color.RED);
                return;
            }
            appendResult("📝 生成新文本：\n", Color.BLACK);
            displayGeneratedTextWithRed(inputText);
        });

        btnShortestPath.addActionListener(e -> {
            String start = word1Field.getText().trim().toLowerCase().replaceAll("[^a-z]", "");
            String end = word2Field.getText().trim().toLowerCase().replaceAll("[^a-z]", "");

            if (start.isEmpty() || end.isEmpty()) {
                appendResult("❌ 请输入两个单词！\n", Color.RED);
                return;
            }
            if (graph.isEmpty()) {
                appendResult("❌ 请先执行功能1！\n", Color.RED);
                return;
            }
            queryShortestPath(start, end);
        });

        frame.setVisible(true);
    }

    // 你要求的标准函数：计算最短路径，返回字符串
    private static String calcShortestPath(String word1, String word2) {
        String start = word1.toLowerCase().replaceAll("[^a-z]", "");
        String end = word2.toLowerCase().replaceAll("[^a-z]", "");

        Set<String> allWords = new HashSet<>();
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
            allWords.add(entry.getKey());
            allWords.addAll(entry.getValue().keySet());
        }

        if (!allWords.contains(start)) {
            return "❌ 单词 \"" + start + "\" 不存在！";
        }
        if (!allWords.contains(end)) {
            return "❌ 单词 \"" + end + "\" 不存在！";
        }
        if (start.equals(end)) {
            return "❌ 起点和终点不能相同！";
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Queue<String> q = new LinkedList<>();

        for (String w : allWords) dist.put(w, -1);
        dist.put(start, 0);
        q.add(start);

        while (!q.isEmpty()) {
            String u = q.poll();
            if (u.equals(end)) break;
            if (!graph.containsKey(u)) continue;
            for (String v : graph.get(u).keySet()) {
                if (dist.get(v) == -1) {
                    dist.put(v, dist.get(u) + 1);
                    prev.put(v, u);
                    q.add(v);
                }
            }
        }

        if (dist.get(end) == -1) {
            return "📶 从 \"" + start + "\" 到 \"" + end + "\" 不可达！";
        }

        List<String> path = new ArrayList<>();
        String cur = end;
        while (cur != null) {
            path.add(cur);
            cur = prev.get(cur);
        }
        Collections.reverse(path);

        StringBuilder sb = new StringBuilder();
        sb.append("📶 最短路径（长度 ").append(path.size() - 1).append("）：");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }

    // 界面调用：显示结果 + 高亮图
    private static void queryShortestPath(String start, String end) {
        String result = calcShortestPath(start, end);
        appendResult(result + "\n", Color.BLACK);

        if (result.contains("→")) {
            List<String> path = extractPathFromString(result);
            imageLabel.setIcon(null);
            System.gc();
            highlightShortestPath(path);

            try {
                File highlightFile = new File("graph/highlighted_graph.png");
                BufferedImage img = ImageIO.read(highlightFile);
                int w = Math.min(img.getWidth(), 1200);
                int h = Math.min(img.getHeight(), 400);
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);

                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.repaint();
                centerPanel.revalidate();
                centerPanel.repaint();

                appendResult("✅ 最短路径已高亮显示！\n", Color.BLACK);
            } catch (Exception e) {
                appendResult("❌ 加载高亮图失败：" + e.getMessage() + "\n", Color.RED);
            }
        }
    }

    // 从字符串结果中提取路径
    private static List<String> extractPathFromString(String result) {
        List<String> path = new ArrayList<>();
        try {
            String[] parts = result.split("：");
            if (parts.length >= 2) {
                String[] nodes = parts[1].split(" → ");
                path.addAll(Arrays.asList(nodes));
            }
        } catch (Exception ignored) {}
        return path;
    }

    // 高亮最短路径
    private static void highlightShortestPath(List<String> path) {
        try {
            File dir = new File("graph");
            if (!dir.exists()) dir.mkdirs();

            Set<String> pathEdges = new HashSet<>();
            for (int i = 0; i < path.size() - 1; i++) {
                String f = path.get(i);
                String t = path.get(i+1);
                pathEdges.add(f + "->" + t);
            }

            StringBuilder dot = new StringBuilder();
            dot.append("digraph G {\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=circle, style=filled, color=lightblue];\n");

            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    String edgeKey = from + "->" + to;
                    if (pathEdges.contains(edgeKey)) {
                        dot.append("    \"" + from + "\" -> \"" + to + "\" [label=\"" + graph.get(from).get(to) + "\", color=red, penwidth=3];\n");
                    } else {
                        dot.append("    \"" + from + "\" -> \"" + to + "\" [label=\"" + graph.get(from).get(to) + "\"];\n");
                    }
                }
            }
            dot.append("}\n");

            File dotFile = new File("graph/highlighted_graph.dot");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFile))) {
                writer.write(dot.toString());
            }

            Process process = Runtime.getRuntime().exec("dot -Tpng graph/highlighted_graph.dot -o graph/highlighted_graph.png");
            process.waitFor();

        } catch (Exception e) {
            appendResult("❌ 生成高亮图失败：" + e.getMessage(), Color.RED);
        }
    }

    // 生成新文本
    private static String generateNewText(String inputText) {
        String cleaned = inputText.toLowerCase().replaceAll("[^a-z\\s]", "");
        String[] words = cleaned.split("\\s+");
        if (words.length <= 1) return inputText;

        StringBuilder sb = new StringBuilder();
        sb.append(words[0]);

        for (int i = 0; i < words.length - 1; i++) {
            String w1 = words[i];
            String w2 = words[i + 1];
            List<String> bridges = getBridgeWords(w1, w2);

            if (!bridges.isEmpty()) {
                String b = bridges.get(random.nextInt(bridges.size()));
                sb.append(" ").append(b);
            }
            sb.append(" ").append(w2);
        }
        return sb.toString();
    }

    // 红色显示生成文本
    private static void displayGeneratedTextWithRed(String inputText) {
        String cleaned = inputText.toLowerCase().replaceAll("[^a-z\\s]", "");
        String[] words = cleaned.split("\\s+");
        if (words.length <= 1) {
            appendResult(inputText + "\n", Color.BLACK);
            return;
        }

        try {
            Document doc = resultArea.getDocument();
            doc.insertString(doc.getLength(), words[0], null);

            for (int i = 0; i < words.length - 1; i++) {
                String w1 = words[i];
                String w2 = words[i + 1];
                List<String> bridges = getBridgeWords(w1, w2);

                if (!bridges.isEmpty()) {
                    String b = bridges.get(random.nextInt(bridges.size()));
                    doc.insertString(doc.getLength(), " ", null);
                    doc.insertString(doc.getLength(), b, redAttr);
                }

                doc.insertString(doc.getLength(), " " + w2, null);
            }

            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            appendResult("❌ 生成失败\n", Color.RED);
        }
    }

    // 输出文本
    private static void appendResult(String text, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, color);
        try {
            resultArea.getDocument().insertString(resultArea.getDocument().getLength(), text, attr);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // 读取文件并建图
    private static void processFile(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                int c;
                while ((c = br.read()) != -1) {
                    char ch = (char) c;
                    sb.append(Character.isLetter(ch) ? Character.toLowerCase(ch) : ' ');
                }
            }
            String text = sb.toString().trim().replaceAll("\\s+", " ");
            buildGraph(text);
            showDirectedGraph();
            Thread.sleep(800);
        } catch (Exception ex) {
            appendResult("❌ 处理失败：" + ex.getMessage() + "\n", Color.RED);
        }
    }

    // 建图
    private static void buildGraph(String text) {
        graph.clear();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            String f = words[i], t = words[i+1];
            graph.computeIfAbsent(f, k -> new HashMap<>()).put(t, graph.get(f).getOrDefault(t, 0) + 1);
        }
    }

    // 展示有向图
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
            appendResult("❌ 生成图失败：" + e.getMessage() + "\n", Color.RED);
        }
    }

    // 查询桥接词
    public static String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase().replaceAll("[^a-z]", "");
        word2 = word2.toLowerCase().replaceAll("[^a-z]", "");

        Set<String> allWords = new HashSet<>();
        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
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

        List<String> bridges = new ArrayList<>();
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
        sb.append("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
        for (int i = 0; i < bridges.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(bridges.get(i)).append("\"");
        }
        sb.append(".");
        return sb.toString();
    }

    // 获取桥接词列表
    private static List<String> getBridgeWords(String w1, String w2) {
        w1 = w1.toLowerCase().replaceAll("[^a-z]", "");
        w2 = w2.toLowerCase().replaceAll("[^a-z]", "");
        List<String> bridges = new ArrayList<>();
        if (graph.containsKey(w1)) {
            for (String w3 : graph.get(w1).keySet()) {
                if (graph.containsKey(w3) && graph.get(w3).containsKey(w2)) {
                    bridges.add(w3);
                }
            }
        }
        return bridges;
    }
}