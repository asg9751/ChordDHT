package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Finger;
import edu.rit.CSCI652.ChordDHT.model.Message;

import java.io.IOException;
import java.util.Scanner;

public class ChordNode {
    public static final String SERVER_IP = "172.17.0.2";
    //    public static final String SERVER_IP = "localhost";
    public static final int SEND_PORT = 6789;
    public static final int RECEIVE_PORT = 6790;
    private static Finger[] fingerTable;

    public static void main(String[] args) {
        TCPSystem tcpSystem = new TCPSystem(SEND_PORT, RECEIVE_PORT);
        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your nodeID: ");
        String nodeID = in.nextLine();

        authenticate(tcpSystem, nodeID);

        tcpSystem.setTCPInterface(new ServerI() {
            @Override
            public void gotMessage(Message recvdMessage, String ip) {
                Logging.print("PORT:" + SEND_PORT + ", type:" + recvdMessage.getType());

                Message sendMessage = null;

                switch (recvdMessage.getType()) {

                    case Message.NODE_AUTHENTICATION:

                        String nodeID = recvdMessage.getNodeID();
                        Logging.print("Server sent nodeID "+nodeID);
                        // create finger table here

                        break;
                }
            }
        });
    }
    public static void authenticate(TCPSystem tcpSystem, String nodeID) {

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
