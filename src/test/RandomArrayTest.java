package test;

public class RandomArrayTest {

    public double sumElements(double[][] a) {
        double tot = 0;
        for (double[] aa : a) {
            for (double x : aa) {
                tot += x;
            }
        }
        return tot;
    }

}
