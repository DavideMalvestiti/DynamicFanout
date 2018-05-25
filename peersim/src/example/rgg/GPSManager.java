package example.rgg;

import java.util.ArrayList;

import peersim.core.*;

import peersim.edsim.EDSimulator;


public class GPSManager extends BLEManager {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	private Cookie cookie;
	
	private ArrayList<Node> pos;
	//private boolean lost = false;
	
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public GPSManager(String prefix) {
		super(prefix);
		cookie = new Cookie(0,0,0,0);
		
	}
	
	
	/**
	 *	Clone method of the class. Returns a deep copy of the BLEManager class. Used
	 *	by the simulation to initialize the {@link peersim.core.Network}
	 *	@return the deep copy of the BLEManager class.
	 */
	public Object clone(){
		Object blem = null;
		blem = (GPSManager)super.clone();;
		
		((GPSManager)blem).pos = new ArrayList();
		
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
			//AdvertisingMessage adv = (AdvertisingMessage)event;
			
			
			
			
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			
			if ( !busy ){
				
				
				// lock nodes
				this.busy = true;
				((GPSManager)rq.sender.getProtocol(pid)).busy = true;
				
				receiver = rq.sender.getIndex();
				
				// delay calculation
				int sizemsg = 100;  // 100 Kbyte
				long delay = ( sizemsg / 125 ) * 1000 + 3;  // 125 Kbyte/s
				
				
				EDSimulator.add(
						delay,
						new ConnectionMessage( rq.idmsg, node, messages.get( rq.idmsg ) ),
						rq.sender,
						pid);
				
				EDSimulator.add(
						delay,
						new Unlock( rq.idmsg, cookie ),
						node,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionMessage.class) {
			ConnectionMessage con = (ConnectionMessage)event;
			
			if ( busy && bleState == 4 ){
				
				
				pos.add( con.sender );
				
				battery = battery - 1;
				if (battery < 0) battery = 0;
				
				// unlock node
				this.busy = false;
				
				
				
			}
			
		} else if (event.getClass() == Unlock.class) {
			
			battery = battery - 1;
			if (battery < 0) battery = 0;
			
			// unlock node
			this.busy = false;
			
			receiver = -1;
		}
	
	}
	
	
	public void myNewmsg( String newid, String newmsg, Node node, int pid ) {
		
		pos.clear();
		//lost = true;
	}

}