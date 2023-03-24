package tetrisModel;

public enum Figure {
    leftAngle   (new int[]{0,1,1,1,  0,0,0,1}),
    rightAngle  (new int[]{0,0,0,1,  0,1,1,1}),
    leftSnake   (new int[]{0,1,1,0,  0,0,1,1}),
    rightSnake  (new int[]{0,0,1,1,  1,1,0,0}),
    pedestal    (new int[]{0,0,1,0,  0,1,1,1}),
    stick       (new int[]{0,0,0,0,  1,1,1,1});

    private final int[] innerView;
    Figure(int[] view){
        innerView = view;
    }


}
