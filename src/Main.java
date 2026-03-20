import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputFilePath;

        // 选择输入文件
        System.out.println("请选择输入文件：");
        System.out.println("1 - text/Easy Test.txt");
        System.out.println("2 - text/Cursed Be The Treasure.txt");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 消费换行符

        if (choice == 1) {
            inputFilePath = "text/Easy Test.txt";
        } else if (choice == 2) {
            inputFilePath = "text/Cursed Be The Treasure.txt";
        } else {
            System.out.println("无效选择，程序退出。");
            return;
        }

        // 确保text目录存在
        File outputDir = new File("text");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 读取并处理文本
        StringBuilder processedText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            int c;
            while ((c = reader.read()) != -1) {
                char ch = (char) c;
                if (Character.isLetter(ch)) {
                    // 字母统一转为小写
                    processedText.append(Character.toLowerCase(ch));
                } else {
                    // 非字母字符（换行、标点、数字等）都替换为空格
                    processedText.append(' ');
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件时发生错误：" + e.getMessage());
            return;
        }

        // 处理连续空格，避免多个空格连在一起
        String result = processedText.toString().replaceAll("\\s+", " ").trim();

        // 写入到temp.txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("text/temp.txt"))) {
            writer.write(result);
            System.out.println("处理完成，结果已输出到 text/temp.txt");
        } catch (IOException e) {
            System.err.println("写入文件时发生错误：" + e.getMessage());
        }

        scanner.close();
    }
}