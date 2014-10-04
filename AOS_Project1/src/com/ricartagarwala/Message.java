package com.ricartagarwala;

import java.io.Serializable;
//import java.net.Socket;


public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String msg_identifier;
	private String msg;
	private int cs_num;
	private int seq_num;
	private int source_node;
	private int destination_node;

	public Message(){
		super();
	}
			
	public Message(String msg_identifier, String msg, int cs_num, int seq_num, int source_node, int destination_node) {
		super();
		this.msg_identifier = msg_identifier;
		this.msg = msg;
		this.cs_num = cs_num;
		this.seq_num = seq_num;
		this.source_node = source_node;
		this.destination_node = destination_node;
	}
	
	public String getMsg_identifier() {
		return msg_identifier;
	}
	public void setMsg_identifier(String msg_identifier) {
		this.msg_identifier = msg_identifier;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public int getCs_num() {
		return cs_num;
	}

	public void setCs_num(int cs_num) {
		this.cs_num = cs_num;
	}

	public int getSeq_num() {
		return seq_num;
	}

	public void setSeq_num(int seq_num) {
		this.seq_num = seq_num;
	}

	
	public int getSource_node() {
		return source_node;
	}

	public void setSource_node(int source_node) {
		this.source_node = source_node;
	}

	public int getDestination_node() {
		return destination_node;
	}

	public void setDestination_node(int destination_node) {
		this.destination_node = destination_node;
	}
	
}
