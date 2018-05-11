package peersim.core;

import java.util.HashMap;
import java.util.Map;

public class BLENode {
	
	private Node node;
	private int battery;
	private Map<String, String> messages;  // idmsg -> msg
	private Map<String, Node> msgRequest;  // idmsg -> dest
	
	
	
	public BLENode (Node node)
	{
		this.node = node;
		this.battery = 100;
		this.messages = new HashMap<String, String>();
		this.msgRequest = new HashMap<String, Node>();
		
	}

	
	public void decreasesBattery() {
		battery-- ;
	}


	/**
	 * @return the battery
	 */
	public int getBattery() {
		return battery;
	}


	/**
	 * @return the messages
	 */
	public Map<String, String> getMessages() {
		return messages;
	}


	/**
	 * @return this Node
	 */
	public Node getNode() {
		return node;
	}


	/**
	 * @return the msgRequests
	 */
	public Map<String, Node> getMsgRequest() {
		return msgRequest;
	}

}