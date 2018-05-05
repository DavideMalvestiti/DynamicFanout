package example.rgg;

import peersim.core.Node;

public class NewMSG {
	
	
	
	private NewMSG(){
	}
	
	
	public static String newIdmsg(Node author){
		
		return String.valueOf( author.getID() ) + System.currentTimeMillis();
	}

	public static String newMsg(Node author){
		
		// 10^x Kbyte
		int sizemsg = 2;
		
		return String.valueOf( sizemsg ) + Long.toHexString(Double.doubleToLongBits(Math.random())) ;
	}

}