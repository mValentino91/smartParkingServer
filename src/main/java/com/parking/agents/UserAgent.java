package com.parking.agents;

import com.google.gson.Gson;
import com.parking.dbManager.PersistenceManager;
import com.parking.dbManager.PersistenceWrapper;
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
import java.util.Map;

/**
 *
 * @author Marco
 */
public class UserAgent extends Agent {

    private static final long serialVersionUID = 1L;
    private PersistenceManager persistence;
    private double location[];
    private double destination[];
    private double threshold;
    private double weights[];
    // The list of known seller agents
    private AID[] sellerAgents;
    private Map<String, Parking> result;

    //method to initialize agent
    protected void setup() {

        System.out.println("Hello! Im User Agent. My id is: " + getAID().getName());
        persistence = PersistenceWrapper.get();
        //get alla input arguments
        Object[] args = getArguments();
        if (args != null) {
            location = (double[]) args[0];
            destination = (double[]) args[1];
            weights = (double[]) args[2];
            threshold = (Double) args[3];
            result = (Map<String,Parking>) args[4];
        }
        addBehaviour(new SimpleBehaviour() {
            private static final long serialVersionUID = 1L;

            public void action() {
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
//					ACLMessage reply = myAgent.receive();
//					if (reply != null && reply.getPerformative() == ACLMessage.PROPOSE) {
//						temp = true;
//						addBehaviour(new RequestPerformer());
//					}
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
        private AID bestSeller; // The agent who provides the best offer 
        private double bestUtility = -1.0; // The best utility obtained		
        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        private Parking carPark = null;
        private String propose;
        private Gson gson = new Gson();

        public void action() {

            switch (step) {
                case 0:
                    ACLMessage reply = myAgent.receive();
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer. Process it
                            String acceptedPark = reply.getContent();
                            Parking parking = gson.fromJson(acceptedPark, Parking.class);
                            // Calculate Utility for UA
                            // if (carPark.getProperties().getUtilityU() >= threshold) {
                            //  if (carPark.getProperties().getUtilityU() > bestUtility) {
                            carPark = parking;
                            bestSeller = reply.getSender();

                            //    }
                            // }
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            // Are received all replies						
                            if (bestSeller == null) {
                                if (reply.getPerformative() == ACLMessage.FAILURE) {
                                    step = 4;
                                } else {
                                    step = 2;
                                }
                            } else if (reply.getPerformative() == ACLMessage.INFORM) {
                                step = 4;
                            } else {
                                step = 3;
                            }
                        }
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    reject.setSender(myAgent.getAID());
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        reject.addReceiver(sellerAgents[i]);
                    }
                    // build a new offer for the buyer
                    // create json propose
                    Gson gson = new Gson();
                    propose = gson.toJson(carPark);
                    // prepare reply
                    // send reply
                    myAgent.send(reject);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(reject.getReplyWith()));
                    step = 0;
                    break;
                case 3:
                    gson = new Gson();
                    propose = gson.toJson(carPark);
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(propose);
                    order.setConversationId("book-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 0;
                    break;
                case 4:
                    // create json propose
                    gson = new Gson();
                    propose = gson.toJson(carPark);
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            result.put(myAgent.getLocalName(), carPark);
                            block();
                        }
                    }
                    break;
            }
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

    }  // End of inner class RequestPerformer
}
