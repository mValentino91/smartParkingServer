/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.controller;

import com.parking.persistence.mongo.documents.Parking;
import com.parking.persistence.mongo.documents.ParkingManager;
import com.parking.persistence.mongo.repositories.ParkingRepository;
import java.util.ArrayList; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

/**
 *
 * @author Marco Valentino
 */
@Controller
@RequestMapping(value = "/service")
public class ServicesManager {

    @Autowired
    ParkingRepository repository;

    @RequestMapping(value = "/test")
    public @ResponseBody
    String get(WebRequest request) {
        
        Parking p = new Parking();
        p.setName("test");
        ParkingManager m = new ParkingManager();
        ArrayList<Parking> list = new ArrayList<Parking>();
        list.add(p);
        m.setParkings(list);
        repository.save(m);
        
        return "ok";
    }
    
}

    
