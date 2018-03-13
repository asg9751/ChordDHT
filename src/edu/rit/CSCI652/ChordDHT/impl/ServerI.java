package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Message;

public interface ServerI {
    void gotMessage(Message message, String ip, int port);
}
