package com.parking.persistence.mongo.documents;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Marco
 */
@Document(collection = "parking")
public class Parking extends Collection {

    private String name;
    private String parkingManagerId;
    private String address;
    private int zone;
    private int capacity;
    private int occupied;
    private float price;
    private boolean isFull;
    private double[] location;
    private double utility;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the parkingManagerId
     */
    public String getParkingManagerId() {
        return parkingManagerId;
    }

    /**
     * @param parkingManagerId the parkingManagerId to set
     */
    public void setParkingManagerId(String parkingManagerId) {
        this.parkingManagerId = parkingManagerId;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the occupied
     */
    public int getOccupied() {
        return occupied;
    }

    /**
     * @param occupied the occupied to set
     */
    public void setOccupied(int occupied) {
        this.occupied = occupied;
    }

    /**
     * @return the price
     */
    public float getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(float price) {
        this.price = price;
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

    /**
     * @return the zone
     */
    public int getZone() {
        return zone;
    }

    /**
     * @param zone the zone to set
     */
    public void setZone(int zone) {
        this.zone = zone;
    }

    /**
     * @return the utility
     */
    public double getUtility() {
        return utility;
    }

    /**
     * @param utility the utility to set
     */
    public void setUtility(double utility) {
        this.utility = utility;
    }
}
