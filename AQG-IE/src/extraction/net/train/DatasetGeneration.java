package extraction.net.train;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Copy;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.generic.GenericTask;

import etxt2db.utils.Pair;

public class DatasetGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String pre = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
//		String relation = "ManMadeDisaster";
//		String tsk = "manmadedisaster";
//		String[] types = new String[]{"ManmadeDisasterAt","MANMADEDISASTER"};
//		String tag = "MANMADEDISASTER";
		
//		String relation = "NaturalDisaster";
//		String tsk = "natdisrelation";
//		String[] types = new String[]{"NaturalDisasterAt","NATURALDISASTER"};
//		String tag = "NATURALDISASTER";
		
//		String relation = "Indictment-Arrest-Trial";
//		String tsk = "charges";
//		String[] types = new String[]{"ChargeTo","CHARGE"};
//		String tag = "CHARGE";
				
//		String relation = "VotingResult";
//		String tsk = "votingresult";
//		String[] types = new String[]{"ofPoliticalEvent","POLITICALEVENT"};
//		String tag = "POLITICALEVENT";
		
		String relation = "PersonCareer";
		String tsk = "personcareer";
		String[] types = new String[]{"CareerOf","CAREER"};
		String tag = "CAREER";		
		
		int[] splits = new int[]{0,1,2,3,4,5}; //5 contains all
				
		File listFile = new File(pre + "Splits/" + relation + ".txt");
		
		String prefix = pre + relation + "/";
		
		File outputFolder = new File(pre + relation + "-IE/");
		
		String task = "/home/pjbarrio/Software/Callisto/tasks/org.mitre.example."+tsk+".jar";
		
		Task tskInstance = GenericTask.getInstance(new File(task));
		
		Set<String> typeTable = new HashSet<String>();
		
		for (int i = 0; i < types.length; i++) {
			
			typeTable.add(types[i]);
		
		}
		
		//After generating the splits.

		//Generate the sgml
		
		List<String> files = FileUtils.readLines(listFile);
		
		Set<Pair<Integer,Integer>> added = new HashSet<Pair<Integer,Integer>>();
		
		for (int i = 0; i < files.size(); i++) {
			
			if (new File(outputFolder,files.get(i) + ".sgml").exists())
				continue;
			
			added.clear();
			
			AWBDocument docum = AWBDocument.fromAIF(new File(prefix + files.get(i)).toURI(), tskInstance);
			
			String text = docum.getSignal().getCharsAt(0);
			
			SgmlDocument sgml = new SgmlDocument(text);
			
			Iterator<PhraseTaggingAnnotation> it = docum.getAllAnnotations();
			
			System.out.println(files.get(i));
			
			while (it.hasNext()){
				
				PhraseTaggingAnnotation ann = it.next();
			
				String role = ann.getAnnotationType().getName();
				
				if (!typeTable.contains(role))
					continue;
				
				int start = ann.getTextExtentStart();

				int end = ann.getTextExtentEnd();
				
				Pair<Integer, Integer> p = new Pair<Integer, Integer>(start, end);
				
				if (added.contains(p))
					continue;
				
				added.add(p);
				
				sgml.createContentTag(start, end, tag, false);

			}

			
			sgml.writeSgml(new FileWriter(new File(outputFolder,files.get(i) + ".sgml")));
			
			FileUtils.write(new File(outputFolder,files.get(i) + ".txt"), sgml.getSignalText());

			
		}
		
		//copy to the training and testing folder
		
		for (int i = 0; i < splits.length; i++) {
			
			File trainingFile = new File(pre + "Splits/"+relation+"-IE/train-"+splits[i]);
			List<String> trfiles = FileUtils.readLines(trainingFile);
			File testingFile = new File(pre + "Splits/"+relation+"-IE/test-"+splits[i]);
			List<String> tefiles = FileUtils.readLines(testingFile);
			
			File outFolderTraining = new File(pre+relation+"-IE/"+splits[i]+"/train/");
			File outFolderTesting = new File(pre+relation+"-IE/"+splits[i]+"/test/");
			File outFolderTestingEmpty = new File(pre+relation+"-IE/"+splits[i]+"/testempty/");
			
			copyFiles(outputFolder,trfiles,outFolderTraining,"sgml");
			copyFiles(outputFolder,tefiles,outFolderTesting,"sgml");
			copyFiles(outputFolder,tefiles,outFolderTestingEmpty,"txt");
			
		}
		
	}

	private static void copyFiles(File prefix, List<String> files,
			File outFolder,String extension) {

		for (int i = 0; i < files.size(); i++) {
			
			copyFiles(new File(prefix,files.get(i) + "." + extension), new File(outFolder,files.get(i)+"." + extension));
			
		}
		
		
	}

	private static void copyFiles(File from, File to) {
		
		Copy copier = new Copy();
				
		copier.setFile(from);
		
		copier.setTofile(to);
		
		copier.execute();
		
	}
}
