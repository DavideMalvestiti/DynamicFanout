package example.rgg;

import peersim.config.*;
import peersim.core.*;

import java.util.HashMap;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;


public class BLEManager implements CDProtocol, EDProtocol, Protocol, BLE {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	
	/* 0 Standby
	 * 1 Scanning
	 * 2 Initiating
	 * 3 Advertising
	 * 4 Connection
	 *   Slave -> Master
	 */
	protected int bleState = 0;
	protected boolean busy = false;
	
	protected double battery = 100.00;
	protected int dynamicFanout = 0;
	protected int advertiseLimit = 0;
	
	protected Map<String, String> messages;  // idmsg -> msg
	protected int timervalid;
	protected int receiver = -1;
	
	
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
	
	}
	
	
	/**
	 * This is the standard method to define to process incoming messages.
	 */
	public void processEvent( Node node, int pid, Object event ) {
		
		
		if (event.getClass() == AdvertisingMessage.class) {
			AdvertisingMessage adv = (AdvertisingMessage)event;
			Cookie cookie = adv.cookie;
			
			if ( !messages.containsKey( adv.idmsg ) 
					&& !busy 
					&& (bleState == 1 || bleState == 2) ){
				
				
				bleState = 2;
				EDSimulator.add(
						3,
						new ConnectionRequest( adv.idmsg, 
								node, 
								new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) ),
						adv.sender,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			Cookie cookie = rq.cookie;
			
			if ( !busy 
					&& bleState == 3
					&& !((BLEManager)rq.sender.getProtocol(pid)).busy 
					&& (cookie.tx < cookie.dynamicFanout) ){
				
				
				// invalid timer
				timervalid++;
				
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
						new Unlock( rq.idmsg, 
								new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) ),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			if ( busy && bleState == 4 ){
				
				
				messages.put( con.idmsg, con.msg );
				
				
				battery = battery - 3;
				if (battery < 0) battery = 0;
				
				// unlock node
				this.busy = false;
				
				bleState = 3;
				this.sendMessages(con.idmsg, node, pid);
			}
			
		} else if (event.getClass() == Unlock.class) {
			Unlock unl = (Unlock)event;
			Cookie cookie = unl.cookie;
			
			battery = battery - 3;
			if (battery < 0) battery = 0;
			
			// unlock node
			this.busy = false;
			
			receiver = -1;
			
			// ae = 0  tx++
			EDSimulator.add(
					0,
					new TimerIdmsg( unl.idmsg, 
							new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx + 1, 0 ), 
							timervalid ),
					node,
					pid);
			
			
		} else if (event.getClass() == TimerIdmsg.class) {
			TimerIdmsg tm = (TimerIdmsg)event;
			Cookie cookie = tm.cookie;
			
			if ( tm.me == timervalid ){
				if ( (cookie.ae < cookie.advertiseLimit)  &&  (cookie.tx < cookie.dynamicFanout)  ){
					
					
					// ae++
					EDSimulator.add(
							5000,
							new TimerIdmsg( tm.idmsg, 
									new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae + 1 ), 
									tm.me ),
							node,
							pid);
					
					bleState = 3;
					this.doAdvertising(tm.idmsg, 
							node, 
							pid,
							new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) );
					
				} else {
					
					timervalid = 0;
					bleState = 0;
					
					Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
					this.periodicActions( linkable.degree() );
				}
			}
			
		}
	
	}
	
	
	protected void periodicActions(int numNeighbors) {
		
		battery--;
		if (battery < 0) battery = 0;
		
		if (!busy){
			if ( battery > 20.00 ){
				
				dynamicFanout = ParametersUpdate.updatedynamicFanout(numNeighbors, battery);
				advertiseLimit = ParametersUpdate.updateadvertiseLimit(numNeighbors, battery);
				if (bleState == 0) bleState = 1;
				
			} else {
				
				bleState = 0;
			}
		}
	
	}
	
	
	protected void sendMessages( String idmsg, Node node, int pid ) {
		
		timervalid = 0;
		
		// ae = 0  tx = 0
		EDSimulator.add(
				0,
				new TimerIdmsg( idmsg, 
						new Cookie( this.dynamicFanout, this.advertiseLimit, 0, 0 ), 
						0 ),
				node,
				pid);
		
		
	}
	
	
	protected void doAdvertising( String idmsg, Node node, int pid, Cookie cookie ) {
		Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
		
		for (int i = 0; i < linkable.degree(); i++) {
			
			Node peern = linkable.getNeighbor(i);
			
			EDSimulator.add(
					3,
					new AdvertisingMessage( idmsg, 
							node,
							new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) ),
					peern,
					pid);
			
			
		}
	
	}
	
	
	public void myNewmsg( String newid, String newmsg, Node node, int pid ) {
		
		messages.put( newid, newmsg );
		
		// battery*
		bleState = 3;
		this.sendMessages(newid, node, pid);
	}
	
	
	/**
	 * @return the bleState
	 */
	public int getbleState() {
		return this.bleState;
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
	final Cookie cookie;
	
	public AdvertisingMessage( String idmsg, Node sender, Cookie cookie ) {
		this.idmsg = idmsg;
		this.sender = sender;
		this.cookie = cookie;
	}
}


/**
* The type of a connectionrequest. It contains an idmsg 
* of type String and the sender node of type {@link peersim.core.Node}.
*/
class ConnectionRequest {
	
	final String idmsg;
	final Node sender;
	final Cookie cookie;
	
	public ConnectionRequest( String idmsg, Node sender, Cookie cookie ) {
		this.idmsg = idmsg;
		this.sender = sender;
		this.cookie = cookie;
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
	
	final String idmsg;
	final Cookie cookie;
	
	public Unlock( String idmsg, Cookie cookie ) {
		this.idmsg = idmsg;
		this.cookie = cookie;
	}
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