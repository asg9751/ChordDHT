package edu.rit.CSCI652.ChordDHT.impl;
import edu.rit.CSCI652.ChordDHT.model.Message;
import edu.rit.CSCI652.ChordDHT.model.Node;

import java.io.IOException;
import java.util.*;

public class RegistrationServer{
    public static final int SEND_PORT = 6790;
    public static final int RECEIVE_PORT = 6789;
    public static final int MAX_NODES = 16;
    static Node[] nodeList;
    static List<Integer> nodeIds;
    static int nodescount = 0;

    private void startService(){
        TCPSystem tcpSystem = new TCPSystem(SEND_PORT, RECEIVE_PORT);
        tcpSystem.setTCPInterface(new ServerI() {
            @Override
            public void gotMessage(Message recvdMessage, String ip, int port) {
                Logging.print("PORT:" + SEND_PORT + ", type:" + recvdMessage.getType());

                Message sendMessage = null;

                switch (recvdMessage.getType()) {

                    case Message.NODE_AUTHENTICATION:

                        int nodeID = recvdMessage.getNodeID();
                        Logging.print("NodeID Server :"+ nodeID );
                        // hash nodeID here and return to client node. Also send predecessor id to client.
                        addNode(nodeID, ip, SEND_PORT);
                        int predID = getPredecessor(nodeID);
                        sendMessage = new Message();
                        sendMessage.setType(Message.RETURN_NODE_AUTHENTICATION);
                        sendMessage.setNodeID(nodeID);
                        sendMessage.setPredID(predID);
                        sendMessage.setMaxNodes(MAX_NODES);
                        sendMessage.setNodeList(nodeList);
                        try {

                            tcpSystem.sendMessage(sendMessage, ip, SEND_PORT);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            }
        });
        tcpSystem.startServer();

    }
    private int getPredecessor(int nodeId){
        Collections.sort(nodeIds,Collections.reverseOrder());
        int predID = nodeId;

        for (int id: nodeIds) {
            if (id < predID) {
                predID = id;
                break;
            }
        }
        if (predID == nodeId)
            predID = Collections.max(nodeIds);

        return predID;
    }

    private void addNode(int nodeId, String ip, int port){
        if (nodeList[nodeId] == null){
            nodeList[nodeId]= new Node(nodeId,ip,port);
            nodeIds.add(nodeId);
            Logging.print("New node added with id "+nodeId);
        }
    }

    public static void main (String args[]) throws Exception{
        nodeList = new Node[MAX_NODES];
        nodeIds = new ArrayList<Integer>();
        RegistrationServer server = new RegistrationServer();
        server.startService();
    }
}