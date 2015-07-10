/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.dbManager;

import com.parking.persistence.mongo.documents.Parking;
import com.parking.persistence.mongo.documents.ParkingManager;

/**
 *
 * @author Marco Valentino
 */
public interface PersistenceManager {
    
    public Iterable<ParkingManager> getAllParkingManager();
    public Iterable<Parking> getAllParking();
    public Iterable<Parking> getParkingByManager(String parkingManagerId);
    
}
