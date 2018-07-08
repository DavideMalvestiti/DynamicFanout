package example.rgg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import peersim.config.*;
import peersim.core.*;


public class BLEReport implements Control {
	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
	 * The protocol to look at.
     * 
     * @config
     */
	private static final String PAR_PROT = "protocol";
	
	// ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

	/**
     * Value obtained from config property
     * {@link #PAR_PROT}.
     */
    private final int pid;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters. Invoked by
     * the simulation engine.
     * 
     * @param prefix
     *            the configuration prefix for this class.
     */
	public BLEReport(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}
	
	
    // Control interface method.
	public boolean execute() {
		
		
		long max = -1;
		int nomsg = 0;
		for (int j = 0; j < Network.size(); j++){
			
			BLE blem = (BLE) Network.get(j).getProtocol(pid);
			
			if ( blem.getTimemsg() == -1 ) {
				nomsg++;
			} else if ( max < blem.getTimemsg() ) {
				max = blem.getTimemsg();
			}
		}
		
		try {
			String fname = "report.dat";
			File file = new File(fname);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath(), true);
			System.out.println("Writing to file " + fname);
			PrintStream pstr = new PrintStream(fos);
			
			pstr.println( ((double)max/1000) + "\t" + nomsg);
			
			fos.close();
			
		} catch (IOException e) {
            throw new RuntimeException(e);
        }

		return false;
    }

}