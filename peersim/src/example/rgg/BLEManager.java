package example.rgg;

import peersim.config.*;
import peersim.core.*;

import java.util.HashMap;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;


public class BLEManager implements CDProtocol, EDProtocol, Protocol {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	/* 0 Standby
	 * 1 Scanning
	 * 2 Initiating
	 * 3 Advertising
	 * 4 Connection
	 */
	public int bleState = 0;
	//public int battery;
	public boolean busy = false;
	private int mss = 0;
	
	private Map<String, String> messages;  // idmsg -> msg
	
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public BLEManager(String prefix) {
		
	}
	
	
	/**
	 *	Clone method of the class. Returns a deep copy of the BLEManager class. Used
	 *	by the simulation to initialize the {@link peersim.core.Network}
	 *	@return the deep copy of the BLEManager class.
	 */
	public Object clone(){
		Object blem = null;
		try{
			blem = (BLEManager)super.clone();
		}
		catch(CloneNotSupportedException e){};
		
		((BLEManager)blem).messages = new HashMap<String, String>();
		
		return blem;
	}
	
	
	//--------------------------------------------------------------------------
	// methods
	//--------------------------------------------------------------------------
	
	
	/**
	 * This is the standard method the define periodic activity.
	 * The frequency of execution of this method is defined by a
	 * {@link peersim.edsim.CDScheduler} component in the configuration.
	 */
	public void nextCycle( Node node, int pid ) {
		
		if (bleState != 4){
			bleState = 1;
		}
		
		
		if (mss == 0) {
		
		/* non va generato qua */ String newid = NewMSG.newIdmsg(node);
		messages.put( newid, NewMSG.newMsg(node) );
		
		//m.msg.substring(0, 1);
		
		
		//System.out.println( node.getID() + " new " + messages.keySet() );
		mss++;
		
		
		bleState = 3;
		this.doAdvertising(newid, node, pid);
		}
		
		
	}
	
	
	/**
	 * This is the standard method to define to process incoming messages.
	 */
	public void processEvent( Node node, int pid, Object event ) {
		
		
		if (event.getClass() == AdvertisingMessage.class) {
			AdvertisingMessage adv = (AdvertisingMessage)event;
			
			if ( !messages.containsKey( adv.idmsg )  &&  !busy ){
				
				
				//System.out.println( node.getID() + "  y  " + adv.idmsg );
				
				
				bleState = 2;
				this.respAdvertising( 
						node, 
						adv.sender, 
						adv.idmsg, 
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest req = (ConnectionRequest)event;
			
			if ( !busy ){
				
				
				
				//long delay = ( (sizemsg * 8) / bspeed ) * 1000;
				
				
				
				this.busy = true;
				((BLEManager)req.sender.getProtocol(pid)).busy = true;
				bleState = 4;
				((BLEManager)req.sender.getProtocol(pid)).bleState = 4;
				
				
				EDSimulator.add(
						500,
						new ConnectionMessage( req.idmsg, node, messages.get( req.idmsg ) ),
						req.sender,
						pid);
				
				
				EDSimulator.add(
						500,
						new Unlock(),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			messages.put( con.idmsg, con.msg );
			
			
			
			System.out.println( node.getID() + " ho ricevuto messaggio " + con.idmsg );
			
			
			this.busy = false;
			bleState = 0;
			
			
			bleState = 3;
			this.doAdvertising(con.idmsg, node, pid);
			
		} else if (event.getClass() == Unlock.class) {
			this.busy = false;
			bleState = 0;
		}
	
	}
	
	
	private void doAdvertising(String idmsg, Node node, int pid) {
		
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		// per ogni Neighbor con stato Scanning o Initiating
		for (int i = 0; i < linkable.degree(); i++) {
			
			
			Node peern = linkable.getNeighbor(i);
			
			EDSimulator.add(
					0,
					new AdvertisingMessage( idmsg, node ),
					peern,
					pid);
			
			
		}
	
	}
	
	
	private void respAdvertising(Node src, Node dest, String idmsg, int pid) {
		
		EDSimulator.add(
				0,
				new ConnectionRequest( idmsg, src ),
				dest,
				pid);
		
		
	}

}


/**
* The type of an advertisingmessage. It contains an idmsg 
* of type String and the sender node of type {@link peersim.core.Node}.
*/
class AdvertisingMessage {
	
	final String idmsg;
	final Node sender;
	
	public AdvertisingMessage( String idmsg, Node sender ) {
		this.idmsg = idmsg;
		this.sender = sender;
	}
}


/**
* The type of a connectionrequest. It contains an idmsg 
* of type String and the sender node of type {@link peersim.core.Node}.
*/
class ConnectionRequest {
	
	final String idmsg;
	final Node sender;
	
	public ConnectionRequest( String idmsg, Node sender ) {
		this.idmsg = idmsg;
		this.sender = sender;
	}
}


/**
* The type of a message. It contains an idmsg of type String, a msg
* of type String and the sender node of type {@link peersim.core.Node}.
*/
class ConnectionMessage {
	
	final String idmsg;
	final Node sender;
	final String msg;
	
	public ConnectionMessage( String idmsg, Node sender, String msg ) {
		this.idmsg = idmsg;
		this.sender = sender;
		this.msg = msg;
	}
}


class Unlock {
	
}