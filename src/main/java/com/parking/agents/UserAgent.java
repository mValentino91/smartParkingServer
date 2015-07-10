/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.agents;

import jade.core.Agent;

/**
 *
 * @author Marco
 */
public class UserAgent extends Agent{
    
    //method to initialize agent
    protected void setup(){
        
        System.out.println("Hello! Im User Agent. My id is: "+getAID().getName());
    }
    
}
