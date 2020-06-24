package net.waals.dogecraft.util;

import java.util.Random;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class RandomUtil {

    public double gaussian(double x) {
        double result = 5*exp(-pow(x,2)/pow(2*35,2))+3;
        return result;
    }

    public double randomGaussianNumber() {
        Random random = new Random();
        double randomDouble = 315 * random.nextDouble();
        return (int) (Math.round(gaussian(randomDouble) * 100.0) / 100.0);
    }
}
