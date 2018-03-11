package edu.rit.CSCI652.ChordDHT.model;

public class Node{

    private int nodeID;
    private String nodeIP;
    private String nodePort;

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public Node(int id, String ip, String port){
        this.nodeID = id;
        this.nodeIP = ip;
        this.nodePort = port;
    }
}