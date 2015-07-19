/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.agents;

/**
 *
 * @author Marco
 */
public class RequestCFP {
    
    private double[] destination;
    private double[] location;

    /**
     * @return the destination
     */
    public double[] getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(double[] destination) {
        this.destination = destination;
    }

    /**
     * @return the location
     */
    public double[] getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(double[] location) {
        this.location = location;
    }
}
