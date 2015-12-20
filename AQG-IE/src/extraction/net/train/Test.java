package extraction.net.train;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import etxt2db.api.ClassificationExecutor;
import etxt2db.api.ClassificationModel;
import etxt2db.serialization.ClassificationModelSerializer;

public class Test {

	public class TesterRunnable implements Runnable{

		private int split;
		private String name;
		private String relation;
		private String type;

		public TesterRunnable(int split, String name, String relation, String tag){

			this.split = split;
			this.name = name;
			this.relation = relation;
			this.type = tag;

		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
		
			String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
			
			ClassificationModelSerializer serial = new ClassificationModelSerializer();

			List<String> types = new ArrayList<String>();
			
			types.add(type);

			File testingDirectory = new File(prefix+relation+"-IE/"+split+"/testempty/");
			
			File classifiedDirectory = new File(testingDirectory,"results" + name + "/");
			
			classifiedDirectory.mkdir();
			
//			try {
//				System.setOut(new PrintStream(new File(testingDirectory, "results." + name + ".output.testing")));
//			} catch (FileNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			ClassificationModel model = null;
			
			if (new File(prefix+relation+"-IE/"+split+"/" + name + "-reloaded.bin").exists())
				model = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + name + "-reloaded.bin");
			else if (new File(prefix+relation+"-IE/"+split+"/" + name + "-reloaded-fast.bin").exists())
				model = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + name + "-reloaded-fast.bin");
			else
				return;
		
			
			ClassificationExecutor exec = new ClassificationExecutor();

			File[] files = testingDirectory.listFiles();
			
			try {
			
				for (File file : files) {
					
					System.out.println(file);
						
					exec.createClassifiedFile(file, model, types, classifiedDirectory.getAbsolutePath());
					
				}
				
				System.out.println(exec.getClassifiedSegments(testingDirectory, model, types));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		}
		
	}
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ParseException {

		String relation = args[0];
		
		String tag = getTag(relation);
		
		if (args.length == 1){
			
			for (int i = 0; i < Train.names.length; i++) {
				
				String name = Train.names[i];
				
				for (int j = 0; j < Train.spl.length; j++) {
					
					int spl = Train.spl[j];
					
					new Test().execute(name,spl,relation, tag);
					
				}
				
			}
			
		} else {
			
			int spl = Integer.valueOf(args[1]);
			
			int name = Integer.valueOf(args[2]);
			
			new Test().execute(Train.names[name],Train.spl[spl],relation, tag);
			
		}
		
	}

	private static String getTag(String relation) {
		
		if (relation.equals("ManMadeDisaster"))
			return "MANMADEDISASTER";
		if (relation.equals("NaturalDisaster"))
			return "NATURALDISASTER";
		if (relation.equals("Indictment-Arrest-Trial"))
			return "CHARGE";
		if (relation.equals("VotingResult"))
			return "POLITICALEVENT";
		if (relation.equals("PersonCareer"))
			return "CAREER";
		
		return null;
	}
	
	private void execute(String name, int spl, String relation, String tag) {
		
		System.out.println(name + " - " + spl);
		
		Thread t = new Thread(new TesterRunnable(spl, name, relation, tag));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
