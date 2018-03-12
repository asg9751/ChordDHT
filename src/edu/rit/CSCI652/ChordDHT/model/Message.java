package edu.rit.CSCI652.ChordDHT.model;

public class Message {
    private int type;
    private int nodeID;
    private int predID;
    private int maxNodes;

    public static final int NODE_AUTHENTICATION = 0;

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
