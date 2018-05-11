package example.rgg;

import peersim.config.*;
import peersim.core.*;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;


public class BLEManager implements CDProtocol, EDProtocol, Protocol {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	/* 0 Standby
	 * 1 Advertising
	 * 2 Connection
	 * 3 Initiating
	 * 4 Scanning
	 */
	//private int BLEstatus;
	public boolean busy = false;
	private int mss = 0;
	
	
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
		
		
		if (mss == 0) {
		
		
		/* non va generato qua */ String newid = NewMSG.newIdmsg(node);
		Network.getble(node.getID()).getMessages().put( newid, NewMSG.newMsg(node) );
		
		//m.msg.substring(0, 1);
		
		
		//System.out.println( node.getID() + " new " + Network.getble(node.getID()).getMessages().keySet() );
		mss++;
		
		
		this.doAdvertising(newid, node, pid);
		}
		
		
		/*
		for ( Map.Entry<String, String> entry : Network.getble(node.getID()).getMessages().entrySet() ){
			
			
			this.doAdvertising(entry.getKey(), node, pid);
			
		}
		*/
		
		
	}
	
	
	/**
	 * This is the standard method to define to process incoming messages.
	 */
	public void processEvent( Node node, int pid, Object event ) {
		
		
		if (event.getClass() == AdvertisingMessage.class) {
			AdvertisingMessage adv = (AdvertisingMessage)event;
			
			if ( ( adv.response == 0 ) 
					&& !Network.getble(node.getID()).getMessages().containsKey( adv.idmsg )
					&& !busy ){
				
				
				
				//System.out.println( node.getID() + "  y  " + adv.idmsg );
				
				
				
				this.respAdvertising( 
						node, 
						adv.sender, 
						adv.idmsg, 
						pid);
				
				
				// task scheduled
				Network.getble(node.getID()).getMsgRequest().put( adv.idmsg, adv.sender );
				
				
				
			} if ( adv.response == 1 
					&& !busy ){
				
				
				
				//long delay = ( (sizemsg * 8) / bspeed ) * 1000;
				
				
				
				this.busy = true;
				((BLEManager)adv.sender.getProtocol(pid)).busy = true;
				
				
				EDSimulator.add(
						500,
						new ConnectionMessage( adv.idmsg, node, Network.getble(node.getID()).getMessages().get( adv.idmsg ) ),
						adv.sender,
						pid);
				
				
				EDSimulator.add(
						500,
						new Unlock(),
						node,
						pid);
				
				
			}
			
			
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			// task canceled
			Network.getble(node.getID()).getMsgRequest().remove( con.idmsg );
			
			Network.getble(node.getID()).getMessages().put( con.idmsg, con.msg );
			
			
			
			System.out.println( node.getID() + " ho ricevuto messaggio " + con.idmsg );
			
			
			this.busy = false;
			
			
			
			this.doAdvertising(con.idmsg, node, pid);
			
		} else if (event.getClass() == Unlock.class) {
			this.busy = false;
		}
	
	}
	
	
	private void doAdvertising(String idmsg, Node node, int pid) {
		
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		// per ogni Neighbor con stato Scanning o Initiating
		for (int i = 0; i < linkable.degree(); i++) {
			
			
			Node peern = linkable.getNeighbor(i);
			
			EDSimulator.add(
					0,
					new AdvertisingMessage( idmsg, node, 0 ),
					peern,
					pid);
			
			
		}
	
	}
	
	
	private void respAdvertising(Node src, Node dest, String idmsg, int pid) {
		
		EDSimulator.add(
				0,
				new AdvertisingMessage( idmsg, src, 1 ),
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
	final int response;
	
	public AdvertisingMessage( String idmsg, Node sender, int response ) {
		this.idmsg = idmsg;
		this.sender = sender;
		this.response = response;
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