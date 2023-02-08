import java.util.Objects;

public class Word implements Comparable<Word> {
    Word() { spelling = ""; counter = 0; }
    Word(String word) { spelling = word; counter = 1; }
    Word(String word, long c) { spelling = word; counter = c; }

    public long getCounter(){
        return counter;
    }

    @Override
    public boolean equals(Object other){
        if(other == null || this.getClass() != other.getClass()) return false;
        Word o = (Word) other;
        return Objects.equals(spelling, o.spelling);
    }

    @Override
    public String toString(){
        return spelling;
    }

    @Override
    public int compareTo(Word other) {
        if(this == other) { return 0; }
        if(Objects.equals(spelling, other.spelling)){
            return 0;
        } else {
            if(counter >= other.counter) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private final String spelling;
    private long counter;
}
