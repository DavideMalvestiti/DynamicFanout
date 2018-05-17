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
	private double battery = 100.00;
	public boolean busy = false;
	private int dynamicFanout = 0;
	private int advertiseLimit = 0;
	
	private int mss = 0;
	
	private Map<String, String> messages;  // idmsg -> msg
	private int timervalid;
	private int receiver = -1;
	
	
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
		
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		this.periodicActions( linkable.degree() );
		
		//----------------------------------------------------------------------
		
		if (mss == 0 && node.getIndex() == 1) {
		
		
		/* non va generato qua */ String newid = NewMSG.newIdmsg(node);
		messages.put( newid, NewMSG.newMsg(node) );
		
		
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
			
			if ( !messages.containsKey( adv.idmsg ) 
					&& !busy 
					&& (bleState == 1 || bleState == 2) ){
				
				
				bleState = 2;
				this.respAdvertising( 
						node, 
						adv.sender, 
						adv.idmsg, 
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			
			if ( !busy && bleState == 3 ){
				
				
				// lock nodes
				this.busy = true;
				((BLEManager)rq.sender.getProtocol(pid)).busy = true;
				bleState = 4;
				((BLEManager)rq.sender.getProtocol(pid)).bleState = 4;
				
				receiver = rq.sender.getIndex();
				
				// delay calculation
				int sizex = Integer.valueOf( messages.get( rq.idmsg ).substring(0, 1) );
				int sizemsg = (int) Math.pow(10, sizex);
				long delay = ( sizemsg / 125 ) * 1000 + 3;  // 125 Kbyte/s
				
				
				EDSimulator.add(
						delay,
						new ConnectionMessage( rq.idmsg, node, messages.get( rq.idmsg ) ),
						rq.sender,
						pid);
				
				EDSimulator.add(
						delay,
						new Unlock(),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			if ( busy && bleState == 4 ){
				
				
				messages.put( con.idmsg, con.msg );
				
				System.out.println( node.getID() + " ho ricevuto messaggio " + con.idmsg );
				
				
				// unlock node
				this.busy = false;
				
				bleState = 3;
				
				this.doAdvertising(con.idmsg, node, pid);
			}
			
		} else if (event.getClass() == Unlock.class) {
			
			// unlock node
			this.busy = false;
			bleState = 1;
			
			receiver = -1;
			
		} else if (event.getClass() == TimerIdmsg.class) {
			TimerIdmsg tm = (TimerIdmsg)event;
			Cookie cookie = tm.cookie;
			
			if ( tm.me == timervalid ){
				if ( (cookie.ae < cookie.advertiseLimit)  &&  (cookie.tx < cookie.advertiseLimit)  ){
					
					
					EDSimulator.add(
							5000,
							new TimerIdmsg( tm.idmsg, 
									new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae + 1 ), 
									tm.me ),
							node,
							pid);
					
					bleState = 3;
					this.doAdvertising(tm.idmsg, node, pid);
					
				} else {
					
					timervalid = 0;
					bleState = 0;
					
					Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
					this.periodicActions( linkable.degree() );
				}
			}
			
		}
	
	}
	
	
	private void periodicActions(int numNeighbors) {
		
		if (!busy){
			if ( battery > 20.00 ){
				
				if (bleState == 0){ bleState = 1; }
				
			} else {
				
				bleState = 0;
				
			}
		}
		
		dynamicFanout = ParametersUpdate.updatedynamicFanout(numNeighbors, battery);
		advertiseLimit = ParametersUpdate.updateadvertiseLimit(numNeighbors, battery);
		// battery--
		
	}
	
	
	@SuppressWarnings("unused")
	private void sendMessages( String idmsg, Node node, int pid ) {
		
		timervalid = 0;
		
		EDSimulator.add(
				0,
				new TimerIdmsg( idmsg, 
						new Cookie( this.dynamicFanout, this.advertiseLimit, 0, 0 ), 
						0 ),
				node,
				pid);
		
		
	}
	
	
	private void doAdvertising( String idmsg, Node node, int pid ) {
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		for (int v = 0; v < linkable.degree(); v++) {
			
			Node peern = linkable.getNeighbor(v);
			
			EDSimulator.add(
					3,
					new AdvertisingMessage( idmsg, node ),
					peern,
					pid);
			
			
		}
	
	}
	
	
	private void respAdvertising( Node src, Node dest, String idmsg, int pid ) {
		
		EDSimulator.add(
				3,
				new ConnectionRequest( idmsg, src ),
				dest,
				pid);
		
		
	}
	
	
	/**
	 * @return the receiver
	 */
	public Node getReceiver() {
		if (receiver != -1){
			return Network.get(receiver);
		}
		return null;
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


class TimerIdmsg {
	
	final String idmsg;
	final Cookie cookie;
	final int me;
	
	public TimerIdmsg( String idmsg, Cookie cookie, int me ) {
		this.idmsg = idmsg;
		this.cookie = cookie;
		this.me = me;
	}
}


class Cookie {
	
	final int dynamicFanout;
	final int advertiseLimit;
	final int tx;
	final int ae;
	
	public Cookie( int dynamicFanout, int advertiseLimit, int tx, int ae ) {
		this.dynamicFanout = dynamicFanout;
		this.advertiseLimit = advertiseLimit;
		this.tx = tx;
		this.ae = ae;
	}
}