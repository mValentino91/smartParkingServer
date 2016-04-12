package com.parking.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parking.csv.CsvCreator;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import com.parking.managers.AgentsManager;
import com.parking.persistence.mongo.documents.Parking;
import com.parking.persistence.mongo.documents.ParkingManager;
import com.parking.persistence.mongo.documents.XMLParser;
import jade.wrapper.ControllerException;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Marco
 */
@Controller
@Scope("application")
public class InitController {
    
    @Autowired
    private PersistenceManager persistence;
    private CsvCreator csv;
    
    private Gson gson = new Gson();
    
    @RequestMapping(value = "/initEnvironment")
    public @ResponseBody
    String init(HttpServletRequest request) {
        //set persistence manager
        PersistenceWrapper.set(persistence);
        // set csvCreator
        HttpSession session = request.getSession();
        ServletContext sc = session.getServletContext();
        CsvCreator csv = new CsvCreator(sc.getRealPath("/") + "dist" + File.separator, "test.csv");
        PersistenceWrapper.setCsvCreator(csv);
        try {
            AgentsManager.startEnvironment();
        } catch (ControllerException ex) {
            Logger.getLogger(InitController.class.getName()).log(Level.SEVERE, null, ex);
            return "Errore nell'inizializzazione dell'ambiente jade";
        }
        /*try {
         AgentsManager.startParkingManagerAgents();
         } catch (StaleProxyException ex) {
         Logger.getLogger(InitController.class.getName()).log(Level.SEVERE, null, ex);
         return "Errore nella creazione dei manager dei parkeggi";
         }*/
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
    
    @RequestMapping(value = "/testUserAgent")
    public @ResponseBody
    String testUserAgent(HttpServletRequest request) throws InterruptedException {
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
        int res = 0;
        double treshold = obj.getAsJsonObject().get("soglia").getAsDouble();
        PersistenceWrapper.numAgents = 150;
        for (int j = 0; j < 150; j++) {
            res = AgentsManager.startUserAgent("id" + j, location, destination, weights, treshold);
            Thread.sleep(400);
        }
        //create json response
        JsonObject response = new JsonObject();
        JsonObject code = new JsonObject();
        code.addProperty("code", res);
        response.add("response", code);
        return gson.toJson(response);
    }
    
    @RequestMapping(value = "/getState")
    public @ResponseBody
    String getState(HttpServletRequest request) {
        return gson.toJson(AgentsManager.getNegotiationState(request.getSession().getId()));
    }
    
    @RequestMapping(value = "/parsingXML")
    public @ResponseBody
    String parsing(HttpServletRequest request, @RequestParam(value = "lat") float lat, @RequestParam(value = "lon") float lon) {
        HttpSession session = request.getSession();
        ServletContext sc = session.getServletContext();
        XMLParser parser = new XMLParser(sc.getRealPath("/") + "dist" + File.separator + "xmls" + File.separator + "carparks.xml", "place", "parking", lat, lon);
        
        ArrayList<Parking> list = parser.getList();
        for (Parking parking : list) {
            persistence.saveParking(parking);
        }
        return gson.toJson(persistence.getAllParking());
    }
    
    @RequestMapping(value = "/parkingManager")
    public @ResponseBody
    String parkingManager(HttpServletRequest request) {
        
        ParkingManager p1 = new ParkingManager();
        p1.setName("NapoliPark");
        ParkingManager p2 = new ParkingManager();
        p2.setName("ParkingPrisca");
        ParkingManager p3 = new ParkingManager();
        p3.setName("ParcheggiCampania");
        
        persistence.saveParkingManager(p1);
        persistence.saveParkingManager(p2);
        persistence.saveParkingManager(p3);
        
        return gson.toJson(persistence.getAllParkingManager());
    }
    
}
