package com.tibco.tibjms.admin;

import java.util.Map;

public class TibjmsAdmin {

	public TibjmsAdmin(Object object, String username, String password) {
		// TODO Auto-generated constructor stub
	}

	public TibjmsAdmin(String serverURL, String username, String password, Map ssl) {
		// TODO Auto-generated constructor stub
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public DurableInfo getDurable(String clientID, String clientID2) {
		// TODO Auto-generated method stub
		return null;
	}

	public DestinationInfo getQueue(String realDestinationName) {
		// TODO Auto-generated method stub
		return null;
	}

	public DestinationInfo getTopic(String realDestinationName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void purgeQueue(String name) {
		// TODO Auto-generated method stub
		
	}

	public void purgeDurable(String clientID, String clientID2) {
		// TODO Auto-generated method stub
		
	}

	public void purgeTopic(String name) {
		// TODO Auto-generated method stub
		
	}

	public QueueInfo[] getQueues() {
		// TODO Auto-generated method stub
		return null;
	}

	public TopicInfo[] getTopics() {
		// TODO Auto-generated method stub
		return null;
	}

	public DurableInfo[] getDurables(String topicName) {
		// TODO Auto-generated method stub
		return null;
	}

}
