package controls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import managers.BLE;
import peersim.config.*;
import peersim.core.*;


public class BLEReport implements Control {
	// ------------------------------------------------------------------------
	// Parameters
	// ------------------------------------------------------------------------

	/**
     * The alpha parameter. It affects the distance relevance in the wiring
     * process.
     * 
     * @config
     */
	private static final String PAR_DENSITY = "density";
	
    private static final String PAR_RADIUS = "radius";

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
     * {@link #PAR_DENSITY}.
     */
    private final double density;

    /**
     * Value obtained from config property
     * {@link #PAR_RADIUS}.
     */
    private final double radius;

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
		density = Configuration.getDouble(prefix + "." + PAR_DENSITY);
		radius = Configuration.getDouble(prefix + "." + PAR_RADIUS);
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
		
		// 
		String den = Double.toString(density);
		if (density == 0.0005) den = "0.0005";
		else if (density == 0.0001) den = "0.0001";
		// 
		
		try {
			String fname = "n" + Network.size() + "r" + (int)radius + "d" + den.replace("." , ",") + ".dat";
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