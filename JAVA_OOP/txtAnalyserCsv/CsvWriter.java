import java.io.FileWriter;
import java.io.IOException;

public class CsvWriter extends FileWriter {
    public CsvWriter(String fileName) throws IOException {
        super(fileName);
    }

    //Подразумевается, что сюда передают именно экземпляр класса Word
    public void write(Object[] array, long wordAmount) throws IOException, IllegalArgumentException {
        if(array.length > 0){
            if(array[0] instanceof Word){
                for(Object obj : array){
                    Word w = (Word) obj;
                    super.write(w.toString()+","+w.getCounter()+","+w.getCounter()*1.0/wordAmount*100+"%\n");
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

}
