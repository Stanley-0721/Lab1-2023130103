import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TextPreprocessor {

    // 新增：给主类调用的方法
    public static void process() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n请选择输入文件：");
        System.out.println("1 - Cursed Be The Treasure");
        System.out.println("2 - Easy Test");
        System.out.print("输入选项(1/2): ");
        int choice = scanner.nextInt();
        scanner.close();

        String filePath;
        if (choice == 1) {
            filePath = "text/Cursed Be The Treasure.txt";
        } else if (choice == 2) {
            filePath = "text/Easy Test.txt";
        } else {
            System.err.println("无效选项！");
            return;
        }

        StringBuilder cleanedText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (char c : line.toCharArray()) {
                    if (Character.isLetter(c)) {
                        cleanedText.append(Character.toLowerCase(c));
                    } else {
                        cleanedText.append(' ');
                    }
                }
                cleanedText.append(' ');
            }
        } catch (IOException e) {
            System.err.println("读取文件错误: " + e.getMessage());
            return;
        }

        String result = cleanedText.toString().trim().replaceAll("\\s+", " ");

        // 写入 temp.txt
        try (FileWriter writer = new FileWriter("text/temp.txt")) {
            writer.write(result);
            System.out.println("✅ 文本清洗完成 → text/temp.txt");
        } catch (IOException e) {
            System.err.println("写入 temp 失败: " + e.getMessage());
        }
    }
}