package managers;

import java.util.HashMap;
import java.util.Map;

import peersim.config.*;
import peersim.core.*;

import peersim.edsim.EDSimulator;
import util.ParametersUpdate;


public class BLEManagerVer4 extends BLEManagerNew {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	private int iniCon = 0;
	
	private Map<String, Node> msgRequest;  // idmsg -> dest
	
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public BLEManagerVer4(String prefix) {
		super(prefix);
		
	}
	
	
	/**
	 *	Clone method of the class. Returns a deep copy of the BLEManager class. Used
	 *	by the simulation to initialize the {@link peersim.core.Network}
	 *	@return the deep copy of the BLEManager class.
	 */
	public Object clone(){
		Object blem = null;
		blem = (BLEManagerVer4)super.clone();;
		
		((BLEManagerVer4)blem).msgRequest = new HashMap<String, Node>();
		
		return blem;
	}
	
	
	//--------------------------------------------------------------------------
	// methods
	//--------------------------------------------------------------------------
	
	
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
				iniCon = 0;
				EDSimulator.add(
						3,
						new ConnectionRequest( adv.idmsg, 
								node, 
								new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) ),
						adv.sender,
						pid);
				
				
				msgRequest.put( adv.idmsg, adv.sender );
			}
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			Cookie cookie = rq.cookie;
			
			if ( !busy 
					&& bleState == 3
					&& !((BLEManagerVer4)rq.sender.getProtocol(pid)).busy 
					&& (cookie.tx < cookie.dynamicFanout) ){
				
				
				// invalid timer
				timervalid++;
				
				// lock nodes
				this.busy = true;
				((BLEManagerVer4)rq.sender.getProtocol(pid)).busy = true;
				bleState = 4;
				((BLEManagerVer4)rq.sender.getProtocol(pid)).bleState = 4;
				iniCon = 0;
				
				receiver = rq.sender.getIndex();
				
				// delay calculation
				int sizex = Integer.valueOf( messages.get( rq.idmsg ).substring(0, 1) );
				int sizemsg = (int) Math.pow(10, sizex);
				long delay = ( sizemsg / 125 ) * 1000 + 3 + 4230;  // 125 Kbyte/s
				
				
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
				msgRequest.remove( con.idmsg );
				
				if (timemsg1 == -1 && Integer.valueOf( con.idmsg.substring(0, 1) ) == 1) {
					timemsg1 = CommonState.getTime();
				}
				
				battery = battery - 3;
				if (battery < 0) battery = 0;
				
				
				// unlock node
				this.busy = false;
				
				bleState = 3;
				iniCon = 0;
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
			
			if ( tm.me == timervalid && bleState != 0 ){
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
					iniCon = 0;
					this.doAdvertising(tm.idmsg, 
							node, 
							pid,
							new Cookie( cookie.dynamicFanout, cookie.advertiseLimit, cookie.tx, cookie.ae ) );
					
				} else {
					
					timervalid = 0;
					bleState = 0;
					iniCon = 0;
					
					Linkable linkable = (Linkable) node.getProtocol( FastConfig.getLinkable(pid) );
					this.periodicActions( linkable.degree() );
				}
			}
			
		} else if (event.getClass() == ConReq.class) {
			ConReq crq = (ConReq)event;
			
			if ( !busy 
					&& bleState != 4
					&& bleState != 0
					&& !((BLEManagerVer4)crq.sender.getProtocol(pid)).busy ){
				
				
				// lock nodes
				this.busy = true;
				((BLEManagerVer4)crq.sender.getProtocol(pid)).busy = true;
				bleState = 4;
				((BLEManagerVer4)crq.sender.getProtocol(pid)).bleState = 4;
				iniCon = 0;
				
				receiver = crq.sender.getIndex();
				
				// delay calculation
				int sizex = Integer.valueOf( messages.get( crq.idmsg ).substring(0, 1) );
				int sizemsg = (int) Math.pow(10, sizex);
				long delay = ( sizemsg / 125 ) * 1000 + 3;  // 125 Kbyte/s
				
				
				EDSimulator.add(
						delay,
						new ConnectionMessage( crq.idmsg, node, messages.get( crq.idmsg ) ),
						crq.sender,
						pid);
				
				EDSimulator.add(
						delay,
						new Ul(),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == Ul.class) {
			
			battery = battery - 3;
			if (battery < 0) battery = 0;
			
			// unlock node
			this.busy = false;
			
			
			receiver = -1;
		}
	
	}
	
	
	protected void periodicActions(int numNeighbors) {
		
		battery--;
		if (battery < 0) battery = 0;
		
		if (!busy){
			if ( battery > 20.00 ){
				
				dynamicFanout = ParametersUpdate.updatedynamicFanout(numNeighbors, battery);
				advertiseLimit = ParametersUpdate.updateadvertiseLimit(numNeighbors, battery);
				
				if ( bleState == 0 ) {
					
					if (msgRequest.isEmpty()) bleState = 1;
					else bleState = 2;
				}
				
			} else {
				
				bleState = 0;
				iniCon = 0;
			}
		}
	
	}
	
	
	public void myNewmsg( String newid, String newmsg, Node node, int pid ) {
		
		messages.put( newid, newmsg );
		
		if (timemsg1 == -1 && node.getIndex() == 1) {
			timemsg1 = CommonState.getTime();
		}
		
		
		bleState = 3;
		this.sendMessages(newid, node, pid);
	}
	
	
	public void pull( Node node, int pid ) {
		
		if ( bleState == 2 ) {
			iniCon++;
		}
		
		if ( iniCon > 20 ) {
			
			for ( Map.Entry<String, Node> entry : msgRequest.entrySet() ){
				
				EDSimulator.add(
						3,
						new ConReq( entry.getKey(), node ) ,
						entry.getValue(),
						pid);
				
				
			}
		}
		
	}

}


/**
* The type of a connectionrequest. It contains an idmsg 
* of type String and the sender node of type {@link peersim.core.Node}.
*/
class ConReq {
	
	final String idmsg;
	final Node sender;
	
	public ConReq( String idmsg, Node sender ) {
		this.idmsg = idmsg;
		this.sender = sender;
	}
}


class Ul {
}