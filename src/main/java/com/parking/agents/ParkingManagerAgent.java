/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.agents;

import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import com.parking.persistence.mongo.documents.Parking;
import jade.core.Agent;

/**
 *
 * @author Marco
 */
public class ParkingManagerAgent extends Agent{
    
    private PersistenceManager persistence;
    private String name;
    private Iterable<Parking> parkingsList;
    
    //method to initialize agent
    protected void setup(){
        
        System.out.println("Hello! Im Parking Manager Agent. My id is: "+getAID().getName());
        persistence = PersistenceWrapper.get();
        name = getAID().getLocalName();
        parkingsList = persistence.getParkingByManager(name);
        
    }
}
