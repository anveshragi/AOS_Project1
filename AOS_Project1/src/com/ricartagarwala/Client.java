package com.ricartagarwala;

import java.util.logging.Logger;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client extends Thread {

	private static Logger logger = Logger.getLogger(Client.class.getName());
	private String serverAddress;
	private int port;
	private Socket socket = null;

	public Client(Socket skt) {
		super();
		this.socket = skt;
	}

	public Client(String serverAddress, int port) {
		super();
		this.serverAddress = serverAddress;
		this.port = port;
	}

	public synchronized void run() {
		
//		System.out.println("Client Thread " + Thread.currentThread().getName());

		try {
			socket = new Socket(this.serverAddress, this.port);
//			logger.info("Connection with server " + this.serverAddress + " at port#" + this.port);		
			
			Node.serverSocketsArray.put(this.socket.getInetAddress().getHostAddress().toString(), this.socket);
			
		} catch (UnknownHostException e) {
			logger.info(" Unknown hostname exception : " + e);
		} catch (IOException e) {
			logger.info("Error connecting to server " + this.serverAddress + " : " + e);
		}
	}

	public void closeConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.info("Error closing the connection with server " + this.serverAddress + " : " + e);
		}
	}
} 


