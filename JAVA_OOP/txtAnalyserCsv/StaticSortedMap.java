import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StatisticSortedSet implements Statistic{
    StatisticSortedSet(){ data = new HashMap<String, Integer>(); }

    @Override
    public void analyseFile(Reader reader) throws IOException {
        wordCounter = 0;
        StringBuilder builder = new StringBuilder();
        boolean eof = false;
        char symbol;

        while(!eof) {
            symbol = (char) reader.read();
            if(Character.isLetterOrDigit(symbol)) {
                builder.append(symbol);
            } else {
                if(builder.length() > 0) { //method merge map
                    ++wordCounter;
                    String newWord = builder.toString();
                    data.merge(newWord, 1, (x, y) -> x + 1);
                    builder.setLength(0);
                }
            }
            if(!reader.ready()) eof = true;
        }

    }

    @Override
    public Object[] getData(){
        ArrayList<Word> arrayView = new ArrayList<Word>();
        for(Map.Entry<String, Integer> pair : data.entrySet()){
            Word newWord = new Word(pair.getKey(), pair.getValue());
            arrayView.add(newWord);
        }
        Object[] toSort = arrayView.toArray();
        Arrays.sort(toSort);
        return toSort;
    }

    @Override
    public long getWordCounter() {
        return wordCounter;
    }

    private HashMap<String, Integer> data;
    private long wordCounter;
}
