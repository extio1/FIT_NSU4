package calcException;

public class FactoryException extends Throwable{
   @Override
   public String getMessage(){
       return "FactoryException: ";
   }
}
