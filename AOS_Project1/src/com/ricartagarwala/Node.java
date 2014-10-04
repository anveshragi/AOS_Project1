package com.ricartagarwala;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;


public class Node {

	public final static Logger logger = Logger.getLogger(Node.class.getName());
	public long elapsed_time = 0;
	public static int total_nodes = 0;
	public static int min_wait_period;
	public static int max_wait_period;
	public static volatile int node_num = 0;
	public static volatile int seq_num = 0;
	public static volatile int highest_seq_num = 0;
	public static volatile int outstanding_reply_count = 0;
	public static volatile int num_of_cs = 0;
	public static volatile int cs_num = 0;
	public static volatile int finish_count = 0;
	public static volatile int max_num_of_messages = 0;
	public static volatile int min_num_of_messages = 50000;
	public static volatile int total_num_of_messages = 0;
	public static volatile int send_num[];
	public static volatile int rec_num[];
	public static volatile int num_of_messages[];
	public static volatile boolean req_CS = false;
	public static volatile boolean[] reply_deferred; // Initially false	v
	
	
	public static volatile Semaphore shared_var = new Semaphore(1, true);
	public static ReadConfig config;
	public Server server;
	public Client client[];	
	public static Hashtable<String,Socket> serverSocketsArray = new Hashtable<String,Socket>();
	public static Hashtable<String,Socket> clientSocketsArray = new Hashtable<String,Socket>();

	public Node() {
		super();
		Node.min_wait_period = 10;
		Node.max_wait_period = 100;
	}

	public static int getNum_of_cs() {
		return num_of_cs;
	}

	public static void setNum_of_cs(int num_of_cs) {
		Node.num_of_cs = num_of_cs;
		send_num = new int[num_of_cs];
		rec_num = new int[num_of_cs];
		num_of_messages = new int [num_of_cs];
	}

	public int randomNumber() {
		Random rnd = new Random();		
		return (rnd.nextInt((max_wait_period-min_wait_period)+1)+min_wait_period);
	}

	public void readConfigFile(String filename) {

		File file = new File(filename);
		Node.config = new ReadConfig();

		try {
			config.read(file);
			Node.total_nodes = config.nodeidentifiers.length;
			Node.reply_deferred = new boolean[Node.total_nodes];
			this.client = new Client[Node.total_nodes];
			Node.finish_count = Node.total_nodes - 1;
		} catch (Exception e) {
			logger.info(" Error Reading Config file : " + e);
		}
	}

	public static synchronized void exit_node() {

		(new Thread(){

			public synchronized void run() {

				while(true){

					if(config.nodeidentifiers[0] == node_num) {

						if(finish_count == 0 && Node.cs_num == Node.num_of_cs) {

								System.out.println("Total number of messages exchanged at this node for executing " + num_of_cs + " critical sections is : " + total_num_of_messages + "\n");
								System.out.println("Maximum number of messages this node had to exchange to enter critical section is : " + max_num_of_messages + "\n");
								System.out.println("Minimum number of messages this node had to exchange to enter critical section is : " + min_num_of_messages + "\n");

							System.out.println("Compuitation Done!!!!!");

							break;
						}

					} else {

						if(Node.cs_num == Node.num_of_cs) {

							StringBuffer buffer = new StringBuffer();
							buffer.append("FINISH" + " ");
							buffer.append(Node.cs_num + " ");
							buffer.append(Node.seq_num + " ");
							buffer.append(Node.node_num + " ");
							buffer.append(config.nodeidentifiers[0] + " ");
							buffer.append("// execution at node " + Node.node_num + " finished");

							try {
								InetAddress hostname = InetAddress.getByName(config.hostnames[0]);
								PrintWriter output = new PrintWriter(Node.serverSocketsArray.get(hostname.getHostAddress().toString()).getOutputStream(), true);
								System.out.println("Message sent : " + buffer.toString());
								output.println(buffer.toString());

							} catch (IOException e) {
								Node.logger.info(" FINISH from " + Node.node_num + " to Node 0 failed ...");
							} 

							System.out.println("Total number of messages exchanged at this node for executing " + num_of_cs + " critical sections is : " + total_num_of_messages + "\n");
							System.out.println("Maximum number of messages this node had to exchange to enter critical section is : " + max_num_of_messages + "\n");
							System.out.println("Minimum number of messages this node had to exchange to enter critical section is : " + min_num_of_messages + "\n");

							break;
						}
					}
				}
			}
		}).start();
	}

