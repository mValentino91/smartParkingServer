/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.negotiation;

/**
 *
 * @author Marco
 */
public class ParkingManagerUtilityCalculator implements UtilityCalculator {

    @Override //media pesata dei parametri
    public double calculate(double[] params, double[] weights) {
        double sum1 = 0, sum2 = 0;
        int index = 0;
        for (double w : weights) {
            sum1 += params[index] * w;
            sum2 += w;
            index++;
        }
        return sum1 / sum2;
    }
}
