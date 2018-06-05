package example.rgg;

import peersim.core.Node;


public interface BLE {
	
	public int getbleState();
	public Node getReceiver();
	public long getTimemsg();

	public void myNewmsg( String newid, String newmsg, Node node, int pid );

}