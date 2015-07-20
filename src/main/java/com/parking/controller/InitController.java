/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import com.parking.managers.AgentsManager;
import com.parking.persistence.mongo.documents.Parking;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Marco Valentino
 */
@Controller
@Scope("application")
public class InitController {

    @Autowired
    private PersistenceManager persistence;

    private Gson gson = new Gson();

    @RequestMapping(value = "/initEnvironment")
    public @ResponseBody
    String init(HttpServletRequest request) {
        //recuperare valori dei pesi
        PersistenceWrapper.set(persistence);
        try {
            AgentsManager.startEnvironment();
        } catch (ControllerException ex) {
            Logger.getLogger(InitController.class.getName()).log(Level.SEVERE, null, ex);
            return "Errore nell'inizializzazione dell'ambiente jade";
        }
        try {
            AgentsManager.startParkingManagerAgents();
        } catch (StaleProxyException ex) {
            Logger.getLogger(InitController.class.getName()).log(Level.SEVERE, null, ex);
            return "Errore nella creazione dei manager dei parkeggi";
        }
        return "inizializzazione avvenuta con successo!";
    }

    @RequestMapping(value = "/initUserAgent")
    public @ResponseBody
    String initUserAgent(HttpServletRequest request) {
        String requestJson = request.getParameter("requestJson");
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(requestJson);
        JsonElement obj = json.getAsJsonArray().get(0);
        JsonElement partenza = obj.getAsJsonObject().get("partenza").getAsJsonArray();
        JsonElement destinazione = obj.getAsJsonObject().get("arrivo").getAsJsonArray();
        double location[] = {partenza.getAsJsonArray().get(0).getAsDouble(), partenza.getAsJsonArray().get(1).getAsDouble()};
        double destination[] = {destinazione.getAsJsonArray().get(0).getAsDouble(), destinazione.getAsJsonArray().get(1).getAsDouble()};
        double weights[] = {
            obj.getAsJsonObject().get("prezzo").getAsDouble(),
            obj.getAsJsonObject().get("distanza").getAsDouble(),
            obj.getAsJsonObject().get("tempo").getAsDouble()
        };
        double treshold = obj.getAsJsonObject().get("soglia").getAsDouble();
        int res = AgentsManager.startUserAgent(request.getSession().getId(), location, destination, weights, treshold);
        //create json response
        JsonObject response = new JsonObject();
        JsonObject code = new JsonObject();
        code.addProperty("code", res);
        response.add("response", code);
        return gson.toJson(response);
    }

    /*@RequestMapping(value = "/testJson")
     public @ResponseBody
     String test(HttpServletRequest request) {
     Parking p = new Parking();
     p.setName("prova");
     p.setAddress("prova");
     p.setCapacity(1);
     p.setIsFull(false);
     p.setLocation(new double[]{0,0});
     p.setOccupied(0);
     p.setParkingManagerId("prova");
     p.setPrice(2);
     p.setZone(3);
     persistence.saveParking(p);
     Gson gson = new Gson();
     Iterable<Parking> list = persistence.getAllParking();
     for (Parking parking : list) {
            
     return gson.toJson(parking);
            
     }
     return "no parkings";
     }*/
}
