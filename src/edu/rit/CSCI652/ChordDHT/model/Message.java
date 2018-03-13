package edu.rit.CSCI652.ChordDHT.model;

public class Message {
    private int type;
    private int nodeID;
    private int predID;
    private int maxNodes;
    Node[] nodeList;

    public static final int NODE_AUTHENTICATION = 0;
    public static final int RETURN_NODE_AUTHENTICATION = 1;
    public static final int FIND_SUCCESSOR = 2;
    public static final int RETURN_FIND_SUCCESSOR = 3;
    public static final int GET_SUCCESSOR = 4;
    public static final int RETURN_SUCCESSOR = 5;
    public static final int GET_PREDECESSOR = 6;
    public static final int RETURN_GET_PREDECESSOR = 7;
    public static final int SET_PREDECESSOR = 8;
    public static final int GET_CLOSESTFINGER = 9;
    public static final int RETURN_CLOSESTFINGER = 10;

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }


    public int getPredID() {
        return predID;
    }

    public void setPredID(int predID) {
        this.predID = predID;
    }

    public Node[] getNodeList() {
        return nodeList;
    }

    public void setNodeList(Node[] nodeList) {
        this.nodeList = nodeList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }


}
