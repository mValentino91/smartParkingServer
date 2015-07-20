package com.parking.agents;

import com.google.gson.Gson;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import com.parking.negotiation.ParkingManagerUtilityCalculator;
import com.parking.negotiation.QuickSortParking;
import com.parking.negotiation.UtilityCalculator;
import com.parking.persistence.mongo.documents.Parking;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Marco
 */
public class ParkingManagerAgent extends Agent {

    private PersistenceManager persistence;
    private UtilityCalculator utilityCalculator = new ParkingManagerUtilityCalculator();
    private String name;
    private Iterable<Parking> parkingsList;
    private HashMap<String, ArrayList<Parking>> proposes = new HashMap<String, ArrayList<Parking>>();
    private Gson gson = new Gson();
    private double[] weights = {0.5, 0.5}; //pesi per il calcolo dell'utilità. Posti liberi e zona

    //method to initialize agent
    protected void setup() {
        //initialize agent
        System.out.println("Hello! Im Parking Manager Agent. My id is: " + getAID().getName());
        persistence = PersistenceWrapper.get();
        name = getAID().getLocalName();
        parkingsList = persistence.getParkingByManager(name);
        System.out.println(gson.toJson(parkingsList));
        // Register the book-selling service in the yellow pages
        // crea un descrittore dell'agente
        DFAgentDescription dfd = new DFAgentDescription();
        // salva l'ID
        dfd.setName(getAID());
        // crea un descrittore del servizio
        ServiceDescription sd = new ServiceDescription();
        // definisci tipo e nome del servizio
        sd.setType("selling");
        sd.setName("JADE-book-trading");
        // aggiungi al decrittore del agente il descrittore del servizio
        dfd.addServices(sd);
        try {
            // registra il descrittore dell'agente
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    private ArrayList<Parking> caclulateProposes() {
        //calculate utilities
        ArrayList<Parking> results = new ArrayList<Parking>();
        for (Parking parking : parkingsList) {
            double[] params = {parking.getCapacity() - parking.getOccupied(), parking.getZone()};
            parking.setUtility(utilityCalculator.calculate(params, this.weights));
            if (parking.getUtility() > 0) {
                results.add(parking);
            }
        }
        QuickSortParking q = new QuickSortParking();
        return q.quicksort(results);
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;

        public void action() {
            ACLMessage msg = myAgent.receive();
            block();
            if (msg != null) {
                // oggetto che incapsula la risposta
                ACLMessage reply = msg.createReply();
                // richiesta di negoziazione da parte dell'utente
                if (msg.getPerformative() == ACLMessage.CFP) {
                    // CFP Message received. Process it
                    /*String jsonMsg = msg.getContent();
                    RequestCFP msgOBJ = gson.fromJson(jsonMsg, RequestCFP.class);
                    destination = msgOBJ.getDestination();
                    location = msgOBJ.getLocation();*/
                    //creare la lista delle preferenze per l'utente
                    System.out.println(msg.getSender().getName());
                    System.out.println(caclulateProposes());
                    
                    proposes.put(msg.getSender().getName(), caclulateProposes());
                    if (proposes.get(msg.getSender().getName()) != null && proposes.get(msg.getSender().getName()).size() > 0) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                        // prepare reply
                        reply.setContent(propose);
                        myAgent.send(reply);
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("not-available");
                        myAgent.send(reply);
                    }
                } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    // ACCEPT_PROPOSAL Message received. Process it
                    String acceptedPark = msg.getContent();
                    // get object
                    Parking parking = gson.fromJson(acceptedPark, Parking.class);
                    //risponde informando della prenotazione
                    //gestire prenotazione
                    reply.setPerformative(ACLMessage.INFORM);
                    myAgent.send(reply);
                    String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                    reply.setContent(propose);
                    myAgent.send(reply);
                    block();
                } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    if (proposes.get(msg.getSender().getName()).size() > 1) {
                        reply.setPerformative(ACLMessage.PROPOSE);
                        // Remove last parking offer by list
                        proposes.get(msg.getSender().getName()).remove(0);
                        // build a new offer for the buyer
                        // create json propose
                        String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                        // prepare reply
                        reply.setContent(propose);
                        myAgent.send(reply);
                        block();
                    } else {
                        // The requested book has been sold to another buyer in the meanwhile .
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("not-available");
                        myAgent.send(reply);
                        block();
                    }
                }
            }
        }
    }
}
