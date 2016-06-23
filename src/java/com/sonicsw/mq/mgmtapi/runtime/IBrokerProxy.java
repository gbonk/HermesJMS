package com.sonicsw.mq.mgmtapi.runtime;

import java.util.ArrayList;

import com.sonicsw.mq.common.runtime.IDurableSubscriptionData;
import com.sonicsw.mq.common.runtime.IQueueData;

public class IBrokerProxy {

	public ArrayList<IQueueData> getQueues(String realDestinationName) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<IDurableSubscriptionData> getDurableSubscriptions(String realDestinationName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void deleteQueueMessages(ArrayList<String> queues) {
		// TODO Auto-generated method stub
		
	}

}
