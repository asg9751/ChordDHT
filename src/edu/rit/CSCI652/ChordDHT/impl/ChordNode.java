package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Finger;
import edu.rit.CSCI652.ChordDHT.model.Message;

import java.io.IOException;
import java.util.Scanner;

public class ChordNode {
    //public static final String SERVER_IP = "172.17.0.2";
    public static final String SERVER_IP = "localhost";
    public static final int SEND_PORT = 6789;
    public static final int RECEIVE_PORT = 6790;
    private static Finger[] fingerTable;

    public static void main(String[] args) {
        ChordMenu chordMenu = new ChordMenu();
        TCPSystem tcpSystem = new TCPSystem(SEND_PORT, RECEIVE_PORT);
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your nodeID: ");
        int nodeID = Integer.valueOf(in.nextLine());

        authenticate(tcpSystem, nodeID);

        tcpSystem.setTCPInterface(new ServerI() {
            @Override
            public void gotMessage(Message recvdMessage, String ip) {
                Logging.print("PORT:" + SEND_PORT + ", type:" + recvdMessage.getType());

                Message sendMessage = null;

                switch (recvdMessage.getType()) {

                    case Message.NODE_AUTHENTICATION:

                        int nodeID = recvdMessage.getNodeID();
                        int predID = recvdMessage.getPredID();
                        int maxNodes = recvdMessage.getMaxNodes();
                        Logging.print("Server sent nodeID "+nodeID);
                        Logging.print("Server sent predID "+predID);
                        Logging.print("Max nodes "+maxNodes);
                        // create finger table here
                        buildFingerTables(nodeID, predID, maxNodes);
                        // show chord menu
                        chordMenu.showMenu();
                        break;
                }
            }
        });
        tcpSystem.startRegServer();
    }

    private static void buildFingerTables(int nodeID, int predID, int maxNodes){
        int rows = (int)Math.ceil(Math.log(maxNodes) / Math.log(2));
        fingerTable = new Finger[rows+1];

        // Set start and interval for finger table
        setFingerTableInterval(fingerTable, nodeID, maxNodes);

    }

    private static void setFingerTableInterval(Finger[] fingerTable, int nodeID, int maxNodes){
        int maxRowIndex = fingerTable.length -1;

        for (int i = 1; i <= maxRowIndex; i++) {
            fingerTable[i] = new Finger();
            fingerTable[i].setStart((nodeID + (int)Math.pow(2,i-1)) % maxNodes);
        }
        for (int j = 1; j < maxRowIndex; j++) {
            fingerTable[j].setIntervalStart(fingerTable[j].getStart());
            fingerTable[j].setIntervalEnd(fingerTable[j+1].getStart());
        }
        fingerTable[maxRowIndex].setIntervalStart(fingerTable[maxRowIndex].getStart());
        fingerTable[maxRowIndex].setIntervalEnd(fingerTable[1].getStart()-1);
    }


    public static void authenticate(TCPSystem tcpSystem, int nodeID) {

        Message message = new Message();
        message.setType(Message.NODE_AUTHENTICATION);
        message.setNodeID(nodeID);
        try {
            tcpSystem.sendMessage(message, SERVER_IP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
