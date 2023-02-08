import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StatisticSortedSet implements Statistic{
    StatisticSortedSet(){ data = new TreeSet<Word>(); }

    @Override
    public void analyseFile(Reader reader) {
        wordCounter = 0;
        StringBuilder builder = new StringBuilder();
        boolean eof = false;
        char symbol;
        try {
            while(!eof) {
                symbol = (char) reader.read();
                if(Character.isLetterOrDigit(symbol)) {
                    builder.append(symbol);
                } else {
                    if(builder.length() > 0) {
                        ++wordCounter;
                        Word newWord = new Word(builder.toString());
                        if(data.add(newWord)){
                            System.out.println(newWord.toString());
                        }
                        builder.setLength(0);
                    }
                }
                if(!reader.ready()) eof = true;
            }
        } catch (IOException ioe) {
            System.out.println("Error while reading the file: " + ioe.getMessage());
        }

    }

    @Override
    public Object[] getData(){
        Object[] arrayView = data.toArray();
        Arrays.sort(arrayView);
        return arrayView;
    }

    @Override
    public long getWordCounter() {
        return wordCounter;
    }

    private TreeSet<Word> data;
    private long wordCounter;
}
