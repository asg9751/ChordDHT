package edu.rit.CSCI652.ChordDHT.model;

public class Finger{

    private int index;
    private int start;
    private int intervalStart;
    private int intervalEnd;
    private int successor;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getIntervalStart() {
        return intervalStart;
    }

    public void setIntervalStart(int intervalStart) {
        this.intervalStart = intervalStart;
    }

    public int getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(int intervalEnd) {
        this.intervalEnd = intervalEnd;
    }

    public int getSuccessor() {
        return successor;
    }

    public void setSuccessor(int successor) {
        this.successor = successor;
    }

    public Finger(int start, int successor) {
        this.start = start;
        this.successor = successor;
    }

    public Finger() {

    }
}