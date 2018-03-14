package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Finger;
import edu.rit.CSCI652.ChordDHT.model.Message;
import edu.rit.CSCI652.ChordDHT.model.Node;

import java.io.IOException;
import java.util.*;

public class ChordNode {
    public static final String SERVER_IP = "172.17.0.2";
    //public static final String SERVER_IP = "localhost";
    public static final int SEND_PORT = 6789;
    public static final int RECEIVE_PORT = 6790;
    private static Finger[] fingerTable;
    private static int myNodeID = 0;
    private static int myPrevID = 0;
    private static Node myNode;
    private static Node prevNode;
    private static int nodesMax = 0;
    private static HashMap<Integer,Node> resultMap = new HashMap<Integer,Node>();
    private static List<String>  contentList = new ArrayList<String>();

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

                            myNode = recvdMessage.getNode();
                            prevNode = recvdMessage.getPrev();
                            myNodeID = myNode.getNodeID();
                            myPrevID = prevNode.getNodeID();
                            nodesMax = recvdMessage.getMaxNodes();

                            Logging.print("Server sent nodeID " + myNodeID);
                            Logging.print("Server sent predID " + myPrevID);
                            Logging.print("Max nodes " + nodesMax);
                            // create finger table here
                            buildFingerTables(myNode, prevNode, nodesMax, tcpSystem);
                            // show chord menu
                            chordMenu.showMenu();
                            break;

