package com.ricartagarwala;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;


public class ReadConfigUsingBufferReader {
	int[] nodeidentifiers;
	String[] hostnames;
	int[] portnumbers;

	public void read() throws Exception
	{
				String dir = System.getProperty("user.dir");
				System.out.println("user.dir = " + dir);
		//		BufferedReader reader = new BufferedReader(new FileReader(dir + "//config1.txt"));
		BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
		String line = null;
		line = reader.readLine();
		System.out.println("skipped line : " + line.toString());
		line = reader.readLine();
		System.out.println("first line : " + line.toString());
		StringTokenizer firstLine = new StringTokenizer(line);
		String noOfNodesString = firstLine.nextToken();
		int noOfNodes = Integer.parseInt(noOfNodesString);
		nodeidentifiers = new int[noOfNodes];
		hostnames = new String[noOfNodes];
		portnumbers = new int[noOfNodes];
		line = reader.readLine();
		System.out.println("second line : " + line.toString());
		while ((line = reader.readLine()) != null) {
			System.out.println("line : " + line.toString());
			StringTokenizer st = new StringTokenizer(line);
			String element = st.nextToken();
			int node = Integer.parseInt(element);
			nodeidentifiers[node] = node;
			String hostname = st.nextToken();
			hostnames[node] = hostname;
			String portnumberString = st.nextToken();
			int portnumber = Integer.parseInt(portnumberString);
			portnumbers[node] = portnumber;
		}
		reader.close();
	}
}
