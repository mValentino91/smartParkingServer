/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.managers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.Map;
import org.springframework.beans.factory.support.ManagedMap;

/**
 *
 * @author Marco
 */
public class AgentsManager {

    private static Map<String, AgentController> hashUserAgent = new ManagedMap<String, AgentController>();
    private static AgentContainer mainContainer;
    private static boolean envStarted = false;

    /**
     * @param sessionId
     * @return the userAgent
     */
    public static AgentController getUserAgent(String sessionId) {
        return hashUserAgent.get(sessionId);
    }

    /**
     * @param sessionId
     * @param userAgent the userAgent to set
     */
    public static void setUserAgent(String sessionId, AgentController userAgent) {
        hashUserAgent.put(sessionId, userAgent);
    }

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

    public static void start(String sessionId) {
        try {
            //se l'agente per la sessione è già stato creato
            if (hashUserAgent.get(sessionId) != null) {
                return;
            }
            //se l'ambiente di esecuzione jade non è stato avviato
            if (!envStarted) {
                startEnvironment();
            }
            //creazione e avvio di un nuovo user agent
            System.out.println("Launching the rma agent on the main container ...");
            AgentsManager.setUserAgent(sessionId, AgentsManager.getMainContainer().createNewAgent(sessionId, "com.parking.agents.UserAgent", new Object[0]));
            AgentsManager.getUserAgent(sessionId).start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
            System.out.println("Error launching agent...");
        }
    }

    public static void restart(String sessionId) {
        try {
            System.out.println(AgentsManager.getUserAgent(sessionId).getState());
            AgentsManager.getUserAgent(sessionId).activate();

        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("SmartSelection (agentRestart): Error launching agent...");
        }

    }

    private static void startEnvironment() {
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
        try {
            cont.start();
        } catch (ControllerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Launching the agent container after ..." + pContainer);
        System.out.println("containers created");
        envStarted = true;
    }

}
