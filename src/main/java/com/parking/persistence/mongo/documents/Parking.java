/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.persistence.mongo.documents;

import org.springframework.data.annotation.TypeAlias;

/**
 *
 * @author Marco
 */
@TypeAlias("com.parking.persistence.mongo.documents.Parking")
public class Parking {
    
    private String name;
    private int fascia;
    private int capienza;
    private int occupazione;
    private boolean isFull;
    private double[] location;

    /**
     * @return the Name
     */
    public String getName() {
        return name;
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the fascia
     */
    public int getFascia() {
        return fascia;
    }

    /**
     * @param fascia the fascia to set
     */
    public void setFascia(int fascia) {
        this.fascia = fascia;
    }

    /**
     * @return the capienza
     */
    public int getCapienza() {
        return capienza;
    }

    /**
     * @param capienza the capienza to set
     */
    public void setCapienza(int capienza) {
        this.capienza = capienza;
    }

    /**
     * @return the occupazione
     */
    public int getOccupazione() {
        return occupazione;
    }

    /**
     * @param occupazione the occupazione to set
     */
    public void setOccupazione(int occupazione) {
        this.occupazione = occupazione;
    }

    /**
     * @return the isFull
     */
    public boolean isIsFull() {
        return isFull;
    }

    /**
     * @param isFull the isFull to set
     */
    public void setIsFull(boolean isFull) {
        this.isFull = isFull;
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
