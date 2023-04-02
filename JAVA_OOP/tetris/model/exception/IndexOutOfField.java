package model.exception;

public class IndexOutOfField extends Exception {
    /**
     *
     * @param passed argument passed to the function
     * @param maximal maximal value that argument can be to not be out of field
     * @param direction the dimension (X, Y etc.)
     */

    private final int passed;
    private final int maximal;
    private final int direction;
    public IndexOutOfField(int passed, int maximal, char direction){
        this.passed = passed;
        this.maximal = maximal;
        this.direction = direction;
    }

    @Override
    public String getMessage(){
        return "Index "+passed+" out of field of size "+maximal+" in "+direction+" direction";
    }

}
