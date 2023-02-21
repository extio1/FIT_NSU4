import java.io.*;

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
        return inStream.ready();
    }

    public String nextLine() throws IOException {
        return inStream.readLine();
    }

    public void linkWithFile(String filePath) throws FileNotFoundException {
        inStream = new BufferedReader(new FileReader(filePath));
    }
    public void linkWithConsole() {
        inStream = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void close() throws Exception {
        inStream.close();
    }

    private BufferedReader inStream = null;
}
