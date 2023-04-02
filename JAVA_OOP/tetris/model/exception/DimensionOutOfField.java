package model.exception;

public class DimensionOutOfField extends Exception{
    private final int passedDim;
    private final int realDim;
    public DimensionOutOfField(int passedDim, int realDim){
        this.passedDim = passedDim;
        this.realDim = realDim;
    }

    @Override
    public String getMessage(){
        return "Dimension "+passedDim+" out of dimension "+realDim+".";
    }

}
