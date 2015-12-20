package contentsummary.generator;

import java.io.File;

import utils.CommandLineExecutor;
import utils.FileHandlerUtils;

public class DatabaseStatisticsGenerator {

	private static final String FILE = "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Statistics.txt";
	private static final String SEP = ",";
	private static String namefile;
	private static File[] list;
	private static long documentCount;
	private static long acceptedTotal;
	private static long htmldocs;
	private static long rejectedTotal;
	private static long htmdocs;
	private static long aspdocs;
	private static long extractedasp;
	private static long acceptedEAsp;
	private static long rejectedEAsp;
	private static long fileCommandReject;
	private static long extractedjsp;
	private static long acceptedEJsp;
	private static long rejectedEJsp;
	private static CommandLineExecutor cle = new CommandLineExecutor();
	private static long fileCommandAccept;
	private static boolean aspdet;
	private static boolean jspdet;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String root = args[0];
		String db1 = args[1];

		namefile = args[5];
		initializeVariables();
		generateStatistics(new File(root + db1));
		appendOutput(db1);
		initializeVariables();
//		generateStatistics(new File(root + db2));
//		appendOutput(db2);
//		initializeVariables();
//		generateStatistics(new File(root + db3));
//		appendOutput(db3);
//		initializeVariables();
//		generateStatistics(new File(root + db4));
//		appendOutput(db4);
	}

	private static void appendOutput(String db) {
		FileHandlerUtils.appendEntry(FILE, generateOuput(db));
		
	}

	private static String generateOuput(String db) {
		
		String s = "";
		
		s = db + SEP + Long.toString(documentCount) + SEP + Long.toString(acceptedTotal) + SEP + Long.toString(rejectedTotal) + SEP + Long.toString(htmldocs) + SEP + Long.toString(htmdocs) + SEP + Long.toString(aspdocs) +
		SEP + Long.toString(extractedasp)+  SEP + Long.toString(acceptedEAsp) + SEP + Long.toString(rejectedEAsp) + SEP + Long.toString(extractedjsp) + SEP + Long.toString(acceptedEJsp) + SEP + Long.toString(rejectedEJsp) + SEP + 
				Long.toString(fileCommandReject)  + SEP + Long.toString(fileCommandAccept);
		
		
		return s;
		
	}

	private static void initializeVariables() {
	
		documentCount = 0; //acceptedTotal + rejectedTotal
		acceptedTotal = 0;
		rejectedTotal = 0;
		htmldocs = 0;
		htmdocs = 0;
		aspdocs = 0;
		extractedasp = 0;
		acceptedEAsp = 0;
		rejectedEAsp = 0;
		extractedjsp = 0;
		acceptedEJsp = 0;
		rejectedEJsp = 0;
		fileCommandReject = 0;
		fileCommandAccept = 0;
		
	}

	private static void generateStatistics(File file) {
		System.out.println(documentCount + " - " + acceptedTotal + " - " + rejectedTotal);
		if (file.isDirectory()){
			list = file.listFiles();
			for (File subfile : list) {
				generateStatistics(subfile);
			}			
		}else{
			documentCount++;
			if (file.getName().endsWith(".html")){
				htmldocs++;
				acceptedTotal++;
			}
			else if (file.getName().endsWith(".htm")){
				htmdocs++;
				acceptedTotal++;
			}	
			else if (file.getName().endsWith(".asp")){
				aspdocs++;
				acceptedTotal++;
			}
			else if (file.getName().endsWith(".mp3") || file.getName().endsWith(".mp4") || file.getName().endsWith(".avi") || file.getName().endsWith(".pdf") || file.getName().endsWith(".js")  || file.getName().endsWith(".css") || file.getName().endsWith(".jpg") || file.getName().endsWith(".wmv") || file.getName().endsWith(".gif") || file.getName().endsWith(".png")  || file.getName().endsWith(".swf")){
				rejectedTotal++;
				fileCommandReject++;
			}
			else{
				
				//Auxiliar!
				aspdet = false;
				jspdet = false;
				
				String ext = getExtension(file.getName());
				
				if (ext.startsWith("asp") || ext.startsWith("jsp")){
					
					if (ext.startsWith("asp")){
						extractedasp++;
						aspdet = true;
					}
					else{
						extractedjsp++;
						jspdet = true;
					}
					
				}
				
				//End Auxiliar
				
				String output = cle.getOutput("file -b " + FileHandlerUtils.format(file,namefile));
				
				output = output.toLowerCase();
				
				if (output.contains("htm")){
					fileCommandAccept++;
					acceptedTotal++;
					if (aspdet){
						acceptedEAsp++;
					}
					if (jspdet){
						acceptedEJsp++;
					}
				}
				else{
					
					append(file.getAbsolutePath(),output);
					
					fileCommandReject++;
					rejectedTotal++;
					if (aspdet){
						rejectedEAsp++;
					}
					if (jspdet){
						rejectedEJsp++;
					}
				}
			}
		}
	}

	private static void append(String file, String output) {
		
		String string = file + " <-> " + output;
		
		FileHandlerUtils.appendEntry("falseoutput" + namefile, string);
		
	}
	
	private static String getExtension(String path) {
		String aux = "yyy.xxx";
		aux = path.substring(path.indexOf('.')+1);
		return aux;
	}
	
}
