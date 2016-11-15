package com.parking.agents;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.parking.csv.CsvCreator;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
import java.awt.Point;
import com.parking.negotiation.UserAgentUtilityCalculator;
import com.parking.negotiation.UtilityCalculator;
import com.parking.persistence.mongo.documents.Parking;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marco
 */
public class UserAgent extends Agent {

    //raggio massimo in cui considerare i parcheggi dal punto di destinazione
    private static final double maxDistance = 0.02;
    //raggio massimo in cui considerare i parcheggi dal punto di partenza
    private static final double maxDistance2 = 0.1;
    //prezzo massimo per un parcheggio
    private static final double maxPrice = 20;
    private static final long serialVersionUID = 1L;
    private PersistenceManager persistence;
    private Gson gson = new Gson();
    private double location[];
    private double destination[];
    private double threshold;
    private double weights[];
    private UtilityCalculator utilityCalculator = new UserAgentUtilityCalculator();
    // The list of known seller agents
    private AID[] sellerAgents;
    private Map<String,Integer> sellerReplies;
    private Map<String, Parking> result;

    //method to initialize agent
    protected void setup() {
        PersistenceWrapper.numAgents--;
        System.out.println("=================================\n"
                + "Hello! Im User Agent. My id is: " + getAID().getName());
        persistence = PersistenceWrapper.get();
        //get alla input arguments
        Object[] args = getArguments();
        if (args != null) {
            location = (double[]) args[0];
            destination = (double[]) args[1];
            weights = (double[]) args[2];
            threshold = (Double) args[3];
            result = (Map<String, Parking>) args[4];
        }
        // Register the book-selling service in the yellow pages
        // crea un descrittore dell'agente
        DFAgentDescription dfd = new DFAgentDescription();
        // salva l'ID
        dfd.setName(getAID());
        // crea un descrittore del servizio
        ServiceDescription sd = new ServiceDescription();
        // definisci tipo e nome del servizio
        sd.setType("buyer");
        sd.setName("JADE-book-trading");
        // aggiungi al decrittore del agente il descrittore del servizio
        dfd.addServices(sd);
        try {
            // registra il descrittore dell'agente
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new SimpleBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                /*System.out.println("=================================\n"
                 + myAgent.getAID().getName() + ": Inizio Negoziazione");*/
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("selling");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    sellerAgents = new AID[result.length];
                    while (result.length <= 0) {
                        result = DFService.search(myAgent, template);
                        sellerAgents = new AID[result.length];
                    }
                    //sellerReplies = new HashMap<String, Integer>();
                    for (int i = 0; i < result.length; i++) {
                        sellerAgents[i] = result[i].getName();
                        //sellerReplies.put(sellerAgents[i].getName(), new Integer(0));
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < sellerAgents.length; i++) {
                    cfp.addReceiver(sellerAgents[i]);
                }
                cfp.setContent("{\"location\":["
                        + location[0] + "," + location[1]
                        + "],\"destination\":["
                        + destination[0] + "," + destination[1] + "]}");
                cfp.setConversationId("trade");
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                cfp.setSender(myAgent.getAID());
                myAgent.send(cfp);
            }

            @Override
            public boolean done() {
                boolean temp = true;
                addBehaviour(new RequestPerformer());
                System.out.println("Done");
                return temp;
            }

        });
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
        System.out.println("User Agent " + getAID().getName() + " terminating.");
    }

    /**
     * Inner class RequestPerformer. This is the behaviour used by Book-buyer
     * agents to request seller agents the target book.
     */
    private class RequestPerformer extends CyclicBehaviour {

        private static final long serialVersionUID = 1L;
        private AID bestSeller = null; // The agent who provides the best offer 
        private double bestUtility = 0; // The best utility obtained		
        private int repliesCnt = 0; // The counter of replies from seller agents
        private int repliesFailure = 0; //The counter of refused replies
        private int round = 0;
        private int totReplies = 0;
        private int totFailure = 0;
        private MessageTemplate mt; // The template to receive replies
        private Parking carPark = null;
        private String propose;

        public void action() {
            //attende messaggi dai parkings managers
            ACLMessage reply = myAgent.receive();
            if (reply != null) {
                // Reply received
                if (reply.getPerformative() == ACLMessage.PROPOSE || reply.getPerformative() == ACLMessage.FAILURE) {
                    //incremento il numero delle risposte
                    repliesCnt++;
                    if (reply.getPerformative() == ACLMessage.FAILURE) {
                        //System.out.println("getResponse: FAILURE " + reply.getSender().getLocalName());
                        repliesFailure++;
                    } else if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        //System.out.println("getResponse: PROPOSE " + reply.getSender().getLocalName());
                        totReplies++;
                        // This is an offer. Process it
                        String acceptedPark = reply.getContent();
                        Parking parking = gson.fromJson(acceptedPark, Parking.class);
                        double utility = calculateUtility(parking);
                        // Calculate Utility for UA
                        if (utility >= threshold && utility > bestUtility) {
                            carPark = parking;
                            bestSeller = reply.getSender();
                            bestUtility = utility;
                        }
                    }
                    //se ho ricevuto tutte le risposte nel round di negoziazione
                    if (repliesCnt >= sellerAgents.length) {
                        //se tutti gli agenti hanno terminato la lista
                        if (repliesFailure >= sellerAgents.length) {
                            System.out.println(myAgent.getAID() + ": Accordo non raggiunto!!");
                            System.out.println(totReplies);
                            myAgent.doDelete();
                        } else {
                            //riaggiorno i contatori
                            repliesCnt = 0;
                            repliesFailure = 0;
                            round++;
                            System.out.println(round);
                            //rispondo a tutti gli agenti venditori
                            //se nessun offerta soddisfa l'agente le rifiuta tutte
                            if (bestSeller == null) {
                                refuseAll(myAgent);
                                //System.out.println("\nRefuse all\n");
                            } else {
                                acceptProposal(myAgent, bestSeller, carPark);
                                //System.out.println("\nAccept\n");
                            }
                        }
                         System.out.println("\n======================================\n");
                    }
                } else if (reply.getPerformative() == ACLMessage.INFORM) {
                    // create json propose
                    //System.out.println("\nINFORM\n");
                    //System.out.println(totReplies);
                    gson = new Gson();
                    String acceptedPark = reply.getContent();
                    Parking parking = gson.fromJson(acceptedPark, Parking.class);
                    //propose = gson.toJson(acceptedPark);
                    /*System.out.println("=================================\n"
                     + myAgent.getAID().getName() + ": Conferma Prenotazione Ricevuta... ");*/
                    result.put(myAgent.getLocalName(), carPark);
                    System.out.println("\n=================================\n"
                            + myAgent.getAID().getName() + " Proposta prenotato - Parcheggio:\n"
                            + "Nome:" + parking.getName() + "\n");
                    CsvCreator csv = PersistenceWrapper.getCsvCreator();
                    csv.writeTest(myAgent.getAID().getName() + "#" + threshold + "#" + bestUtility + "#" + parking.getZone() + "#" + parking.getParkingManagerId() + "#" + parking.getUtility() + "#" + parking.getCapacity() + "#" + (parking.getOccupied()) + "#" + parking.getName() + "#" + round);
                    myAgent.doDelete();
                } else {
                    //se la prenotazione non va a buon fine
                    refuseAll(myAgent);
                }
            }
        }

        public void refuseAll(Agent myAgent) {

            /*System.out.println("=================================\n"
             + myAgent.getAID().getName() + ": Reject_proposal ");*/
            // Send the purchase order to the seller that provided the best offer
            ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
            reject.setSender(myAgent.getAID());
            for (int i = 0; i < sellerAgents.length; i++) {
                reject.addReceiver(sellerAgents[i]);
                //System.out.println("add refuse: "+ sellerAgents[i].getLocalName());
            }
            reject.setConversationId("trade");
            myAgent.send(reject);
        }

        public void acceptProposal(Agent myAgent, AID bestSeller, Parking carPark) {

            /*System.out.println("=================================\n"
             + myAgent.getAID().getName() + ": Accept_Proposal " + bestSeller.getName());*/
            ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            order.addReceiver(bestSeller);
            order.setSender(myAgent.getAID());
            order.setConversationId("book-trade");
            order.setReplyWith("order" + System.currentTimeMillis());
            myAgent.send(order);
        }

        /**
         * @return the propose
         */
        public String getPropose() {
            return propose;
        }

        /**
         * @param propose the propose to set
         */
        public void setPropose(String propose) {
            this.propose = propose;
        }

        public double calculateUtility(Parking parking) {

            Point.Double dest = new Point2D.Double(destination[0], destination[1]);
            Point.Double loc = new Point2D.Double(location[0], location[1]);
            double distance = dest.distance(parking.getLocation()[0], parking.getLocation()[1]);
            double distance2 = loc.distance(parking.getLocation()[0], parking.getLocation()[1]);

            if (distance > maxDistance || distance2 > maxDistance2 || parking.getPrice() > maxPrice) {
                return 0;
            }
            double[] params = {maxPrice - parking.getPrice(), maxDistance - distance, maxDistance2 - distance2};
            return utilityCalculator.calculate(params, weights, new double[]{maxPrice, maxDistance, maxDistance2});
        }
    }

}  // End of inner class RequestPerformer
