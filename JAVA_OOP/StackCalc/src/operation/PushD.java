package operation;

import calcException.NotDefined;
import calcException.NotEnoughOperandToConfigure;
import calcException.WrongFormatInputData;
import main.Main;
import runtimeContext.RuntimeContext;
import stack.MyStack;

import java.util.Objects;
import java.util.logging.Level;

public class PushD implements StackOperation<Double>, CustomizableOperation {
    private String arg;

    @Override
    public void apply(MyStack<Double> stack, RuntimeContext<Double> context) throws NotDefined {
        try {
            Double defined = context.getIfDefined(arg);
            stack.push(Objects.requireNonNullElseGet(defined, () -> Double.parseDouble(arg)));
        } catch (NumberFormatException e){
            throw new NotDefined(this, arg);
        }
    }

    @Override
    public void set(String[] option) throws NotEnoughOperandToConfigure {
        if(option.length < 2){
            throw new NotEnoughOperandToConfigure(this);
        } else {
            arg = option[1];
            Main.logger.log(Level.INFO, this.toString()+" is configured by "+arg);
        }
    }
}
