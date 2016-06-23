package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.net.UnknownHostException;

import org.objectweb.joram.client.jms.Destination;


public class AdminModule {

	public static void connect(String hostname, int port, String username, String password, int cnxTimer) throws ConnectException, UnknownHostException{
		// TODO Auto-generated method stub
		
	}

	public static User[] getUsers() throws ConnectException {
		// TODO Auto-generated method stub
		return null;
	}

	public static Destination[] getDestinations() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void disconnect() {
		// TODO Auto-generated method stub
		
	}

}
