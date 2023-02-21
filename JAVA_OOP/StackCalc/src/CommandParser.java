import java.io.*;
import java.util.Scanner;

public class CommandParser implements AutoCloseable {
    public CommandParser()  {
        linkWithConsole();
    }
    public CommandParser(String filePath) throws FileNotFoundException {
        linkWithFile(filePath);
    }
    public CommandParser(String[] args) throws FileNotFoundException{
        if(args.length == 0){
            linkWithConsole();
        } else {
            linkWithFile(args[0]);
        }
    }

    public boolean ready() throws IOException {
        return inStream.hasNextLine();
    }

    public String nextLine() throws IOException {
        return inStream.nextLine();
    }

    public void linkWithFile(String filePath) throws FileNotFoundException {
        inStream = new Scanner(new FileReader(filePath));
    }
    public void linkWithConsole() {
        inStream = new Scanner(new InputStreamReader(System.in));
    }

    @Override
    public void close() {
        inStream.close();
    }

    private Scanner inStream = null;
}
