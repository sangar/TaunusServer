/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.gasa.master;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gard
 */
class Server implements Runnable {

    private TaunusServerUI tui;
    private ArrayList<Connection> connlist;
    private int mPort;
    private ServerSocket server;
    private static final int MAX_CONN = 10;
    
    public Server(TaunusServerUI tui) {
        this.tui = tui;
        
        System.out.println("Server instantiated");
    }
    
    /**
     * Public methods
     */
    
    public void runServer(int port) {
        this.mPort = port;
		
	connlist = new ArrayList<Connection>();
		
	Thread t = new Thread(null, this, "ServerThread");
	t.start();
    }
    
    public void setTextToDisplay(String str) {
        tui.onTextReceived(str);
    }
    
    public void setSensorValueToGUI(int sId, int sensorValue) {
        tui.onSensorValueReceive(sId, sensorValue);
    }
    
    public void setRecordedDataToGUI(ArrayList<Point> rd) {
        tui.onRecordedDataReceive(rd);
    }
    
    public void sendString(String str, Connection conn) {
        try {
            conn.sendString(str);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public ArrayList<Connection> getConnlist() {
        return connlist;
    }
    
    public void setConnectedStatus(boolean connected) {
        if (connected) {
            tui.onStatusChange(connected);
        } else {
            if (connlist.size() == 0) {
                tui.onStatusChange(connected);
            } else {
                tui.onClientDisconnect();
            }
        }
    }
    
    /**
     * Private methods
     * */
	
    private void registerWithServer() {
	InputStream in;
	try {
            System.out.println("Register with server...");
            in = new URL(String.format("http://folk.uio.no/gardbs/ipreq.php?mode=set&port=%d", mPort)).openStream();
            String serverStatus = new Scanner(in).useDelimiter("\\A").next();
            setTextToDisplay("Serverstatus: " + serverStatus);
            in.close();
	} catch (MalformedURLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            setTextToDisplay("Serverstatus: " + e.getMessage());
	} catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            setTextToDisplay("Serverstatus: " + e.getMessage());
	}
    }
    
    /** Initializes the server socket */
    private void initServerSock(int port) throws IOException {
	server = new ServerSocket(port);
	server.setReuseAddress(true);
    }
    
    /** Run by the server to accept new connections */
    private void listenForConnection() throws IOException {
	while(true) {
            System.out.println("Waiting for connection...");
            // Wait for client to connect
            Socket cli = server.accept();
            if(connlist.size() < MAX_CONN) {
		// if numCli is less then 100
                initConnection(cli);
            }
	}
    } // end listenForConnection
    
    /** Initializes a connection and adds it to connection list */
    private void initConnection(Socket cli) {	
	Connection conn = new Connection(this, cli);
        conn.setConnlist(connlist);
	Thread t = new Thread(conn);
	t.start();
	
	connlist.add(conn);
		
	setConnectedStatus(true);
    } // end initConnection()
    
    
    /**
     * Thread method
     */
    @Override
    public void run() {
        try {
            registerWithServer();
            initServerSock(mPort);
            listenForConnection();
	} catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            setTextToDisplay("run(): " + e.getMessage());
            setConnectedStatus(false);
	}
    }
}
