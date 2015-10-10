package com.parking.agents;

import com.google.gson.Gson;
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
import java.util.Map;

/**
 *
 * @author Marco
 */
public class UserAgent extends Agent {

    //raggio massimo in cui considerare i parcheggi dal punto di destinazione
    private static final double maxDistance = 0.01;
    //raggio massimo in cui considerare i parcheggi dal punto di partenza
    private static final double maxDistance2 = 1;
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
    private Map<String, Parking> result;

    //method to initialize agent
    protected void setup() {

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
        addBehaviour(new SimpleBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
                System.out.println("=================================\n"
                        + myAgent.getAID().getName() + ": Inizio Negoziazione");
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
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (int i = 0; i < sellerAgents.length; ++i) {
                    cfp.addReceiver(sellerAgents[i]);
                }
                cfp.setConversationId("trade");
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(cfp);
            }

            @Override
            public boolean done() {
                boolean temp = true;
                addBehaviour(new RequestPerformer());
                ACLMessage reply = myAgent.receive();
                if (reply != null && reply.getPerformative() == ACLMessage.PROPOSE) {
                    temp = true;
                    addBehaviour(new RequestPerformer());
                }
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
        private MessageTemplate mt; // The template to receive replies
        private Parking carPark = null;
        private String propose;

        public void action() {
            //attende messaggi dai parkings managers
            ACLMessage reply = myAgent.receive();
            if (reply != null) {
                // Reply received
                if (reply.getPerformative() == ACLMessage.PROPOSE) {

                    // This is an offer. Process it
                    String acceptedPark = reply.getContent();
                    Parking parking = gson.fromJson(acceptedPark, Parking.class);
                    double utility = calculateUtility(parking);
                    System.out.println("\n=================================\n"
                            + myAgent.getAID().getName() + " Proposta Ricevuta - Parcheggio:\n "
                            + "Nome:" + parking.getName() + "\n"
                            + "Soglia: " + threshold + "\n"
                            + "Utilità: " + utility + "\n"
                            + "Manager: " + parking.getParkingManagerId() + "\n"
                            + "Prezzo: " + parking.getPrice() + "\n"
                            + "Percentuale posti liberi: " + ((double)((parking.getCapacity() - parking.getOccupied())/ parking.getCapacity()))+ "\n"
                            + "Zona: " + parking.getZone() + "\n"
                            + "Utilità Manager: " + parking.getUtility() + "\n"
                            + "=================================\n");
                    // Calculate Utility for UA
                    if (utility >= threshold && utility > bestUtility) {
                        carPark = parking;
                        bestSeller = reply.getSender();
                        bestUtility = utility;
                    }
                    repliesCnt++;
                    if (repliesCnt >= sellerAgents.length) {
                        //ho ricevuto le proposte da tutti gli agenti
                        repliesCnt = 0;
                        //rispondo a tutti gli agenti venditori
                        //se nessun offerta soddisfa l'agente le rifiuta tutte
                        if (bestSeller == null) {
                            refuseAll(myAgent);
                        } else {
                            acceptProposal(myAgent, bestSeller, carPark);
                        }
                    }
                } else if (reply.getPerformative() == ACLMessage.INFORM) {
                    // create json propose
                    gson = new Gson();
                    propose = gson.toJson(carPark);
                    System.out.println("=================================\n"
                            + myAgent.getAID().getName() + ": Conferma Prenotazione Ricevuta... ");
                    result.put(myAgent.getLocalName(), carPark);
                    System.out.println("=================================\n"
                            + myAgent.getAID().getName() + ": Prenotazione avvenuta " + carPark.getName());
                } else {
                    //se la prenotazione non va a buon fine
                    refuseAll(myAgent);
                }
            }
        }

        public void refuseAll(Agent myAgent) {

            System.out.println("=================================\n"
                    + myAgent.getAID().getName() + ": Reject_proposal ");
            // Send the purchase order to the seller that provided the best offer
            ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
            reject.setSender(myAgent.getAID());
            for (int i = 0; i < sellerAgents.length; ++i) {
                reject.addReceiver(sellerAgents[i]);
            }
            myAgent.send(reject);
        }

        public void acceptProposal(Agent myAgent, AID bestSeller, Parking carPark) {

            System.out.println("=================================\n"
                    + myAgent.getAID().getName() + ": Accept_Proposal " + bestSeller.getName());
            gson = new Gson();
            String propose = gson.toJson(carPark);
            ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            order.addReceiver(bestSeller);
            order.setContent(propose);
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
