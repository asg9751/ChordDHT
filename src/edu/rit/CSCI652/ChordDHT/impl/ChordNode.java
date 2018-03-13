package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Finger;
import edu.rit.CSCI652.ChordDHT.model.Message;
import edu.rit.CSCI652.ChordDHT.model.Node;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ChordNode {
    public static final String SERVER_IP = "172.17.0.2";
    //public static final String SERVER_IP = "localhost";
    public static final int SEND_PORT = 6789;
    public static final int RECEIVE_PORT = 6790;
    private static Finger[] fingerTable;
    //TCPSystem tcpSystem;
    static int myNodeID = 0;
    static int myPrevID = 0;
    static int nodesMax = 0;
    static Node[] nodeList;
    static HashMap<Integer,List<Integer>> resultMap = new HashMap<Integer,List<Integer>>();

    public static void main(String[] args) {
        ChordMenu chordMenu = new ChordMenu();
        TCPSystem tcpSystem = new TCPSystem(SEND_PORT, RECEIVE_PORT);

            tcpSystem.setTCPInterface(new ServerI() {
                @Override
                public void gotMessage(Message recvdMessage, String ip, int port) {
                    Logging.print("PORT:" + SEND_PORT + ", type:" + recvdMessage.getType());

                    Message sendMessage = null;

                    switch (recvdMessage.getType()) {

                        case Message.RETURN_NODE_AUTHENTICATION:

                            int nodeID = recvdMessage.getNodeID();
                            int predID = recvdMessage.getPredID();
                            int maxNodes = recvdMessage.getMaxNodes();
                            myNodeID = nodeID;
                            myPrevID = predID;
                            nodesMax = maxNodes;
                            nodeList = recvdMessage.getNodeList();
                            Logging.print("Server sent nodeID " + nodeID);
                            Logging.print("Server sent predID " + predID);
                            Logging.print("Max nodes " + maxNodes);
                            // create finger table here
                            buildFingerTables(nodeID, predID, maxNodes, nodeList, tcpSystem);
                            // show chord menu
                            chordMenu.showMenu();
                            break;

                        case Message.FIND_SUCCESSOR:
                            int id = recvdMessage.getNodeID();
                            int fid = findSuccessor(id,tcpSystem);
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_FIND_SUCCESSOR);
                            sendMessage.setNodeID(fid);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_FIND_SUCCESSOR:
                            int rid = recvdMessage.getNodeID();
                            resultMap.put(Message.RETURN_FIND_SUCCESSOR, Arrays.asList(rid));
                            break;

                        case Message.GET_SUCCESSOR:
                            int sucID = getSuccessor();
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_SUCCESSOR);
                            sendMessage.setNodeID(sucID);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_SUCCESSOR:
                            int suID = recvdMessage.getNodeID();
                            Logging.print(" Return successor "+suID);
                            resultMap.put(Message.RETURN_SUCCESSOR, Arrays.asList(suID));
                            break;

                        case Message.GET_PREDECESSOR:
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_GET_PREDECESSOR);
                            sendMessage.setNodeID(myPrevID);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_GET_PREDECESSOR:
                            int predd = recvdMessage.getNodeID();
                            Logging.print(" Return predecessor "+predd);
                            resultMap.put(Message.RETURN_GET_PREDECESSOR, Arrays.asList(predd));
                            break;

                        case Message.SET_PREDECESSOR:
                            int pre = recvdMessage.getNodeID();
                            myPrevID = pre;
                            break;

                        case Message.GET_CLOSESTFINGER:
                            int fingid = recvdMessage.getNodeID();
                            int finger = closestPrecedingFinger(fingid);
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_CLOSESTFINGER);
                            sendMessage.setNodeID(finger);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_CLOSESTFINGER:
                            int cloId = recvdMessage.getNodeID();
                            Logging.print(" Return closest finger "+cloId);
                            resultMap.put(Message.RETURN_CLOSESTFINGER, Arrays.asList(cloId));
                            break;
                    }
                }
            });
            tcpSystem.startServer();

            chordMenu.setChordMenuInterface(new ChordMenu.ChordMenuInterface() {
                @Override
                public void invokePrintFingers(){
                    System.out.println("**** Printing finger table ****");
                    int maxRowIndex = fingerTable.length -1;
                    for (int i = 1; i <= maxRowIndex; i++) {
                        System.out.println(fingerTable[i]);
                    }
                }

                @Override
                public void invokeInsertKey(){

                }

                @Override
                public void invokeLookup(){

                }

                @Override
                public void invokeRemove(){

                }
            });

        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your nodeID: ");
        int nodeID = Integer.valueOf(in.nextLine());
        authenticate(tcpSystem, nodeID);
    }

    private static void buildFingerTables(int nodeID, int predID, int maxNodes, Node[] nodeList, TCPSystem tcpSystem){
        int rows = (int)Math.ceil(Math.log(maxNodes) / Math.log(2));
        fingerTable = new Finger[rows+1];

        // Set start and interval for finger table
        setFingerTableInterval(fingerTable, nodeID, maxNodes);
        // Set successor in finger table
        setFingerTableSuccessor(fingerTable, nodeID, predID, nodeList, tcpSystem);
    }

    private static void setFingerTableInterval(Finger[] fingerTable, int nodeID, int maxNodes){
        int maxRowIndex = fingerTable.length -1;

        for (int i = 1; i <= maxRowIndex; i++) {
            fingerTable[i] = new Finger();
            fingerTable[i].setIndex(i-1);
            fingerTable[i].setStart((nodeID + (int)Math.pow(2,i-1)) % maxNodes);
        }
        for (int j = 1; j < maxRowIndex; j++) {
            fingerTable[j].setIntervalStart(fingerTable[j].getStart());
            fingerTable[j].setIntervalEnd(fingerTable[j+1].getStart());
        }
        fingerTable[maxRowIndex].setIntervalStart(fingerTable[maxRowIndex].getStart());
        fingerTable[maxRowIndex].setIntervalEnd(fingerTable[1].getStart()-1);
    }

    private static void setFingerTableSuccessor(Finger[] fingerTable, int nodeID, int predID, Node[] nodeList, TCPSystem tcpSystem){
        int maxRowIndex = fingerTable.length -1;

        for (int i = 1; i <= maxRowIndex; i++) {
            fingerTable[i].setSuccessor(nodeID);
        }

        if (predID != nodeID) {
            try {
                // Initiate finger table using predecessor
                initFingerTable(predID, nodeList, tcpSystem);
                Logging.print("Finger table initialized");
                // Update finger table of other nodes
                //update_others();
                Logging.print("Updated finger table of others");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void initFingerTable(int predID, Node[] nodeList, TCPSystem tcpSystem){
        // Find successor of predecessor
        Message message = new Message();
        message.setType(Message.FIND_SUCCESSOR);
        message.setNodeID(fingerTable[1].getStart());
        Node prev = nodeList[predID];
        try {
            tcpSystem.sendMessage(message, prev.getNodeIP(), prev.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int succ = resultMap.get(Message.RETURN_FIND_SUCCESSOR).get(0);
        System.out.println("Successor is "+succ);
        fingerTable[1].setSuccessor(succ);

        // Ask for its predecessor
        Node n = nodeList[succ];
        message = new Message();
        message.setType(Message.GET_PREDECESSOR);
        message.setNodeID(succ);
        try {
            tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int pre = resultMap.get(Message.RETURN_GET_PREDECESSOR).get(0);

        // Set predecessor
        message = new Message();
        message.setType(Message.SET_PREDECESSOR);
        message.setNodeID(succ);
        try {
            tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int maxRowIndex = fingerTable.length -1;
        boolean type = true;
        int next = 0;
        for (int i = 1; i <= maxRowIndex-1; i++) {

            next = fingerTable[i].getSuccessor();

            if (myNodeID >= next) {
                type = false;
            }else{
                type = true;
            }
            Finger nextFinger = fingerTable[i+1];
            if ((type==true && (nextFinger.getStart() >= myNodeID && nextFinger.getStart() <= next))
                    || (type==false && (nextFinger.getStart() >= myNodeID || nextFinger.getStart() <= next))) {

                nextFinger.setSuccessor(fingerTable[i].getSuccessor());
            } else {
                message = new Message();
                message.setType(Message.FIND_SUCCESSOR);
                message.setNodeID(fingerTable[1].getStart());
                prev = nodeList[predID];
                try {
                    tcpSystem.sendMessage(message, prev.getNodeIP(), prev.getNodePort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int successor = resultMap.get(Message.RETURN_FIND_SUCCESSOR).get(0);
                int successorTemp = successor;
                System.out.println("Successor is "+succ);


                int fingerStart = nextFinger.getStart();
                int fingerSuccessor = nextFinger.getSuccessor();
                if (fingerStart > successor)
                    successor = successor + nodesMax;
                if (fingerStart > fingerSuccessor)
                    fingerSuccessor = fingerSuccessor + nodesMax;

                if ( fingerStart <= successor && successor <= fingerSuccessor ) {
                    nextFinger.setSuccessor(successorTemp);
                }
            }
        }
    }

    public static int findSuccessor(int id, TCPSystem tcpSystem){
        System.out.println("Find successor of key"+id);

        int predID = findPredecessor(id, tcpSystem);
        System.out.println("Predecessor *** "+predID);
        Message message = new Message();
        message.setType(Message.GET_SUCCESSOR);
        Node n = nodeList[predID];
        try {
            tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int key:resultMap.keySet()){
            Logging.print("Key "+key+" Value "+resultMap.get(key).get(0));
        }
        return resultMap.get(Message.RETURN_SUCCESSOR).get(0);
    }

    public static int findPredecessor(int id, TCPSystem tcpSystem){
        System.out.println("*** Find predecessor " + id);

        int successor = fingerTable[1].getSuccessor();
        int currNodeID = myNodeID;
        boolean type = true;
        Node myNode = nodeList[myNodeID];

        if (currNodeID >= successor)
            type = false;

        Message message = null;

        while ((type==true && (id <= currNodeID || id > successor)) ||
                (type==false && (id <= currNodeID && id > successor))){

            message = new Message();
            message.setType(Message.GET_CLOSESTFINGER);
            message.setNodeID(id);
            try {
                tcpSystem.sendMessage(message, myNode.getNodeIP(), myNode.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            currNodeID = resultMap.get(Message.RETURN_CLOSESTFINGER).get(0);

            message = new Message();
            message.setType(Message.GET_SUCCESSOR);

            try {
                tcpSystem.sendMessage(message, myNode.getNodeIP(), myNode.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            successor = resultMap.get(Message.RETURN_SUCCESSOR).get(0);

            if (currNodeID >= successor){
                type = false;
            }else{
                type = true;
            }
        }

        System.out.println("*** Return predecessor " + currNodeID);
        return currNodeID;
    }


    public static int closestPrecedingFinger(int id){
        // Returns the closest finger preceding id
        System.out.println("*** Get closest preceding finger" + id);
        int maxRowIndex = fingerTable.length -1;
        boolean type = true;

        if (myNodeID >= id) {
            type = false;
        }

        for (int i = maxRowIndex; i >= 1; i--) {
            int nodeID = fingerTable[i].getSuccessor();
            if(type){
                if (nodeID > myNodeID && nodeID < id) {
                    System.out.println("*** Return closest preceding finger #" + nodeID);
                    return nodeID;
                }
            }else{
                if (nodeID > myNodeID || nodeID < id) {
                    System.out.println("*** Return closest preceding finger ##" + nodeID);
                    return nodeID;
                }
            }
        }
        System.out.println("*** Return closest preceding finger ### " + myNodeID);
        return myNodeID;
    }

    public static int getSuccessor(){
        return fingerTable[1].getSuccessor();
    }


    public static void authenticate(TCPSystem tcpSystem, int nodeID) {

        Message message = new Message();
        message.setType(Message.NODE_AUTHENTICATION);
        message.setNodeID(nodeID);
        try {
            tcpSystem.sendMessage(message, SERVER_IP, SEND_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
