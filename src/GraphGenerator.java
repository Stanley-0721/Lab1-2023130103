import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class GraphGenerator {

    // 主方法：生成图 + 图片
    public static void generateGraphAndImage() {
        String tempPath = "text/temp.txt";
        String content = "";

        try (BufferedReader br = new BufferedReader(new FileReader(tempPath))) {
            content = br.readLine();
        } catch (IOException e) {
            System.err.println("读取 temp.txt 失败");
            return;
        }

        if (content == null || content.trim().isEmpty()) {
            System.err.println("temp.txt 内容为空");
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

        // 保存 graph.txt
        try (FileWriter writer = new FileWriter("text/graph.txt")) {
            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int w = graph.get(from).get(to);
                    writer.write(from + " -> " + to + " : " + w + "\n");
                }
            }
            System.out.println("✅ 有向图生成 → text/graph.txt");
        } catch (IOException e) {
            System.err.println("写入 graph.txt 失败");
        }

        // 生成图片
        generateImage(graph);
    }

    private static void generateImage(Map<String, Map<String, Integer>> graph) {
        try {
            int width = 1600;
            int height = 1000;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(Color.BLACK);

            // ✅ 修复冲突：写全名 java.util.List
            java.util.List<String> nodes = new ArrayList<>(graph.keySet());
            Map<String, Point> pos = new HashMap<>();
            int r = 350;
            int cx = width / 2;
            int cy = height / 2;

            // 节点布局
            for (int i = 0; i < nodes.size(); i++) {
                double ang = 2 * Math.PI * i / nodes.size();
                int x = cx + (int) (r * Math.cos(ang));
                int y = cy + (int) (r * Math.sin(ang));
                pos.put(nodes.get(i), new Point(x, y));

                g.setColor(new Color(220, 240, 255));
                g.fillOval(x - 35, y - 20, 70, 40);
                g.setColor(Color.BLACK);
                g.drawOval(x - 35, y - 20, 70, 40);
                g.drawString(nodes.get(i), x - 25, y + 5);
            }

            // 画有向边
            g.setColor(new Color(50, 100, 180));
            for (String from : graph.keySet()) {
                Point p1 = pos.get(from);
                for (String to : graph.get(from).keySet()) {
                    Point p2 = pos.get(to);
                    if (p1 == null || p2 == null) continue;

                    drawArrow(g, p1.x, p1.y, p2.x, p2.y);
                    String weight = String.valueOf(graph.get(from).get(to));
                    g.drawString(weight, (p1.x + p2.x) / 2, (p1.y + p2.y) / 2 - 10);
                }
            }

            ImageIO.write(image, "png", new File("text/graph.png"));
            System.out.println("✅ 有向图图片生成 → text/graph.png");
        } catch (Exception e) {
            System.err.println("生成图片失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 10;
        int x3 = x2 - (int) (arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = y2 - (int) (arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = x2 - (int) (arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = y2 - (int) (arrowSize * Math.sin(angle + Math.PI / 6));
        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x2, y2, x4, y4);
    }
}