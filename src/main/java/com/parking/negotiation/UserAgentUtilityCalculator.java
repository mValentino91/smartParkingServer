
package com.parking.negotiation;

/**
 *
 * @author Marco
 */
public class UserAgentUtilityCalculator implements UtilityCalculator {
   
     /**
     * @param params the parameters.
     * @param weights the weights for each params.
     * @return utility
     */
    @Override
    public double calculate(double[] params, double[] weights, double[] normFactors) {
        //normalizzare da 1 a 0
        double sum1 = 0;
        int index = 0;
        for (double w : weights) {
            sum1 += params[index] / normFactors[index] * w;
            index++;
        }
        return sum1;
    }

}
