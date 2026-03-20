import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
        panel.add(new JLabel("新文本:"));
        panel.add(newTextField);
        panel.add(btnGenerate);

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
            appendResult("📝 生成新文本：", Color.BLACK);
            generateNewTextWithColor(inputText);
        });

        frame.setVisible(true);
    }

    private static void appendResult(String text, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, color);
        try {
            resultArea.getDocument().insertString(resultArea.getDocument().getLength(), text, attr);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static void generateNewTextWithColor(String inputText) {
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
                java.util.List<String> bridges = getBridgeWords(w1, w2);

                if (!bridges.isEmpty()) {
                    String bridge = bridges.get(random.nextInt(bridges.size()));
                    doc.insertString(doc.getLength(), " ", null);
                    doc.insertString(doc.getLength(), bridge, redAttr);
                }
                doc.insertString(doc.getLength(), " " + w2, null);
            }
            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            appendResult("❌ 生成失败\n", Color.RED);
        }
    }

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
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("text/temp.txt"))) {
                bw.write(text);
            }
            buildGraph(text);
            showDirectedGraph();
            Thread.sleep(800);
        } catch (Exception ex) {
            appendResult("❌ 处理失败：" + ex.getMessage() + "\n", Color.RED);
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
            appendResult("❌ 生成图失败：" + e.getMessage() + "\n", Color.RED);
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase().replaceAll("[^a-z]", "");
        word2 = word2.toLowerCase().replaceAll("[^a-z]", "");

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

    private static java.util.List<String> getBridgeWords(String w1, String w2) {
        w1 = w1.toLowerCase().replaceAll("[^a-z]", "");
        w2 = w2.toLowerCase().replaceAll("[^a-z]", "");

        java.util.List<String> bridges = new java.util.ArrayList<>();
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