import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();
    private static JTextPane resultArea;
    private static JLabel imageLabel;
    private static JPanel centerPanel;
    private static Random random = new Random();
    private static SimpleAttributeSet redAttr;

    // PageRank 参数
    private static final double DAMPING_FACTOR = 0.85;
    private static final double CONVERGENCE_THRESHOLD = 1e-6;
    private static final int MAX_ITERATIONS = 1000;
    private static Map<String, Double> prValues = new HashMap<>();

    // 随机游走控制
    private static volatile boolean isWalking = false;
    private static volatile boolean stopWalk = false;

    public static void main(String[] args) {
        redAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(redAttr, Color.RED);

        JFrame frame = new JFrame("Lab1");
        frame.setSize(950, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // ===================== 顶部面板分两行 =====================
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // 第一行：所有按钮 + 单词输入
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JButton btnLoadGen = new JButton("1. 加载文本并生成有向图");
        JButton btnShowGraph = new JButton("2. 展示有向图");
        JButton btnQuery = new JButton("3. 查询桥接词");
        JButton btnGenerate = new JButton("4. 生成新文本");
        JButton btnShortestPath = new JButton("5. 查询最短路径");
        JButton btnPageRank = new JButton("6. 查询PageRank");
        JButton btnRandomWalk = new JButton("7. 随机游走");
        JButton btnStopWalk = new JButton("停止游走");

        JTextField word1Field = new JTextField(6);
        JTextField word2Field = new JTextField(6);

        row1.add(btnLoadGen);
        row1.add(btnShowGraph);
        row1.add(new JLabel("单词1:"));
        row1.add(word1Field);
        row1.add(new JLabel("单词2:"));
        row1.add(word2Field);
        row1.add(btnQuery);
        row1.add(btnGenerate);
        row1.add(btnShortestPath);
        row1.add(btnPageRank);
        row1.add(btnRandomWalk);
        row1.add(btnStopWalk);

        // 第二行：新文本输入框
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JTextField newTextField = new JTextField(40);
        row2.add(new JLabel("新文本输入:"));
        row2.add(newTextField);

        topPanel.add(row1);
        topPanel.add(row2);

        // ===================== 中央显示区域 =====================
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        resultArea = new JTextPane();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane imageScroll = new JScrollPane(imageLabel);

        centerPanel.add(scrollPane);
        centerPanel.add(imageScroll);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        // 1. 加载文件
        btnLoadGen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("text");
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                processFile(file.getAbsolutePath());
                appendResult("✅ 已加载：" + file.getName() + "，有向图已生成！\n", Color.BLACK);
            }
        });

        // 2. 展示有向图
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
                appendResult("✅ 有向图已显示！\n", Color.BLACK);
            } catch (Exception ex) {
                appendResult("❌ 图片加载失败！\n", Color.RED);
            }
        });

        // 3. 查询桥接词
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

        // 4. 生成新文本
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

        // 5. 最短路径
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

        // 6. 查询PageRank
        btnPageRank.addActionListener(e -> {
            String word = word1Field.getText().trim();
            if (word.isEmpty()) {
                appendResult("❌ 请输入要查询的单词！\n", Color.RED);
                return;
            }
            if (graph.isEmpty()) {
                appendResult("❌ 请先执行功能1！\n", Color.RED);
                return;
            }
            Double pr = calPageRank(word);
            appendResult(String.format("🔍 单词 \"%s\" 的PageRank值：%.6f\n", word, pr), Color.BLACK);
        });

        // 7. 随机游走（可停止、实时显示、带延迟）
        btnRandomWalk.addActionListener(e -> {
            if (graph.isEmpty()) {
                appendResult("❌ 请先执行功能1！\n", Color.RED);
                return;
            }
            if (isWalking) {
                appendResult("⚠️ 正在游走中，请先停止！\n", Color.RED);
                return;
            }
            new Thread(() -> {
                appendResult("\n==================== 随机游走 ====================\n", Color.BLACK);
                String path = randomWalk();
                if (stopWalk) {
                    appendResult("\n🛑 已手动停止！\n", Color.RED);
                } else {
                    appendResult("\n✅ 游走完成！\n", Color.BLACK);
                }
                try (FileWriter fw = new FileWriter("random_walk.txt")) {
                    fw.write(path);
                    appendResult("📄 路径已保存：random_walk.txt\n", Color.BLACK);
                } catch (IOException ex) {
                    appendResult("❌ 写入文件失败\n", Color.RED);
                }
            }).start();
        });

        // 停止游走
        btnStopWalk.addActionListener(e -> {
            if (isWalking) {
                stopWalk = true;
                appendResult("🛑 正在停止游走...\n", Color.RED);
            } else {
                appendResult("⚠️ 当前未在游走\n", Color.RED);
            }
        });

        frame.setVisible(true);
    }

    // ===================== 随机游走（可停止、实时显示） =====================
    public static String randomWalk() {
        isWalking = true;
        stopWalk = false;

        Set<String> allNodes = new HashSet<>();
        for (var entry : graph.entrySet()) {
            allNodes.add(entry.getKey());
            allNodes.addAll(entry.getValue().keySet());
        }
        if (allNodes.isEmpty()) return "无节点";

        List<String> nodeList = new ArrayList<>(allNodes);
        String current = nodeList.get(random.nextInt(nodeList.size()));
        List<String> path = new ArrayList<>();
        Set<String> usedEdges = new HashSet<>();
        path.add(current);

        appendResult("起点：" + current, Color.BLUE);

        while (!stopWalk) {
            if (!graph.containsKey(current)) break;
            Map<String, Integer> nextNodes = graph.get(current);
            if (nextNodes.isEmpty()) break;

            List<String> nextList = new ArrayList<>(nextNodes.keySet());
            String next = nextList.get(random.nextInt(nextList.size()));
            String edge = current + "->" + next;

            if (usedEdges.contains(edge)) {
                appendResult(" 🛑(边重复)", Color.RED);
                break;
            }

            usedEdges.add(edge);
            path.add(next);
            appendResult(" → " + next, Color.BLACK);
            current = next;

            try { Thread.sleep(350); } catch (Exception ignored) {}
        }

        isWalking = false;
        return String.join(" ", path);
    }

    // ===================== PageRank =====================
    public static Double calPageRank(String word) {
        if (graph.isEmpty()) return 0.0;
        computePageRank();
        word = word.toLowerCase().replaceAll("[^a-z]", "");
        return prValues.getOrDefault(word, 0.0);
    }

    private static void computePageRank() {
        Set<String> nodes = new HashSet<>();
        for (var entry : graph.entrySet()) {
            nodes.add(entry.getKey());
            nodes.addAll(entry.getValue().keySet());
        }
        int N = nodes.size();
        if (N == 0) return;

        Map<String, Double> pr = new HashMap<>();
        Map<String, Integer> outDegree = new HashMap<>();
        for (String node : nodes) {
            pr.put(node, 1.0 / N);
            outDegree.put(node, graph.getOrDefault(node, Collections.emptyMap()).size());
        }

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Map<String, Double> newPR = new HashMap<>();
            double sinkSum = 0.0;
            for (String node : nodes) {
                if (outDegree.get(node) == 0) sinkSum += pr.get(node);
            }

            double maxDiff = 0.0;
            for (String u : nodes) {
                double sum = 0.0;
                for (String v : nodes) {
                    if (graph.containsKey(v) && graph.get(v).containsKey(u)) {
                        sum += pr.get(v) / outDegree.get(v);
                    }
                }
                double val = (1 - DAMPING_FACTOR) / N + DAMPING_FACTOR * (sum + sinkSum / N);
                newPR.put(u, val);
                maxDiff = Math.max(maxDiff, Math.abs(val - pr.get(u)));
            }
            pr = newPR;
            if (maxDiff < CONVERGENCE_THRESHOLD) break;
        }
        prValues = pr;
    }

    // ===================== 加权最短路径 =====================
    private static String calcShortestPath(String word1, String word2) {
        String start = word1.toLowerCase().replaceAll("[^a-z]", "");
        String end = word2.toLowerCase().replaceAll("[^a-z]", "");

        Set<String> allWords = new HashSet<>();
        for (var entry : graph.entrySet()) {
            allWords.add(entry.getKey());
            allWords.addAll(entry.getValue().keySet());
        }

        if (!allWords.contains(start)) return "❌ 不存在：" + start;
        if (!allWords.contains(end)) return "❌ 不存在：" + end;
        if (start.equals(end)) return "❌ 起点终点相同";

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (String w : allWords) {
            dist.put(w, Integer.MAX_VALUE);
            prev.put(w, null);
        }
        dist.put(start, 0);
        pq.add(new AbstractMap.SimpleEntry<>(start, 0));

        while (!pq.isEmpty()) {
            String u = pq.poll().getKey();
            if (u.equals(end)) break;
            if (!graph.containsKey(u)) continue;

            for (String v : graph.get(u).keySet()) {
                int weight = graph.get(u).get(v);
                if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + weight < dist.get(v)) {
                    dist.put(v, dist.get(u) + weight);
                    prev.put(v, u);
                    pq.add(new AbstractMap.SimpleEntry<>(v, dist.get(v)));
                }
            }
        }

        if (dist.get(end) == Integer.MAX_VALUE) return "📶 不可达";

        List<String> path = new ArrayList<>();
        for (String cur = end; cur != null; cur = prev.get(cur)) path.add(cur);
        Collections.reverse(path);

        StringBuilder sb = new StringBuilder("📶 加权最短路径（权重" + dist.get(end) + "）：");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) sb.append(" → ");
            sb.append(path.get(i));
        }
        return sb.toString();
    }

    // ===================== 工具方法 =====================
    private static void queryShortestPath(String start, String end) {
        String result = calcShortestPath(start, end);
        appendResult(result + "\n", Color.BLACK);
        if (result.contains("→")) {
            List<String> path = new ArrayList<>();
            try { path = Arrays.asList(result.split("：")[1].split(" → ")); } catch (Exception ignored) {}
            highlightShortestPath(path);
            try {
                BufferedImage img = ImageIO.read(new File("graph/highlighted_graph.png"));
                Image scaled = img.getScaledInstance(Math.min(img.getWidth(), 1200), Math.min(img.getHeight(), 400), Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                appendResult("✅ 路径已高亮\n", Color.BLACK);
            } catch (Exception e) { appendResult("❌ 高亮图失败\n", Color.RED); }
        }
    }

    private static void highlightShortestPath(List<String> path) {
        try {
            Set<String> es = new HashSet<>();
            for (int i = 0; i < path.size()-1; i++) es.add(path.get(i)+"->"+path.get(i+1));
            StringBuilder dot = new StringBuilder("digraph G { rankdir=LR; node [shape=circle,style=filled,color=lightblue];\n");
            for (String f : graph.keySet()) for (String t : graph.get(f).keySet()) {
                if (es.contains(f+"->"+t)) dot.append("\""+f+"\"->\""+t+"\"[label="+graph.get(f).get(t)+",color=red,penwidth=3];\n");
                else dot.append("\""+f+"\"->\""+t+"\"[label="+graph.get(f).get(t)+"];\n");
            }
            dot.append("}");
            new File("graph").mkdirs();
            Files.writeString(Path.of("graph/highlighted_graph.dot"), dot);
            Process p = Runtime.getRuntime().exec("dot -Tpng graph/highlighted_graph.dot -o graph/highlighted_graph.png");
            p.waitFor();
        } catch (Exception e) { appendResult("❌ 生成高亮图失败\n", Color.RED); }
    }

    private static void displayGeneratedTextWithRed(String inputText) {
        // 严格清洗单词，只保留字母和空格，转小写
        String cleanText = inputText.toLowerCase().replaceAll("[^a-z\\s]", " ").replaceAll("\\s+", " ").trim();
        String[] words = cleanText.split(" ");

        if (words.length <= 1) {
            appendResult(inputText + "\n", Color.BLACK);
            return;
        }

        try {
            Document doc = resultArea.getDocument();
            // 先输出第一个单词
            doc.insertString(doc.getLength(), words[0], null);

            for (int i = 0; i < words.length - 1; i++) {
                String w1 = words[i];
                String w2 = words[i + 1];
                List<String> bridges = getBridgeWords(w1, w2);

                // 有桥接词：插入 空格 + 红色桥接词
                if (!bridges.isEmpty()) {
                    String bridge = bridges.get(random.nextInt(bridges.size()));
                    doc.insertString(doc.getLength(), " ", null);
                    doc.insertString(doc.getLength(), bridge, redAttr);
                }
                // 插入下一个单词
                doc.insertString(doc.getLength(), " " + w2, null);
            }

            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            appendResult("❌ 生成失败\n", Color.RED);
        }
    }

    private static List<String> getBridgeWords(String w1, String w2) {
        List<String> bridges = new ArrayList<>();
        if (graph.containsKey(w1)) for (String w3 : graph.get(w1).keySet())
            if (graph.containsKey(w3) && graph.get(w3).containsKey(w2)) bridges.add(w3);
        return bridges;
    }

    private static void appendResult(String text, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, color);
        try {
            resultArea.getDocument().insertString(resultArea.getDocument().getLength(), text, attr);
        } catch (BadLocationException e) {}
    }

    private static void processFile(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                int c; while ((c = br.read()) != -1)
                    sb.append(Character.isLetter((char)c) ? Character.toLowerCase((char)c) : ' ');
            }
            buildGraph(sb.toString().replaceAll("\\s+", " "));
            showDirectedGraph();
        } catch (Exception ex) { appendResult("❌ 处理失败\n", Color.RED); }
    }

    private static void buildGraph(String text) {
        graph.clear();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            String f = words[i], t = words[i+1];
            graph.computeIfAbsent(f, k->new HashMap<>()).put(t, graph.get(f).getOrDefault(t,0)+1);
        }
    }

    public static void showDirectedGraph() {
        try {
            new File("graph").mkdirs();
            StringBuilder dot = new StringBuilder("digraph G { rankdir=LR; node [shape=circle,style=filled,color=lightblue];\n");
            for (String from : graph.keySet()) for (String to : graph.get(from).keySet())
                dot.append("\"").append(from).append("\"->\"").append(to).append("\" [label=").append(graph.get(from).get(to)).append("];\n");
            dot.append("}");
            Files.writeString(Path.of("graph/graph.dot"), dot);
            Process p = Runtime.getRuntime().exec("dot -Tpng graph/graph.dot -o graph/graph.png");
            p.waitFor();
        } catch (Exception e) { appendResult("❌ 生成图失败\n", Color.RED); }
    }

    // ===================== 【已修改】查询桥接词方法（完全匹配实验手册格式） =====================
    public static String queryBridgeWords(String word1, String word2) {
        // 清洗输入，保留纯字母并转小写
        word1 = word1.toLowerCase().replaceAll("[^a-z]", "");
        word2 = word2.toLowerCase().replaceAll("[^a-z]", "");

        // 收集图中所有节点
        Set<String> allNodes = new HashSet<>();
        for (var entry : graph.entrySet()) {
            allNodes.add(entry.getKey());
            allNodes.addAll(entry.getValue().keySet());
        }

        // 场景1：word1 或 word2 不在图中
        if (!allNodes.contains(word1) || !allNodes.contains(word2)) {
            return String.format("No \"%s\" and \"%s\" in the graph!", word1, word2);
        }

        // 场景2：获取桥接词
        List<String> bridgeWords = getBridgeWords(word1, word2);

        // 场景3：无桥接词
        if (bridgeWords.isEmpty()) {
            return String.format("No bridge words from \"%s\" to \"%s\"!", word1, word2);
        }

        // 场景4：有桥接词，按数量格式化输出
        StringBuilder sb = new StringBuilder();
        if (bridgeWords.size() == 1) {
            // 单个桥接词
            sb.append(String.format("The bridge words from \"%s\" to \"%s\" is: \"%s\"",
                    word1, word2, bridgeWords.get(0)));
        } else if (bridgeWords.size() == 2) {
            // 两个桥接词
            sb.append(String.format("The bridge words from \"%s\" to \"%s\" are: \"%s\" and \"%s\"",
                    word1, word2, bridgeWords.get(0), bridgeWords.get(1)));
        } else {
            // 多个桥接词（逗号分隔，最后一个用and）
            for (int i = 0; i < bridgeWords.size(); i++) {
                if (i == 0) {
                    sb.append(String.format("The bridge words from \"%s\" to \"%s\" are: \"%s\"",
                            word1, word2, bridgeWords.get(i)));
                } else if (i == bridgeWords.size() - 1) {
                    sb.append(String.format(", and \"%s\"", bridgeWords.get(i)));
                } else {
                    sb.append(String.format(", \"%s\"", bridgeWords.get(i)));
                }
            }
        }
        sb.append(".");
        return sb.toString();
    }
}