	public void activateServer() {

		for(int i = 0; i < config.nodeidentifiers.length; i++){
			try {
				if(config.hostnames[i].equals(InetAddress.getLocalHost().getHostName().toString())) {

					Node.node_num = Integer.valueOf(InetAddress.getLocalHost().getHostName().toString().substring(3,5))-1;

					this.server = new Server(config.portnumbers[i]);
					this.server.start();
				}
			} catch (UnknownHostException e) {
				logger.info(" Unknown hostname exception : " + e);
			}
		}
	}

	public void activateClientConnections() {

		for(int j = 0; j < config.nodeidentifiers.length; j++){
			try {	
				if(!(config.hostnames[j].equals(InetAddress.getLocalHost().getHostName().toString()))) {

					this.client[j] = new Client(config.hostnames[j], config.portnumbers[j]);
					this.client[j].start();
				}
			} catch (UnknownHostException e) {
				logger.info(" Unknown hostname exception : " + e);
			}
		}	
	}

	public synchronized void request_CS(final int cs_num) {

		(new Thread() {
			@Override
			public synchronized void run() {

				//				System.out.println("request_CS " + Thread.currentThread().getName());

				//				logger.info("Request for entering critical section");
				//				System.out.println("Request for entering critical section\n");

				try {

					shared_var.acquire();
					req_CS = true;
					
					//					System.out.println("Choosing a sequence number...");
					//					logger.info("Choosing a sequence number...");	
					
					seq_num = highest_seq_num + 1;
					shared_var.release();

				} catch (InterruptedException e) {
					logger.info("Error while acquring the shared variable: " + e);
				}

				outstanding_reply_count = total_nodes - 1;

				//				System.out.println("outstanding reply count : " + outstanding_reply_count);
				//				System.out.println("node number : " + Node.node_num);

				PrintWriter output = null;
				Iterator<Entry<String, Socket>> iter = Node.clientSocketsArray.entrySet().iterator();
				Entry<String, Socket> clientSocket;
				Socket serverSocketForRespectiveClient = null;
				int index = 0;

				long start_time = System.currentTimeMillis();

				while(iter.hasNext()) {

					clientSocket = iter.next();

					index = Integer.valueOf(clientSocket.getValue().getInetAddress().getHostName().toString().substring(3,5))-1;

					StringBuffer buffer = new StringBuffer();
					buffer.append("REQUEST" + " ");
					buffer.append(cs_num + " ");
					buffer.append(seq_num + " ");
					buffer.append(node_num + " ");
					buffer.append(index + " ");	
					buffer.append("// request for critical section access");

					try {					
						serverSocketForRespectiveClient = Node.serverSocketsArray.get(clientSocket.getKey().toString());
						output = new PrintWriter(serverSocketForRespectiveClient.getOutputStream(), true);
						output.println(buffer.toString());
						System.out.println("\nMessage Sent : " + buffer.toString() +"\n");

						send_num[cs_num]++;
					} catch (IOException e) {
						Node.logger.info(" REQUEST to Node " + index + " failed ...");
					}
				}

				//				System.out.println("REQUEST message containing our sequence number and our node number sent to all other nodes...");
				//				System.out.println("Waiting for REPLY from each of the nodes...");

				//				logger.info("REQUEST message containing our sequence number and our node number sent to all other nodes...");
				//				logger.info("Waiting for REPLY from each of the nodes...");


				while(Node.outstanding_reply_count > 0){}

				//				logger.info("Critical section processing can be done now...");
				//				System.out.println("Critical section processing can be done now...");
				
				long end_time = System.currentTimeMillis();

				elapsed_time = end_time-start_time;

				try {
					FileWriter fos; 
					BufferedWriter bw; 
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					
					if(config.nodeidentifiers[0] == node_num && cs_num == 0) {	
						fos  = new FileWriter("logs.txt");
						bw = new BufferedWriter(fos);
						String str= "#NODE_ID" + "        " + "#CS_NUM" + "        " + "#Action" + "        " + "#Time of the action";
						bw.write(str + "\n");
						bw.close();
						fos.close();
					}
					
					fos = new FileWriter("logs.txt", true);
					bw = new BufferedWriter(fos);
					
					String str =  node_num + "                    " + cs_num + "        " + "Entering" + "        " + dateFormat.format(date);
					
					synchronized(this){
						bw.write(str + "\n");
					}		

					//				logger.info(node_num + "        " + "Entering");

					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						logger.info("Error while thread sleeping: " + e);
					}

					str = node_num + "                    " + cs_num + "        " + "Leaving" + "        " + dateFormat.format(date);
					synchronized(this){
						bw.write(str + "\n");
					}

					bw.close();
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}	

				//				logger.info(node_num + "        " + "Leaving");

				//				System.out.println("Critical section " + cs_num + " released...");
				
				logger.info("Critical section " + cs_num + " released... \n");

				System.out.println("Time elapsed between making a request and being able to enter the critical section is : " + elapsed_time + " ms \n");

				req_CS = false;

				iter = Node.clientSocketsArray.entrySet().iterator();

				while(iter.hasNext()) {
					clientSocket = iter.next();

					index = Integer.valueOf(clientSocket.getValue().getInetAddress().getHostName().toString().substring(3,5)) - 1;
					
					int reply_deferred_index = index;
					for(int i = 0; i<config.nodeidentifiers.length;i++) {
						if(config.hostnames[i].equals(clientSocket.getValue().getInetAddress().getHostName().toString())) {
							reply_deferred_index = i;
						}
					}
					
					if(reply_deferred[reply_deferred_index]) {

						reply_deferred[reply_deferred_index] = false;

						StringBuffer buffer = new StringBuffer();
						buffer.append("REPLY" + " ");
						buffer.append(cs_num + " ");
						buffer.append(seq_num + " ");
						buffer.append(node_num + " ");
						buffer.append(index + " ");
						buffer.append("// deferred reply");
						
						try {
							serverSocketForRespectiveClient = Node.serverSocketsArray.get(clientSocket.getKey().toString());
							output = new PrintWriter(serverSocketForRespectiveClient.getOutputStream(), true);
							output.println(buffer.toString());
							System.out.println("\nMessage Sent : " + buffer.toString() +"\n");
							send_num[cs_num]++;
						} catch (IOException e) {
							Node.logger.info("REPLY to Node " + index + " failed ...");
						}

						//						System.out.println("Sent a REPLY to node " + index + " ...");
						//						logger.info("Sent a REPLY to node " + i + " ...");
					}

				}


				num_of_messages[cs_num] = send_num[cs_num] + rec_num[cs_num];

				System.out.println("Number of messages exchanged to enter the critical section number " + cs_num + " is : " + num_of_messages[cs_num] + "\n");

				total_num_of_messages = total_num_of_messages + num_of_messages[cs_num];

				if(num_of_messages[cs_num] > max_num_of_messages)
					max_num_of_messages = num_of_messages[cs_num];
				if(num_of_messages[cs_num] < min_num_of_messages)
					min_num_of_messages = num_of_messages[cs_num];

				Node.cs_num++;
			}
		}).start();

	}

	public void displaySocketsArray() {
		Iterator<Entry<String, Socket>> iter = Node.serverSocketsArray.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Socket> socket = iter.next();
			System.out.println("Server sockets array : ");
			System.out.println("key - " + socket.getKey() + " value - " + socket.getValue());
		}
		iter = Node.clientSocketsArray.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Socket> socket = iter.next();
			System.out.println("Client sockets array : ");
			System.out.println("key - " + socket.getKey() + " value - " + socket.getValue());
		}		
	}

}