                        case Message.FIND_SUCCESSOR:
                            int id = recvdMessage.getNodeID();
                            Node fid = findSuccessor(id,tcpSystem);
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_FIND_SUCCESSOR);
                            sendMessage.setNode(fid);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_FIND_SUCCESSOR:
                            Node rid = recvdMessage.getNode();
                            System.out.println("Return find successor"+rid.getNodeID());
                            resultMap.put(Message.RETURN_FIND_SUCCESSOR, rid);
                            break;

                        case Message.GET_SUCCESSOR:
                            Node suc = getSuccessor();
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_SUCCESSOR);
                            sendMessage.setNodeID(suc.getNodeID());
                            sendMessage.setNode(suc);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_SUCCESSOR:
                            Node su = recvdMessage.getNode();
                            Logging.print(" Return successor "+su.getNodeID());
                            resultMap.put(Message.RETURN_SUCCESSOR, su);
                            break;

                        case Message.GET_PREDECESSOR:
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_GET_PREDECESSOR);
                            sendMessage.setNode(prevNode);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_GET_PREDECESSOR:
                            Node predd = recvdMessage.getNode();
                            Logging.print(" Return predecessor "+predd.getNodeID());
                            resultMap.put(Message.RETURN_GET_PREDECESSOR, predd);
                            break;

                        case Message.SET_PREDECESSOR:
                            Node pre = recvdMessage.getNode();
                            prevNode = pre;
                            myPrevID = pre.getNodeID();
                            break;

                        case Message.GET_CLOSESTFINGER:
                            int fingid = recvdMessage.getNodeID();
                            Node finger = closestPrecedingFinger(fingid);
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_CLOSESTFINGER);
                            sendMessage.setNode(finger);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                        case Message.RETURN_CLOSESTFINGER:
                            Node clo = recvdMessage.getNode();
                            Logging.print(" Return closest finger "+clo.getNodeID());
                            resultMap.put(Message.RETURN_CLOSESTFINGER, clo);
                            break;

                        case Message.UPDATE_FINGERS:
                            int updId = recvdMessage.getNodeID();
                            String ipaddr = recvdMessage.getIp();
                            int nodePort = recvdMessage.getPort();
                            int index = recvdMessage.getIndex();
                            Node n = new Node(updId,ipaddr,nodePort);
                            updateFingerTable(n,index,tcpSystem);
                            break;

                        case Message.UPDATE_FINGERS_REMOVE:
                            int upd = recvdMessage.getNodeID();
                            String ipadd = recvdMessage.getIp();
                            int nodeP = recvdMessage.getPort();
                            int ind = recvdMessage.getIndex();
                            Node r = new Node(upd,ipadd,nodeP);
                            updateFingerTableForRemove(r,ind,tcpSystem);
                            break;

                        case Message.INSERT_CONTENT:
                            String content = recvdMessage.getContent();
                            contentList.add(content);
                            System.out.println("Inserted content "+content+" at node "+myNodeID);
                            break;

                        case Message.GET_CONTENT:
                            String compareStr = recvdMessage.getContent();
                            String matchingStr = "No matching content";
                            for(String str:contentList){
                                if(str.equals(compareStr)){
                                    matchingStr = str;
                                }
                            }
                            sendMessage = new Message();
                            sendMessage.setType(Message.RETURN_GET_CONTENT);
                            sendMessage.setContent(matchingStr);
                            try {
                                tcpSystem.sendMessage(sendMessage, ip, port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Returning content >>> "+matchingStr+" from node "+myNodeID);
                            break;

                        case Message.RETURN_GET_CONTENT:
                            String contentStr = recvdMessage.getContent();
                            System.out.println("Content is >>> "+contentStr);
                            break;

                        case Message.SET_CONTENT:
                            String contents = recvdMessage.getContent();
                            contentList.addAll(Arrays.asList(contents.split(":")));
                            System.out.println("Added contents from node"+recvdMessage.getNodeID());
                            break;
                    }
                }
            });
            tcpSystem.startServer();

            chordMenu.setChordMenuInterface(new ChordMenu.ChordMenuInterface() {
                @Override
                public void invokePrintFingers(){
                    showFingerTable();
                    chordMenu.showMenu();
                }

                @Override
                public void invokeInsertKey(){
                    Scanner input = new Scanner(System.in);
                    System.out.print("Please enter key for the content: ");
                    int key = Integer.valueOf(input.nextLine());
                    System.out.print("Please enter the content: ");
                    String content = input.nextLine();
                    insertContent(key, content, tcpSystem);
                    chordMenu.showMenu();
                }

                @Override
                public void invokeLookup(){
                    Scanner inp = new Scanner(System.in);
                    System.out.print("Please enter key for lookup: ");
                    int key = Integer.valueOf(inp.nextLine());
                    System.out.print("Please enter the content: ");
                    String content = inp.nextLine();
                    lookupContent(key, content, tcpSystem);
                    chordMenu.showMenu();
                }

                @Override
                public void invokeLeave(){
                     //Transfer content to successor
                    transferContent(myNode,tcpSystem);
                    // Update finger table of others for removal
                    updateOthersForRemove(tcpSystem);
                }
            });

        Scanner in = new Scanner(System.in);
        System.out.print("Please enter your nodeID: ");
        int nodeID = Integer.valueOf(in.nextLine());
        authenticate(tcpSystem, nodeID);
    }

    private static void showFingerTable(){
        System.out.println("**** Printing finger table ****");
        int maxRowIndex = fingerTable.length -1;
        for (int i = 1; i <= maxRowIndex; i++) {
            System.out.println(fingerTable[i]);
        }
    }

    private static void buildFingerTables(Node node, Node pred, int maxNodes, TCPSystem tcpSystem){
        int rows = (int)Math.ceil(Math.log(maxNodes) / Math.log(2));
        fingerTable = new Finger[rows+1];

        // Set start and interval for finger table
        setFingerTableInterval(fingerTable, node, maxNodes);
        // Set successor in finger table
        setFingerTableSuccessor(fingerTable, node, pred, tcpSystem);
    }

    private static void setFingerTableInterval(Finger[] fingerTable, Node node, int maxNodes){
        int maxRowIndex = fingerTable.length -1;

        for (int i = 1; i <= maxRowIndex; i++) {
            fingerTable[i] = new Finger();
            fingerTable[i].setIndex(i-1);
            fingerTable[i].setStart((node.getNodeID() + (int)Math.pow(2,i-1)) % maxNodes);
        }
        for (int j = 1; j < maxRowIndex; j++) {
            fingerTable[j].setIntervalStart(fingerTable[j].getStart());
            fingerTable[j].setIntervalEnd(fingerTable[j+1].getStart());
        }
        fingerTable[maxRowIndex].setIntervalStart(fingerTable[maxRowIndex].getStart());
        fingerTable[maxRowIndex].setIntervalEnd(fingerTable[1].getStart()-1);
    }

    private static void setFingerTableSuccessor(Finger[] fingerTable, Node node, Node pred, TCPSystem tcpSystem){
        int maxRowIndex = fingerTable.length -1;

        for (int i = 1; i <= maxRowIndex; i++) {
            fingerTable[i].setSuccessor(node);
        }

        if (pred.getNodeID() != node.getNodeID()) {
            try {
                // Initiate finger table using predecessor
                initFingerTable(pred, tcpSystem);
                Logging.print("Finger table initialized");
                showFingerTable();
                // Update finger table of other nodes
                updateOthers(tcpSystem);
                Logging.print("Updated finger table of others");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void initFingerTable(Node prev, TCPSystem tcpSystem){
        // Find successor of predecessor and set as my successor
        Message message = new Message();
        message.setType(Message.FIND_SUCCESSOR);
        message.setNodeID(fingerTable[1].getStart());

        try {
            tcpSystem.sendMessage(message, prev.getNodeIP(), prev.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Node successor = resultMap.get(Message.RETURN_FIND_SUCCESSOR);
        int succ = successor.getNodeID();
        System.out.println("Successor is "+succ);
        fingerTable[1].setSuccessor(successor);

        // Ask for predecessor of my successor and set as my predecessor

        message = new Message();
        message.setType(Message.GET_PREDECESSOR);
        message.setNodeID(succ);
        try {
            tcpSystem.sendMessage(message, successor.getNodeIP(), successor.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        prevNode = resultMap.get(Message.RETURN_GET_PREDECESSOR);
        myPrevID = prevNode.getNodeID();

        // Set predecessor of my successor as me
        message = new Message();
        message.setType(Message.SET_PREDECESSOR);
        message.setNodeID(succ);
        message.setNode(myNode);
        try {
            tcpSystem.sendMessage(message, fingerTable[1].getSuccessor().getNodeIP(), fingerTable[1].getSuccessor().getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Update my remaining fingers
        int maxRowIndex = fingerTable.length -1;
        boolean type = true;
        int next = 0;
        for (int i = 1; i <= maxRowIndex-1; i++) {

            next = fingerTable[i].getSuccessor().getNodeID();

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
                message.setNodeID(nextFinger.getStart());

                try {
                    tcpSystem.sendMessage(message, prev.getNodeIP(), prev.getNodePort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int successorID = resultMap.get(Message.RETURN_FIND_SUCCESSOR).getNodeID();
                System.out.println("Successor is "+succ);


                int fingerStart = nextFinger.getStart();
                int fingerSuccessor = nextFinger.getSuccessor().getNodeID();
                if (fingerStart > successorID)
                    successorID = successorID + nodesMax;
                if (fingerStart > fingerSuccessor)
                    fingerSuccessor = fingerSuccessor + nodesMax;

                if ( fingerStart <= successorID && successorID <= fingerSuccessor ) {
                    nextFinger.setSuccessor(resultMap.get(Message.RETURN_FIND_SUCCESSOR));
                }
            }
        }
    }

    public static synchronized void updateOthers(TCPSystem tcpSystem){

        int maxRowIndex = fingerTable.length -1;
        for (int i = 1; i <= maxRowIndex; i++) {
            int id = myNodeID - (int)Math.pow(2,i-1) + 1;
            if (id < 0)
                id = id + nodesMax;

            Node p = findPredecessor(id, tcpSystem);
            Node n = myNode;
            Message message = new Message();
            message.setType(Message.UPDATE_FINGERS);
            message.setNodeID(myNodeID);
            message.setIp(n.getNodeIP());
            message.setPort(n.getNodePort());
            message.setIndex(i);
            try {
                tcpSystem.sendMessage(message, p.getNodeIP(), p.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static synchronized void updateOthersForRemove(TCPSystem tcpSystem){

        int maxRowIndex = fingerTable.length -1;
        for (int i = 1; i <= maxRowIndex; i++) {
            int id = myNodeID - (int)Math.pow(2,i-1) + 1;
            if (id < 0)
                id = id + nodesMax;
            System.out.println("Id is"+id);
            Node p = findPredecessor(id, tcpSystem);
            Node n = myNode;
            Message message = new Message();
            message.setType(Message.UPDATE_FINGERS_REMOVE);
            message.setNodeID(myNodeID);
            message.setIp(n.getNodeIP());
            message.setPort(n.getNodePort());
            message.setIndex(i);
            try {
                tcpSystem.sendMessage(message, p.getNodeIP(), p.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void updateFingerTable(Node s, int i, TCPSystem tcpSystem){

        boolean type = true;
        int currID = s.getNodeID();
        int next = fingerTable[i].getSuccessor().getNodeID();
        if (myNodeID >= next) {
            type = false;
        }else {
            type = true;
        }
        // Current node id is in between myNodeID and next
        if ( ((type==true && (currID >= myNodeID && currID < next)) ||
                (type==false && (currID >= myNodeID || currID < next)))
                && (myNodeID != currID)) {

            fingerTable[i].setSuccessor(s);
            Node n = prevNode;
            Message message = new Message();
            message.setType(Message.UPDATE_FINGERS);
            message.setNodeID(currID);
            message.setIndex(i);
            try {
                tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void updateFingerTableForRemove(Node r, int i, TCPSystem tcpSystem){
        System.out.println("Inside remove ## "+r.toString());
        boolean type = true;
        int currID = r.getNodeID();
        int next = fingerTable[i].getSuccessor().getNodeID();
        if (myNodeID >= next) {
            type = false;
        }else {
            type = true;
        }
        // Current node id is successor
        if (next == currID && currID!=myNodeID){
            Message message = new Message();
            message.setType(Message.GET_SUCCESSOR);

            try {
                tcpSystem.sendMessage(message, r.getNodeIP(), r.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Node successor = resultMap.get(Message.RETURN_SUCCESSOR);
            int succ = successor.getNodeID();
            System.out.println("Successor in remove is "+succ);
            fingerTable[i].setSuccessor(successor);

            Node n = prevNode;
            message = new Message();
            message.setType(Message.UPDATE_FINGERS_REMOVE);
            message.setNodeID(currID);
            message.setIp(r.getNodeIP());
            message.setPort(r.getNodePort());
            message.setIndex(i);
            try {
                tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void insertContent(int key, String content, TCPSystem tcpSystem){
        System.out.println("*** Insert key "+ key +" Content "+content+" clear***");
        Node n = findSuccessor(key, tcpSystem);

        Message message = new Message();
        message.setType(Message.INSERT_CONTENT);
        message.setNodeID(n.getNodeID());
        message.setContent(content);
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
        System.out.println(" *** Content inserted succesfully ***");
    }

    public static void lookupContent(int key, String content, TCPSystem tcpSystem){
        System.out.println("*** Lookup key "+ key +" Content "+content+" ***");
        Node n = findSuccessor(key, tcpSystem);

        Message message = new Message();
        message.setType(Message.GET_CONTENT);
        message.setNodeID(n.getNodeID());
        message.setContent(content);

        try {
            tcpSystem.sendMessage(message, n.getNodeIP(), n.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void transferContent(Node node, TCPSystem tcpSystem){
        System.out.println("*** Transferring content of node "+ node.getNodeID());
        //Node n = findSuccessor(node.getNodeID(), tcpSystem);
        String result = "";
        for(String str: contentList){
            result += str+":";
        }
        // Transfer content only if present
        if(result != ""){
            result = result.substring(0, result.length() - 1);

            Message message = new Message();
            message.setType(Message.SET_CONTENT);
            message.setNodeID(node.getNodeID());
            message.setContent(result);
            try {
                tcpSystem.sendMessage(message, fingerTable[1].getSuccessor().getNodeIP(), fingerTable[1].getSuccessor().getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        contentList = null;
    }

    public static Node findSuccessor(int id, TCPSystem tcpSystem){
        System.out.println("Find successor of key"+id);

        Node pred = findPredecessor(id, tcpSystem);
        System.out.println("Predecessor *** "+pred.getNodeID());
        Message message = new Message();
        message.setType(Message.GET_SUCCESSOR);

        try {
            tcpSystem.sendMessage(message, pred.getNodeIP(), pred.getNodePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int key:resultMap.keySet()){
            Logging.print("Key "+key+" Value "+resultMap.get(key).getNodeID());
        }
        return resultMap.get(Message.RETURN_SUCCESSOR);
    }

    public static Node findPredecessor(int id, TCPSystem tcpSystem){
        System.out.println("*** Find predecessor " + id);

        int successor = fingerTable[1].getSuccessor().getNodeID();
        Node nod = myNode;
        int currNodeID = nod.getNodeID();
        boolean type = true;

        if (currNodeID >= successor)
            type = false;

        Message message = null;


        while ((type==true && (id <= currNodeID || id > successor)) ||
                (type==false && (id <= currNodeID && id > successor))){

            message = new Message();
            message.setType(Message.GET_CLOSESTFINGER);
            message.setNodeID(id);
            try {
                tcpSystem.sendMessage(message, nod.getNodeIP(), nod.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nod = resultMap.get(Message.RETURN_CLOSESTFINGER);
            currNodeID = nod.getNodeID();

            message = new Message();
            message.setType(Message.GET_SUCCESSOR);

            try {
                tcpSystem.sendMessage(message, nod.getNodeIP(), nod.getNodePort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            successor = resultMap.get(Message.RETURN_SUCCESSOR).getNodeID();

            if (currNodeID >= successor){
                type = false;
            }else{
                type = true;
            }
        }

        System.out.println("*** Return predecessor " + nod.getNodeID());
        return nod;
    }


    public static Node closestPrecedingFinger(int id){
        // Returns the closest finger preceding id
        System.out.println("*** Get closest preceding finger" + id+"at node "+myNodeID);
        int maxRowIndex = fingerTable.length -1;
        boolean type = true;

        if (myNodeID >= id) {
            type = false;
        }

        for (int i = maxRowIndex; i >= 1; i--) {
            int nodeID = fingerTable[i].getSuccessor().getNodeID();
            System.out.println("Node ID " + nodeID);
            if(type){
                if (nodeID > myNodeID && nodeID < id) {
                    System.out.println("*** Return closest preceding finger #" + nodeID);
                    return fingerTable[i].getSuccessor();
                }
            }else{
                if (nodeID > myNodeID || nodeID < id) {
                    System.out.println("*** Return closest preceding finger ##" + nodeID);
                    return fingerTable[i].getSuccessor();
                }
            }
        }
        System.out.println("*** Return closest preceding finger ### " + myNodeID);
        return myNode;
    }

    public static Node getSuccessor(){
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
