/**
 * @author Amol Gaikwad
 * Interface for receiving message
 */
package edu.rit.CSCI652.ChordDHT.impl;

import edu.rit.CSCI652.ChordDHT.model.Message;

public interface ServerI {
    void gotMessage(Message message, String ip, int port);
}
