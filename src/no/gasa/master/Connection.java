/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.gasa.master;

import java.awt.Point;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gard
 */
public class Connection implements Runnable {

    Server mHostServer;
    // This connection socket
    private Socket sock;
    // Connections to the server
    private ArrayList<Connection> connlist;

    // Input/Output buffered streams
    private BufferedInputStream input;
    private BufferedOutputStream output;

    // Receiving recorded data
    private ArrayList<Point> recordedData;
    private boolean isReceivingRecordedData = false;
    
    /** Constructor */
    public Connection(Server hostServer, Socket sock) {
	this.mHostServer = hostServer;
	this.sock = sock;
	System.out.println("Connection instantiated...");
    }

    /* Handy method to directly download text */
    public String convertStreamToString(InputStream is) {
	return new Scanner(is).useDelimiter("\\A").next();
    }

    /**
     * Public methods
     * */

    // Method to update connection list
    public void setConnlist(ArrayList<Connection> connlist) {
	this.connlist = connlist;
    }
    
    // Sends string as byte array
    public void sendString(String str) throws IOException {

//    	System.out.println(String.format("Sending string: %s", str));

	ByteArrayOutputStream byteout = new ByteArrayOutputStream(str.length() + 1);

	DataOutputStream out = new DataOutputStream(byteout);

	for (int i = 0; i < str.length(); i++)
            out.write((char) str.charAt(i));

        out.write((char) '\0');

        output.write(byteout.toByteArray(), 0, byteout.size());
        output.flush();
    } // end sendString
    
    /**
     * Private methods
     * */

    // Allocates the input/output streams
    private void getStreams() throws IOException {
	input = new BufferedInputStream(sock.getInputStream());
	output = new BufferedOutputStream(sock.getOutputStream());
	output.flush();
    }

    private void closeConnection() throws IOException {
	input.close();
	output.flush();
	output.close();
	connlist.remove(this);
	sock.close();
        mHostServer.setConnectedStatus(false);
	System.out.println("Connection terminated...");
    }

    // Receives byte array and returns the string
    private String recvString() throws IOException {

//	System.out.println("Receiving string");

        char c;
	StringBuilder sb = new StringBuilder();

	while (true) {
            c = (char) input.read();
            if (c == '\0')
                break;
            sb.append(c);
       	}

//	System.out.println(String.format("String received: %s", sb.toString()));

	return sb.toString();
    } // end recvString

    private void setTextToDisplay(String str) {
        mHostServer.setTextToDisplay(String.format("Client: %s", str));
    }
    
    private void setSensorValueToGUI(int sId, int sensorValue) {
        mHostServer.setSensorValueToGUI(sId, sensorValue);
    }
    
    private void setRecordedDataToGUI(ArrayList<Point> rd) {
        mHostServer.setRecordedDataToGUI(rd);
    }
    
    /** This threads run method */
    @Override
    public void run() {
        try {
            // get streams
            getStreams();
            
            // handle incoming/outgoing
            String helloStr = recvString();
            setTextToDisplay(helloStr);

            // setup regularly ping timer -> recv pong
            while (true) {
		// process requests
		String message = recvString();

                try {
                    int cmd = Integer.parseInt(message.substring(0, 3));
                    String[] arr = message.split(":");
                    switch (cmd) {
                        case 101:
                            int sID = Integer.valueOf(arr[1]);
                            int val = Integer.valueOf(arr[2]);
                            if (!isReceivingRecordedData) {
                                setSensorValueToGUI(sID, val);
                            } else {
                                recordedData.add(new Point(sID, val));
                            }
                            break;
                        case 104:
                            System.out.println("Start receiving recorded data...");
                            recordedData = new ArrayList<Point>();
                            isReceivingRecordedData = true;
                            break;
                        case 105:
                            System.out.println("Stop receiving recorded data...");
                            setRecordedDataToGUI(recordedData);
                            isReceivingRecordedData = false;
                    }
                    
                } catch (NumberFormatException e) {
                    setTextToDisplay(message);
                }

                if (message.equalsIgnoreCase("exit")) {
                    setTextToDisplay("Exiting...");
                    break;
		}

//		sendString(message);
            } // end while

            // remove this socket and close it
            closeConnection();
	} catch (IOException e) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, e);
            setTextToDisplay(e.getMessage());
	}

	System.out.println("Connection: Bye bye...");

	} // end run

	@Override
	public String toString() {
		return String.format("Client: %s", sock.getInetAddress().getHostAddress());
	}
} // end class
