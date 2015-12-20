package extraction.net.train;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import etxt2db.api.ClassificationModel;
import etxt2db.api.ClassificationModelCreator;
import etxt2db.api.ClassificationModelCreator.MLTechnique;
import etxt2db.features.CharacterFeatureClassifier;
import etxt2db.features.CharacterNGramsFeatureClassifier;
import etxt2db.features.CharacterTypeFeatureClassifier;
import etxt2db.features.ContainsSubstringFeatureClassifier;
import etxt2db.features.EditableTokenFE;
import etxt2db.features.OpenNLPPOSandChunkingFeatureClassifier;
import etxt2db.features.PatternFeatureClassifier;
import etxt2db.features.StemmedValueFeatureClassifier;
import etxt2db.features.ValueCaseInsensitiveFeatureClassifier;
import etxt2db.features.ValueCaseSensitiveFeatureClassifier;
import etxt2db.serialization.ClassificationModelSerializer;

public class Train {

	protected static int[] spl = {0,1,2,3,4,5};

	protected static MLTechnique[] mltechniques = {ClassificationModelCreator.MLTechnique.HMM, ClassificationModelCreator.MLTechnique.CRF, ClassificationModelCreator.MLTechnique.SVM, ClassificationModelCreator.MLTechnique.MEMM};

	protected static String[] names = {"HMM","CRF","SVM","MEMM"}; 
	
	
	public class TrainerRunnable implements Runnable {
	
		private int folder;
		private MLTechnique mltechnique;
		private String name;
		private String type;
		private String relation;

		public TrainerRunnable(int folder, MLTechnique mltechnique, String name, String relation, String tag){
			
			this.folder = folder;
			this.mltechnique = mltechnique;
			this.name = name;
			this.relation = relation;
			this.type = tag;
		}
		
		public void run() {
			
			String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
			
			if (new File(prefix+relation+"-IE/"+folder+"/" + name + "-reloaded.bin").exists())
				return;
			
			
			ClassificationModelSerializer serial = new ClassificationModelSerializer();
			ClassificationModelCreator trainer = new ClassificationModelCreator();
			List<String> types = new ArrayList<String>();
			
			types.add(type);
	
			List<CharacterFeatureClassifier> features = new ArrayList<CharacterFeatureClassifier>();;
			features.add(new ValueCaseInsensitiveFeatureClassifier());
			features.add(new ValueCaseSensitiveFeatureClassifier());
			features.add(new PatternFeatureClassifier());
			features.add(new CharacterTypeFeatureClassifier());
			features.add(new ContainsSubstringFeatureClassifier("-"));
			features.add(new CharacterNGramsFeatureClassifier(3));
			features.add(new CharacterNGramsFeatureClassifier(4));
			features.add(new StemmedValueFeatureClassifier());
			features.add(new OpenNLPPOSandChunkingFeatureClassifier());
			
			
			try {

				EditableTokenFE featureExtractor = new EditableTokenFE(features,3);
				
				File trainingFile = new File(prefix+relation+"-IE/"+folder+"/train/");
				
				ClassificationModel model = trainer.trainMachineLearningModel(trainingFile,mltechnique,types, featureExtractor);
				
				serial.serializeClassificationModel(model, prefix+relation+"-IE/"+folder+"/" + name + "-reloaded-fast.bin");

			} catch (IOException e) {
				
				System.err.println("IO EXCEPTION: " + folder + " - " + name);
				
				e.printStackTrace();

			} catch (ParseException e) {
				
				System.err.println("PARSER EXCEPTION: " + folder + " - " + name);
				
				e.printStackTrace();
			
			}
				
		}

	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException{
		
		String relation = args[0];
		String tag = getTag(relation);
		
		if (args.length == 1){
			
			for (int i = 0; i < spl.length; i++) {
				
				for (int j = 0; j < names.length; j++) {
			
					new Train().execute(spl[i],mltechniques[j],names[j],relation,tag);
					
				}
				
			}
			
		} else {
			
			int split = Integer.valueOf(args[1]);
			
			int name = Integer.valueOf(args[2]);
			
			System.out.println(split + " - " + name);
			
			new Train().execute(spl[split], mltechniques[name], names[name], relation, tag);
			
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

	private void execute(int folder, MLTechnique mltechnique, String name, String relation, String tag) {
		
		System.err.println(folder + " - " + name);
				
		Thread t = new Thread(new TrainerRunnable(folder, mltechnique, name, relation,tag));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
