/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.agents;

import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import jade.core.Agent;

/**
 *
 * @author Marco
 */
public class UserAgent extends Agent {

    private PersistenceManager persistence;
    private double location[];
    private double destination[];
    private double threshold = 0.6; 

    //method to initialize agent
    protected void setup() {

        System.out.println("Hello! Im User Agent. My id is: " + getAID().getName());
        persistence = PersistenceWrapper.get();
        //get alla input arguments
        Object[] args = getArguments();
        if(args!=null){
            location = (double[]) args[0];
            destination = (double[]) args[1];
        }
        //get all sellers
        //start negotiation
    }
}
