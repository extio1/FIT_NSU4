import java.io.*;

public class Main {
    public static void main(String[] args) {
        Reader reader = null;
        CsvWriter writer = null;
        StatisticSortedSet analyser = new StatisticSortedSet();
        try {
            reader = new InputStreamReader(new FileInputStream("C:/Users/User/IdeaProjects/untitled/src/input.txt"));
            writer = new CsvWriter("C:/Users/User/IdeaProjects/untitled/src/output.txt");

            analyser.analyseFile(reader);
            writer.write(analyser.getData(), analyser.getWordCounter());

        } catch (IOException | IllegalArgumentException ioe) {
            System.out.println(ioe.getMessage());
        } finally {
            if(reader != null && writer != null){
                try{
                    writer.close();
                    reader.close();
                } catch(IOException ioe){
                    System.out.println(ioe.getMessage());
                    ioe.printStackTrace(System.err);
                }
            }
        }
    }
}
