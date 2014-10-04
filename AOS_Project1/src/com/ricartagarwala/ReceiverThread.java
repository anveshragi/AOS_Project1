package com.ricartagarwala;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ReceiverThread extends Thread{
	
	private Socket clientSocket;
	private BufferedReader input;

	public ReceiverThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
		try {
			this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		
		} catch (IOException e) {
			 e.printStackTrace();
		}
	}

	public synchronized void run() {
		
//		System.out.println("ReceiverThread " + Thread.currentThread().getName());

		try {
			while(true) {
				try {
					String read = this.input.readLine();				
					System.out.println("\nMessage Received : " + read.toString() + "\n");
					
					String[] tokens = read.split(" ");
					
					Message message = new Message();
					
					message.setMsg_identifier(tokens[0]);
					message.setCs_num(Integer.parseInt(tokens[1]));
					message.setSeq_num(Integer.parseInt(tokens[2]));
					message.setSource_node(Integer.parseInt(tokens[3]));
					message.setDestination_node(Integer.parseInt(tokens[4]));
					message.setMsg(tokens[5]);
					
					receive_message(message);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
	}
	
	public synchronized void receive_message(Message message) {
		
		if(message.getMsg_identifier().equals("REQUEST")){
//			System.out.println("\nMessage Received : " + message.getMsg_identifier() + message.getCs_num() + message.getSeq_num() + message.getSource_node() + message.getDestination_node() + message.getMsg() + "\n");
			receive_request(message);
			Node.rec_num[message.getCs_num()]++;
		} 
		else if(message.getMsg_identifier().equals("REPLY")) {
//			System.out.println("\nMessage Received : " + message.getMsg_identifier() + message.getCs_num() + message.getSeq_num() + message.getSource_node() + message.getDestination_node() + message.getMsg() + "\n");
			receive_reply();
			Node.rec_num[message.getCs_num()]++;
		}
		else if(message.getMsg_identifier().equals("FINISH")) {
			Node.finish_count--;
		}
		
	}
	
	public synchronized void receive_request(Message message) {

		PrintWriter output;
		boolean defer_it = true;
		Node.highest_seq_num = Node.highest_seq_num > message.getSeq_num() ? Node.highest_seq_num : message.getSeq_num();

		try {
			Node.shared_var.acquire();
			defer_it = (Node.req_CS && ((message.getSeq_num() > Node.seq_num) || ((message.getSeq_num() == Node.seq_num) && (message.getSource_node() > Node.node_num))));
			Node.shared_var.release();
		} catch (InterruptedException e) {
			Node.logger.info(" Error while acquring the shared variable: " + e);
		}
		
		int reply_deferred_index = message.getSource_node();
		for(int i = 0; i<Node.config.nodeidentifiers.length;i++) {
			if(Node.config.hostnames[i].equals(this.clientSocket.getInetAddress().getHostName().toString())) {
				reply_deferred_index = i;
			}
		}
		
		if(defer_it){
			Node.reply_deferred[reply_deferred_index] = true;
		}

		else{
			Socket serverSocketForRespectiveClient = Node.serverSocketsArray.get(this.clientSocket.getInetAddress().getHostAddress().toString());
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("REPLY" + " ");
			buffer.append(message.getCs_num() + " ");
			buffer.append(message.getSeq_num() + " ");
			buffer.append(message.getSource_node() + " ");
			buffer.append(message.getDestination_node() + " ");
			buffer.append("// undeferred reply");
			
			try {
				output = new PrintWriter(serverSocketForRespectiveClient.getOutputStream(), true);
				output.println(buffer.toString());
				System.out.println("\nMessage Sent : " + buffer.toString() +"\n");
				Node.send_num[message.getCs_num()]++;
			} catch (IOException e) {
				Node.logger.info("REPLY to Node " + message.getDestination_node() + " failed ...");
			}
		}
	}		
	
	public synchronized void receive_reply() {

		Node.outstanding_reply_count = Node.outstanding_reply_count - 1;

	}
}
		






//				Node.logger.info(message.getSeq_num() + " is the sequence number being requested...");
		//				Node.logger.info(message.getSource_node() + " is the node number making the request...");

		//		System.out.println(message.getSeq_num() + " is the sequence number being requested...");
		//		System.out.println(message.getSource_node() + " is the node number making the request...");

		

		//				System.out.println("defer_it is TRUE when we have priority over Node-" + message.getSource_node() + "'s request...");
		//				Node.logger.info("defer_it is TRUE when we have priority over Node-" + message.getSource_node() + "'s request...");

		//				System.out.println("outstanding_reply_count " + Node.outstanding_reply_count);

		//					System.out.println("defer_it inside true loop\n");

		//					System.out.println("\nFollowing message sent from receiver...(defer_it inside false loop)");

	//				System.out.println("Redcucing outstanding_reply_count by 1..." + Node.outstanding_reply_count + "\n");

	

