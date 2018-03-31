/**
 * @author Amol Gaikwad
 * Model for Chord Node
 */
package edu.rit.CSCI652.ChordDHT.model;

public class Node{

    private int nodeID;
    private String nodeIP;
    private int nodePort;

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

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public Node(int id, String ip, int port){
        this.nodeID = id;
        this.nodeIP = ip;
        this.nodePort = port;
    }

    @Override
    public String toString() {
        return "NodeID "+nodeID +"nodeIP "+nodeIP+"nodePort "+nodePort;
    }
}