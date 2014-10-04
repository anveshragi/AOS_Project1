package com.ricartagarwala;

import java.net.UnknownHostException;
import java.io.*;
import java.util.logging.Logger;

public class Project {

	private static Logger logger = Logger.getLogger(Project.class.getName());

	public static void main(String[] args) throws UnknownHostException, IOException {

		int time_interval_for_CS = 0, i = 0, j = 0;
		
		Node node = new Node();		
		node.readConfigFile("config.txt");
		Node.setNum_of_cs(40);

		node.activateServer();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		node.activateClientConnections();	
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		time_interval_for_CS = node.randomNumber();
		System.out.println("Time interval for accessing critical section using random number : " + time_interval_for_CS + "\n");
		
		for(i=0;i<Node.getNum_of_cs()/2;i++){
			try {
				Thread.sleep(time_interval_for_CS);
			} catch (InterruptedException e) {
				logger.info(" Error Thread sleep : " + e);
			}
			node.request_CS(i);		
		}
		
		if(Node.node_num%2 != 0) {
			for(j=0;j<Node.getNum_of_cs()/2;j++) {
				try {
					Thread.sleep(time_interval_for_CS);
				} catch (InterruptedException e) {
					logger.info(" Error Thread sleep : " + e);
				}
				node.request_CS(i+j);
			}
		} else {
			Node.max_wait_period = 500;
			Node.min_wait_period  = 200;
			time_interval_for_CS = node.randomNumber();
			
			for(j=0;j<Node.getNum_of_cs()/2;j++) {
				try {
					Thread.sleep(time_interval_for_CS);
				} catch (InterruptedException e) {
					logger.info(" Error Thread sleep : " + e);
				}
				node.request_CS(i+j);
			}
		}		
		
		Node.exit_node();
	}
}
