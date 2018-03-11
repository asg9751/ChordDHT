package edu.rit.CSCI652.ChordDHT.impl;
import edu.rit.CSCI652.ChordDHT.model.Message;
import edu.rit.CSCI652.ChordDHT.model.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationServer{
    public static final int SEND_PORT = 6790;
    public static final int RECEIVE_PORT = 6789;
    static List<Node> nodeList;
    static int nodescount = 0;

    private void startService(){
        TCPSystem tcpSystem = new TCPSystem(SEND_PORT, RECEIVE_PORT);
        tcpSystem.setTCPInterface(new ServerI() {
            @Override
            public void gotMessage(Message recvdMessage, String ip) {
                Logging.print("PORT:" + SEND_PORT + ", type:" + recvdMessage.getType());

                Message sendMessage = null;

                switch (recvdMessage.getType()) {

                    case Message.NODE_AUTHENTICATION:

                        String nodeID = recvdMessage.getNodeID();
                        // hash nodeID here and return to client node
                        sendMessage = new Message();
                        sendMessage.setType(Message.NODE_AUTHENTICATION);
                        sendMessage.setNodeID(nodeID);
                        try {

                            tcpSystem.sendMessage(sendMessage, ip);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            }
        });


    }
    public static void main (String args[]) throws Exception{
        nodeList = new ArrayList<Node>();
        RegistrationServer server = new RegistrationServer();
        server.startService();
    }
}