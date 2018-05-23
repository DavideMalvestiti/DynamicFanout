package example.rgg;

import peersim.core.*;

import peersim.edsim.EDSimulator;


public class BLEManagerLight extends BLEManager {
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
	
	private Cookie cookie;
	
	
	//--------------------------------------------------------------------------
	// Initialization
	//--------------------------------------------------------------------------
	
	/**
	 * @param prefix string prefix for config properties
	 */
	public BLEManagerLight(String prefix) {
		super(prefix);
		cookie = new Cookie(0,0,0,0);
		
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
			
			if ( !messages.containsKey( adv.idmsg ) 
					&& !busy 
					&& (bleState == 1 || bleState == 2) ){
				
				
				bleState = 2;
				EDSimulator.add(
						3,
						new ConnectionRequest( adv.idmsg, node, cookie ),
						adv.sender,
						pid);
				
				
			}
			
		} else if (event.getClass() == ConnectionRequest.class) {
			ConnectionRequest rq = (ConnectionRequest)event;
			
			if ( !busy 
					&& bleState == 3
					&& !((BLEManagerLight)rq.sender.getProtocol(pid)).busy ){
				
				
				// lock nodes
				this.busy = true;
				((BLEManagerLight)rq.sender.getProtocol(pid)).busy = true;
				bleState = 4;
				((BLEManagerLight)rq.sender.getProtocol(pid)).bleState = 4;
				
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
						new Unlock( rq.idmsg, cookie ),
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
				this.doAdvertising(con.idmsg, node, pid, cookie);
			}
			
		} else if (event.getClass() == Unlock.class) {
			
			battery = battery - 3;
			if (battery < 0) battery = 0;
			
			// unlock node
			this.busy = false;
			
			bleState = 1;
			
			receiver = -1;
		}
	
	}
	
	
	public void myNewmsg( String newid, String newmsg, Node node, int pid ) {
		
		messages.put( newid, newmsg );
		
		// battery*
		bleState = 3;
		this.doAdvertising(newid, node, pid, cookie);
	}

}