package com.parking.agents;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import com.parking.negotiation.ConcreteInputCalculator;
import com.parking.negotiation.InputCalculator;
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
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Marco
 */
public class ParkingManagerAgent extends Agent {

    //raggio massimo in cui considerare i parcheggi dal punto di destinazione
    private static final double maxDistance = 0.02;
    //raggio massimo in cui considerare i parcheggi dal punto di partenza
    private static final double maxDistance2 = 1;
    private PersistenceManager persistence;
    private InputCalculator inputCalc = new ConcreteInputCalculator();
    private UtilityCalculator utilityCalculator = new ParkingManagerUtilityCalculator();
    private String name;
    private Iterable<Parking> parkingsList;
    private HashMap<String, ArrayList<Parking>> proposes = new HashMap<String, ArrayList<Parking>>();
    private Gson gson = new Gson();
    private double[] weights = {0.5, 0.5}; //pesi per il calcolo dell'utilità. Posti liberi e zona
    //method to initialize agent

    protected void setup() {
        //initialize agent
        System.out.println("=================================\n"
                + "Hello! Im Parking Manager Agent. My id is: " + getAID().getName());
        persistence = PersistenceWrapper.get();
        name = getAID().getLocalName();
        parkingsList = persistence.getParkingByManager(name);
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

    private ArrayList<Parking> caclulateProposes(JsonElement json) {
        //calculate utilities
        ArrayList<Parking> results = new ArrayList<Parking>();
        for (Parking parking : parkingsList) {
            JsonArray loc = json.getAsJsonObject().get("location").getAsJsonArray();
            JsonArray dest = json.getAsJsonObject().get("destination").getAsJsonArray();
            Point.Double dest1 = new Point2D.Double(dest.get(0).getAsDouble(), dest.get(1).getAsDouble());
            Point.Double loc1 = new Point2D.Double(loc.get(0).getAsDouble(), loc.get(1).getAsDouble());
            double distance = dest1.distance(parking.getLocation()[0], parking.getLocation()[1]);
            double distance1 = loc1.distance(parking.getLocation()[0], parking.getLocation()[1]);
            if (distance <= maxDistance && distance1 <= maxDistance2) {
                double[] params = {parking.getCapacity() - parking.getOccupied(), parking.getZone()};
                //il parametro dell'evento è per il momento impostato a false, da pensare
                //TODO
                //utilità su zona e percentuale di occupazione del parcheggio
                parking.setPrice(inputCalc.getDynamicPrice(parking.getZone(), parking.getOccupied(), parking.getCapacity(), false));
                parking.setUtility(utilityCalculator.calculate(params, this.weights, new double[]{parking.getCapacity(), 4}));
                if (parking.getUtility() > 0) {
                    results.add(parking);
                }
            }
        }
        QuickSortParking q = new QuickSortParking();
        return q.quicksort(results);
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;
        private int cont = 0;

        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                cont++;
                // oggetto che incapsula la risposta
                // richiesta di negoziazione da parte dell'utente
                if (msg.getPerformative() == ACLMessage.CFP) {
                    /*System.out.println("=================================\n"
                     + myAgent.getAID().getName() + ": Nuova Richiesta Ricevuta... ");*/
                    JsonParser parser = new JsonParser();
                    JsonElement json = parser.parse(msg.getContent());
                    proposes.put(msg.getSender().getName(), caclulateProposes(json));
                    //controlla se ci sono ancora proposte disponibili
                    if (proposes.get(msg.getSender().getName()) != null && proposes.get(msg.getSender().getName()).size() > 0) {
                        ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
                        //prende il primo parcheggio disponibile
                        String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                        System.out.println(myAgent.getAID().getLocalName() + ": " + proposes.get(msg.getSender().getName()).size());
                        // prepare reply
                        reply.setContent(propose);
                        reply.addReceiver(msg.getSender());
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND CFP\n");
                        myAgent.send(reply);
                    } else {
                        ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
                        reply.addReceiver(msg.getSender());
                        reply.setContent("not-available");
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND FAILURE\n");
                        myAgent.send(reply);
                    }
                } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    //System.out.println("\n"+ myAgent.getAID().getLocalName()+ "ACCEPTPROPOSAL\n");
                    /*System.out.println("=================================\n"
                     + myAgent.getAID().getName() + ": Proposta accettata dall'utente.. ");*/
                    // get object
                    Parking parking = proposes.get(msg.getSender().getName()).get(0);
                    //verifico che l'utilità corrente non sia dimezzata rispetto a quella calcolata a inizio negoziazione
                    double[] params = {parking.getCapacity() - parking.getOccupied(), parking.getZone()};
                    if (parking.getCapacity() - parking.getOccupied() <= 0 || parking.getUtility() / 2 >= utilityCalculator.calculate(params, weights, new double[]{parking.getCapacity(), 4})) {
                        //rispondere con messaggio di fallimento e riprendere la negoziazione dal parcheggio successivo
                        ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
                        String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                        reply.setContent(propose);
                        reply.addReceiver(msg.getSender());
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND FAILURE\n");
                        reply.setSender(myAgent.getAID());
                        myAgent.send(reply);
                        System.out.println("=================================\n"
                                + myAgent.getAID().getName() + ": Impossibile effettuare la prenotazione... " + parking.getName());
                    } else {
                        //è possibile prenotare il parcheggio
                        parking.setOccupied(parking.getOccupied() + 1);
                        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                        String propose = gson.toJson(parking);
                        reply.setContent(propose);
                        reply.addReceiver(msg.getSender());
                        reply.setSender(myAgent.getAID());
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND INFORM\n");
                        myAgent.send(reply);
                        /*System.out.println("=================================\n"
                         + myAgent.getAID().getName() + ": Prenotazione effettuata... " + parking.getName());*/
                    }
                } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    if (proposes.get(msg.getSender().getName()).size() > 1) {
                        ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
                        // Remove last parking offer by list
                        proposes.get(msg.getSender().getName()).remove(0);
                        // build a new offer for the buyer
                        // create json propose
                        String propose = gson.toJson(proposes.get(msg.getSender().getName()).get(0));
                        // prepare reply
                        reply.addReceiver(msg.getSender());
                        reply.setContent(propose);
                        reply.setSender(myAgent.getAID());
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND PROPOSE\n");
                        myAgent.send(reply);
                    } else {
                        // The requested book has been sold to another buyer in the meanwhile .
                        //System.out.println("=================================\n"
                        //        + "Prenotazione gia' effettuata");
                        //System.out.println("\n" + myAgent.getAID().getLocalName() + cont + "SEND FAILURE\n");
                        ACLMessage reply = new ACLMessage(ACLMessage.FAILURE);
                        reply.setContent("not-available");
                        reply.addReceiver(msg.getSender());
                        reply.setSender(myAgent.getAID());
                        myAgent.send(reply);
                    }
                }
            }
        }
    }
}
