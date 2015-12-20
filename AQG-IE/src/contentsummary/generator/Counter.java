package contentsummary.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Counter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		DocumentCounter doc = new DocumentCounter();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("salida.out", true));
		
		String name = "/proj/db/NoBackup/pjbarrio/sites/Business/Bloomberg/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name)));
//		
//		bw.close();
//		
		Runtime r = Runtime.getRuntime();
//		
//		r.gc();
//		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Business/Forbes/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name)));
//		
//		bw.close();
//		
//		r.gc();
		
		
		
//		name = "/proj/db/NoBackup/pjbarrio/sites/General/CBS/";
		
//		bw.append(name + ": " +  doc.documentsCount(new File(name)));

//		bw.close();
		
//		r.gc();
		
//		name = "/proj/db/NoBackup/pjbarrio/sites/General/CNN/";
		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
		
//		bw.append(name + ": " +  doc.documentsCount(new File(name)));
	
//		bw.close();
		
//		r.gc();
		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
		
//		name = "/proj/db/NoBackup/pjbarrio/sites/General/UsNews/";
		
//		bw.write(name + ": " +  doc.documentsCount(new File(name)));

//		bw.close();
		
//		r.gc();
		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Movie/Hollywoodreporter/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name)));
//
//		bw.close();
//		
//		r.gc();
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Trip/People/";
//		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name)));
//
//		bw.close();
//
//		r.gc();
//		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Trip/TMZ/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name)));
//
//		bw.close();
//		
//		r.gc();
		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist/";
		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-TheEconomist.txt"));

//		bw.close();
		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Movie/CinemaBlend/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-cinemablend.txt"));
//
//		bw.close();
//		
//		r.gc();
//		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Movie/Empire/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-empire.txt"));
//
//		bw.close();
//		
//		r.gc();
//		
//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/Trip/Variety/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-variety.txt"));
//
//		bw.close();
//		
//		r.gc();

//		bw = new BufferedWriter(new FileWriter("salida.out", true));
//		
//		name = "/proj/db/NoBackup/pjbarrio/sites/General/CBS/";
//		
//		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-CBS.txt"));
//
//		bw.close();
//
//		r.gc();
		
		bw = new BufferedWriter(new FileWriter("salida.out", true));
		
		name = "/proj/db/NoBackup/pjbarrio/sites/Movie/Empire/";
		
		bw.write("\n" + name + ": " +  doc.documentsCount(new File(name),"count-Empire.txt"));

		bw.close();

		r.gc();
		
	}

}
