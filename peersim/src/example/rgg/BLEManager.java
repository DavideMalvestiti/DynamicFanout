package example.rgg;

import peersim.vector.SingleValueHolder;
import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;

import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;


public class BLEManager extends SingleValueHolder
implements CDProtocol, EDProtocol {
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
	
	
	private int mess = 0;
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public BLEManager(String prefix) { 
		super(prefix); 
		//BLEstatus = 1;
		
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
		
		
		if ( !Network.getble(node.getID()).isBusy() ){
			
			
			
			this.doAdvertising(node, pid);
			
		
		
		
		if ( !Network.getble(node.getID()).getMsgRequest().keySet().isEmpty() ) {
		System.out.println( node.getID() + "  miss  " + Network.getble(node.getID()).getMsgRequest().keySet() );
		}
		
		
		
		// uno alla volta 
		for ( Map.Entry<String, Node> entry : Network.getble(node.getID()).getMsgRequest().entrySet() ){
			
			
			this.respAdvertising( 
					node, 
					entry.getValue(), 
					entry.getKey(), 
					pid);
			
		}
		
		
		
		}
		
		
	
	}
	
	
	/**
	 * This is the standard method to define to process incoming messages.
	 */
	public void processEvent( Node node, int pid, Object event ) {
		
		//Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
        //Transport transport = (Transport) node.getProtocol( FastConfig.getTransport(pid) );
		
        
		if (event.getClass() == AdvertisingMessage.class) {
			AdvertisingMessage adv = (AdvertisingMessage)event;
			
			
			
			if ( ( adv.response == 0 ) 
					&& !Network.getble(node.getID()).getMessages().containsKey( adv.idmsg )
					&& !Network.getble(node.getID()).isBusy() ){
				
				
				// ()
				Network.getble(node.getID()).setBusy(true);
				System.out.println( node.getID() + "  y  " + adv.idmsg );
				
				
				
				this.respAdvertising( 
						node, 
						adv.sender, 
						adv.idmsg, 
						pid);
				
				
				// task scheduled
				Network.getble(node.getID()).getMsgRequest().put( adv.idmsg, adv.sender );
				
				// ()
				Network.getble(node.getID()).setBusy(false);
				
				
			} if ( adv.response == 1 
					&& !Network.getble(node.getID()).isBusy() ){
				
				
				
				//long delay = ( (sizemsg * 8) / bspeed ) * 1000;
				
				
				
				Network.getble(node.getID()).setBusy(true);
				Network.getble(adv.sender.getID()).setBusy(true);
				
				
				EDSimulator.add(
						1000,
						new ConnectionMessage( adv.idmsg, node, Network.getble(node.getID()).getMessages().get( adv.idmsg ) ),
						adv.sender,
						pid);
				
				
				// stallo 1000
				Network.getble(node.getID()).setBusy(false);
				
				
			}
			
			
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			// task canceled
			Network.getble(node.getID()).getMsgRequest().remove( con.idmsg );
			
			Network.getble(node.getID()).getMessages().put( con.idmsg, con.msg );
			
			
			System.out.println( node.getID() + " ho ricevuto messaggio " + con.idmsg );
			System.out.println( node.getID() + "  updatemiss  " + Network.getble(node.getID()).getMsgRequest().keySet() );
			
			
			Network.getble(node.getID()).setBusy(false);
			
			
			this.doAdvertising(node, pid);
		}
	
	}
	
	
	private void doAdvertising(Node node, int pid) {
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		Transport transport = (Transport) node.getProtocol( FastConfig.getTransport(pid) );
		
		if (linkable.degree() > 0) {
			
			
			// per ogni Neighbor con stato Scanning (4)
			Node peern = linkable.getNeighbor(
					CommonState.r.nextInt(linkable.degree()));
			
			
			
			
			if (  mess < 1 ){
			
			
			/* non va generato qua */ String newid = null; newid = NewMSG.newIdmsg(node);
			Network.getble(node.getID()).getMessages().put( newid, NewMSG.newMsg(node) );
			
			//m.msg.substring(0, 1);
			
			
			
			System.out.println( node.getID() + " new " + Network.getble(node.getID()).getMessages().keySet() );
			
			
			mess++;
			//}
			
			
			transport.send(
					node,
					peern,
					new AdvertisingMessage( newid, node, 0 ),
					pid);
			}
			
			
			
		}
	
	}
	
	
	private void respAdvertising(Node src, Node dest, String idmsg, int pid) {
		
		
		Transport transport = (Transport) src.getProtocol( FastConfig.getTransport(pid) );
    	
    	transport.send(
				src,
				dest,
				new AdvertisingMessage( idmsg, src, 1 ),
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