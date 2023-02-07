import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Main {
    public static void main(String[] args) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(args[0]));



        } catch (IOException ioe) {
            System.out.println("Error while reading the file: "+ioe.getMessage());
        }
        finally {
            if(reader != null){
                try{
                    reader.close();
                } catch(IOException ioe){
                    System.out.println("Error while closing the file: "+ioe.getMessage());
                    ioe.printStackTrace(System.err);
                }
            }
        }
    }
}
