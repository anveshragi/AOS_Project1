package com.ricartagarwala;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Server extends Thread {

	private static Logger logger = Logger.getLogger(Server.class.getName());
	private int port;
	private Socket clientSocket = null;
	private ServerSocket socket;

	public Server(int port) {
		super();
		this.port = port;
	}


	public synchronized void run() {

		try {
			socket = new ServerSocket(this.port);
			
//			System.out.println("Initializing server...\nServer on " + InetAddress.getLocalHost().getHostName() + " listening on port#" + this.port + "\n");
//			logger.info("Initializing server...\nServer on " + InetAddress.getLocalHost().getHostName() + " listening on port#" + this.port + "\n");

			while(true) {
//				System.out.println("Entering Server Thread while loop... and it's thread number is ..." + Thread.currentThread().getName());
				try {
					this.clientSocket = socket.accept();
//					logger.info("At Server...Connection with client " + clientsocket.getInetAddress().getHostName() + " established\n");

					Node.clientSocketsArray.put(this.clientSocket.getInetAddress().getHostAddress().toString(),this.clientSocket);
					
					ReceiverThread receiverThread = new ReceiverThread(this.clientSocket);
					receiverThread.start();
					
				} catch (IOException e) {
					logger.info("Error connecting with client " + this.clientSocket.getInetAddress().getHostName() + " : " + e);
				} 
			}
		} catch (IOException e) {
			try {
				logger.info("Error activating server " + InetAddress.getLocalHost().getHostName() + " with port# " + this.port + " : " + e);
			} catch (UnknownHostException e1) {
				logger.info(" Unknown hostname exception : " + e);
			}
		} 
	}

}