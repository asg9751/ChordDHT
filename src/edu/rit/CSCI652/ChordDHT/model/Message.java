package edu.rit.CSCI652.ChordDHT.model;

public class Message {
    private int type;
    private String nodeID;

    public static final int NODE_AUTHENTICATION = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }


}
