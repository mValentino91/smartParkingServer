/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.managers;

import com.parking.dbManager.PersistenceWrapper;
import com.parking.persistence.mongo.documents.Parking;
import com.parking.persistence.mongo.documents.ParkingManager;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marco
 */
public class AgentsManager {

    private static Map<String, AgentController> hashUserAgent = new HashMap<String, AgentController>();
    private static Map<String, Parking> results = new HashMap<String, Parking>();
    private static Map<String, AgentController> hashParkingManagerAgent = new HashMap<String, AgentController>();
    private static AgentContainer mainContainer;
    private static boolean envStarted = false;

    /**
     * @return the mainContainer
     */
    public static AgentContainer getMainContainer() {
        return mainContainer;
    }

    /**
     * @param aMainContainer the mainContainer to set
     */
    public static void setMainContainer(AgentContainer aMainContainer) {
        mainContainer = aMainContainer;
    }

    public static int startUserAgent(String sessionId, double location[], double destination[], double weights[], double treshold) {
        try {
            //se l'agente per la sessione è già stato creato
            if (hashUserAgent.get(sessionId) != null) {
                return 0;
            }
            //se l'ambiente di esecuzione jade non è stato avviato
            if (!envStarted) {
                try {
                    startEnvironment();
                    startParkingManagerAgents();
                } catch (ControllerException ex) {
                    ex.printStackTrace();
                    System.out.println("Error start jade environment...");
                    return 1;
                }
            }
            //creazione e avvio di un nuovo user agent
            System.out.println("Launching the rma agent on the main container ...");
            AgentsManager.setUserAgent(sessionId, AgentsManager.getMainContainer().createNewAgent(sessionId, "com.parking.agents.UserAgent",
                    new Object[]{location, destination, weights, treshold, results}));
            AgentsManager.getUserAgent(sessionId).start();
            return 0;

        } catch (StaleProxyException e) {
            e.printStackTrace();
            System.out.println("Error launching agent...");
            return 1;
        }
    }

    public static int restartUserAgent(String sessionId) {
        try {
            System.out.println(AgentsManager.getUserAgent(sessionId).getState());
            AgentsManager.getUserAgent(sessionId).activate();
            return 0;

        } catch (StaleProxyException e) {
            //e.printStackTrace();
            System.out.println("SmartSelection (agentRestart): Error launching agent...");
            return 1;
        }

    }

    public static void startEnvironment() throws ControllerException {
        //se l'ambiente è già stato avviato
        if (envStarted) {
            return;
        }
        // Get a hold on JADE runtime
        jade.core.Runtime.instance().shutDown();
        jade.core.Runtime rt = jade.core.Runtime.instance();
        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);
        System.out.print("runtime created\n");
        // Create a default profile
        Profile profile = new ProfileImpl(null, 1200, null);
        System.out.print("profile created\n");
        System.out.println("Launching a whole in-process platform..." + profile);
        AgentsManager.setMainContainer(rt.createMainContainer(profile));
        // now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        System.out.println("Launching the agent container ..." + pContainer);
        AgentContainer cont = rt.createAgentContainer(pContainer);
        cont.start();
        System.out.println("Launching the agent container after ..." + pContainer);
        System.out.println("containers created");
        envStarted = true;
    }

    public static void startParkingManagerAgents() throws StaleProxyException {

        Iterable<ParkingManager> parkingManagers = PersistenceWrapper.get().getAllParkingManager();

        //per ogni parking manager creo un nuovo agente
        for (ParkingManager parkingManager : parkingManagers) {
            //creazione e avvio di un nuovo parking manager agent
            System.out.println("Launching new parking manager agent on the main container ...");
            AgentsManager.setParkingManagerAgent(parkingManager.getName(), AgentsManager.getMainContainer().createNewAgent(parkingManager.getName(), "com.parking.agents.ParkingManagerAgent", new Object[0]));
            AgentsManager.getParkingManagerAgent(parkingManager.getName()).start();
        }
    }

    /**
     * @param sessionId
     * @return the userAgent
     */
    private static AgentController getUserAgent(String sessionId) {
        return hashUserAgent.get(sessionId);
    }

    /**
     * @param sessionId
     * @param userAgent the userAgent to set
     */
    private static void setUserAgent(String sessionId, AgentController userAgent) {
        hashUserAgent.put(sessionId, userAgent);
    }

    /**
     * @param id
     * @return the parkingManagerAgent
     */
    private static AgentController getParkingManagerAgent(String id) {
        return hashParkingManagerAgent.get(id);
    }

    /**
     * @param id
     * @return the parking
     */
    public static Parking getNegotiationState(String id) {
        return results.get(id);
    }

    /**
     * @param sessionId
     * @param userAgent the parkingManagerAgent to set
     */
    private static void setParkingManagerAgent(String id, AgentController parkingManagerAgent) {
        hashParkingManagerAgent.put(id, parkingManagerAgent);
    }
}
