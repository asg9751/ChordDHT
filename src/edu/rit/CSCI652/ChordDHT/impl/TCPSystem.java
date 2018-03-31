/**
 * @author Amol Gaikwad
 *         Utility class to handle TCP
 */

package edu.rit.CSCI652.ChordDHT.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;
import edu.rit.CSCI652.ChordDHT.model.Message;

public class TCPSystem {
    boolean close = false;
    public int sendPort;
    public int receivePort;
    ServerI serverI;


    public TCPSystem(int sendPort, int receivePort)
    {
        this.sendPort = sendPort;
        this.receivePort = receivePort;
    }


    public void sendMessage(Message message, String ipAddress, int port) throws IOException
    {

        Gson gson = new Gson();
        String messageStr = gson.toJson(message);
        sendToClient(messageStr, ipAddress, port);
    }

    public void setTCPInterface(ServerI serverI){
        this.serverI = serverI;
    }

    public void startServer()
    {
        new Thread()
        {
            @Override
            public void run()
            {

                try
                {
                    ServerSocket serverSocket = new ServerSocket(receivePort);
//                    System.out.println("Connected.");
                    while (!close)
                    {
                        Socket receiverSocket = serverSocket.accept();

                        if (receiverSocket.isConnected())
                        {

                            Thread thread = new Thread()
                            {
                                @Override
                                public void run() {
                                    handleMessage();
                                }
                                public synchronized void handleMessage() {
                                    String recieverIp = receiverSocket.getInetAddress().getHostAddress();
                                    int recieverPort = receiverSocket.getLocalPort();

                                    try
                                    {
                                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
                                        String messageStr = bufferedReader.readLine();
                                        receiverSocket.close(); //Close connection after reading

                                        if (messageStr != null)
                                        {
                                            Gson gson = new Gson();
                                            Message message = gson.fromJson(messageStr, Message.class);
                                            serverI.gotMessage(message, recieverIp, recieverPort);
                                        }

                                    } catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            };

                            thread.start();

                        }

                    }
                } catch (IOException e)
                {
                    System.out.println("I/O error: " + e);
                }
            }

        }.start();

    }


    public void close()
    {
        close = true;
    }

    public void sendToClient(String line, String ipAddress, int port) throws IOException
    {


        InetAddress inetAddress = Inet4Address.getByName(ipAddress);
        System.out.println("Sending to:" + ipAddress+" at port " +port);
        Socket receiverSocket = new Socket(inetAddress, port);
        DataOutputStream dataOutputStream = new DataOutputStream(receiverSocket.getOutputStream());
        dataOutputStream.writeBytes(line);
        receiverSocket.close();


    }

}
