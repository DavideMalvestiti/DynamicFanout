package example.rgg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class Parser {

	public Parser() {
		
		int num = 1000;
		int radius;
		String density;
		String line;
		String[] parts;
		FileInputStream sorgente;
		Scanner sc;
		
		try {
			File file = new File("result.dat");
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath(), true);
			PrintStream pstr = new PrintStream(fos);
			pstr.println(""); pstr.println("");
			
			
			for (int i = 0; i < 3; i++){
				
				radius = getradius(i);
				int k = 4;
				if (radius == 50) k = 6;
				
				for (int j = 0; j < k; j++){
					
					density = getdensity(j);
					String fname = "risultati/n" + num + "r" + radius + "d" + density + ".dat";
					
					sorgente = new FileInputStream(fname);
					sc = new Scanner(sorgente);
					
					while ( sc.hasNextLine() ){
						line = sc.nextLine();
						
						if (line != "") {
							
							parts = line.split("\t");
							int nx = num - Integer.valueOf( parts[1] ) ;
							
							pstr.println( parts[0] + "\t" + nx );
						}
					}
					
					sc.close();
					pstr.println("");
				}
				pstr.println("");
			}
			
			System.out.println("Writing to file result.dat");
			fos.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
        }
	
	}

	public int getradius( int i ) {
		int r = 0;
		
		switch (i) {
		case 0:
			r = 10;
			break;
		case 1:
			r = 15;
			break;
		case 2:
			r = 50;
			break;
		}
		
		return r;
	}

	public String getdensity( int j ) {
		String d = null;
		
		switch (j) {
		case 0:
			d = "0,02";
			break;
		case 1:
			d = "0,01";
			break;
		case 2:
			d = "0,008";
			break;
		case 3:
			d = "0,001";
			break;
		case 4:
			d = "0,0005";
			break;
		case 5:
			d = "0,0001";
			break;
		}
		
		return d;
	}	

	public static void main(String[] args) {
		new Parser();

	}

}