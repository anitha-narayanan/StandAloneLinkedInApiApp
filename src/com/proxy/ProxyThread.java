package com.proxy;

import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyThread extends Thread {
	private Socket socket = null;
	private ProxyServer proxyServer = null; 
	public ProxyThread(ProxyServer ps, Socket socket) {
		super("ProxyThread");
		this.socket = socket;
		this.proxyServer = ps;
	}

	public void run() {
		//get input from user
		//set accessToken and state value
		//send response to user

		try {
			DataOutputStream out =
					new DataOutputStream(socket.getOutputStream());
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			String inputLine;
			int cnt = 0;
			String urlToCall = "";
			///////////////////////////////////
			//begin get request from client
			while ((inputLine = in.readLine()) != null) {
				try {
					StringTokenizer tok = new StringTokenizer(inputLine);
					tok.nextToken();
				} catch (Exception e) {
					break;
				}
				//parse the first line of the request to find the url
				if (cnt == 0) {
					String[] tokens = inputLine.split(" ");
					urlToCall = tokens[1];
					//can redirect this to output log
					System.out.println("Request for : " + urlToCall);
					if(!urlToCall.startsWith("/?code=")){
						continue;
					}
					String[] codeState = urlToCall.split("=");
					String  accessToken=codeState[1].substring(0, codeState[1].length()-6);
					System.out.println("accessToken: "+accessToken);
					String state=codeState[2];
					System.out.println("state: "+ state);
					proxyServer.accessToken = accessToken;
					proxyServer.state = state;
					proxyServer.listening = false;
					break;
				}

				cnt++;
			}
			//end get request from client
			try{
				out.writeBytes("Thank you for authorizing");
				out.flush();

				//end send response to client
				///////////////////////////////////
			} catch (Exception e) {
				//can redirect this to error log
				System.err.println("Encountered exception: " + e);
				//encountered error - just send nothing back, so
				//processing can continue
				out.writeBytes("");
			}

			//close out all resources
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if (socket != null) {
				socket.close();
			}

		} catch (IOException e) {

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}
}
