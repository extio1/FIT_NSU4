import java.io.*;
import java.util.Objects;
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

    public boolean exit(){
        return exit;
    }

    public boolean ready() throws IOException {
        return inStream.hasNextLine();
    }

    public String nextLine() throws IOException {
        String line = inStream.nextLine();
        exit = Objects.equals(line, "QUIT");
        if(line.startsWith("#")) {
            if(ready()) {
                nextLine();
            } else {
                exit = true;
            }
        }
        return line;
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

    boolean exit = false;
    private Scanner inStream = null;
}
