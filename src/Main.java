public class Main {
    public static void main(String[] args) {
        System.out.println("===== 文本清洗 → 有向图 → 图片 全自动 =====");
        TextPreprocessor.process();
        GraphGenerator.generateGraphAndImage();
        System.out.println("\n🎉 全部完成！");
    }
}