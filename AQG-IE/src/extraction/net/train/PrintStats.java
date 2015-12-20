package extraction.net.train;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.io.SgmlElement;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import etxt2db.utils.Pair;

public class PrintStats {

	private static final String RELOADED = "reloaded";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String relation = args[0];
		
		String tag = getTag(relation);
		
		String type = RELOADED;
		
		if (args.length == 1){
			
			System.out.println(relation);
			
			for (int i = 0; i < Train.names.length; i++) {
				
				String name = Train.names[i];
				
				for (int j = 0; j < Train.spl.length; j++) {
					
					int spl = Train.spl[j];
					
					new PrintStats().printStats(name,spl,relation, tag,pW, type);
					
				}
				
			}
			
		} else {
			
			int spl = Integer.valueOf(args[1]);
			
			int name = Integer.valueOf(args[2]);
			
			new PrintStats().printStats(Train.names[name],Train.spl[spl],relation, tag,pW, type);
			
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
	
	private void printStats(String name, int spl, String relation,
			String tag, persistentWriter pW, String type) throws IOException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";

		if (type.equals(RELOADED));
			if (!new File(prefix+relation+"-IE/"+spl+"/" + name + "-reloaded.bin").exists() &&  !new File(prefix+relation+"-IE/"+spl+"/" + name + "-reloaded-fast.bin").exists())
				return;
		
		File testingDirectory = new File(prefix+relation+"-IE/"+spl+"/testempty/");
		
		File classifiedDirectory = new File(testingDirectory,"results" + name + "/");
		
		File goldTestingDirectory = new File(prefix+relation+"-IE/"+spl+"/test/");
		
		File files = new File(prefix + "/Splits/"+relation+"-IE/test-"+spl);
		
		List<String> list = FileUtils.readLines(files);
		
		int tp = 0;
		int fp = 0;
		int fn = 0;
		
		for (int i = 0; i < list.size(); i++) {
			
			File mf = new File(classifiedDirectory,list.get(i) + ".txt");
			
			File gf = new File(goldTestingDirectory,list.get(i) + ".sgml");
			
			if (!mf.exists())
				continue;
			
			SgmlDocument msgml = new SgmlDocument(new FileReader(mf));
			
			char ch = msgml.getSignalText().charAt(0);
			
			SgmlDocument gsgml = new SgmlDocument(new FileReader(gf));
			
			int offset = getOffset(gsgml,ch); //because of how the classified files are generated
			
			Set<Pair<Integer,Integer>> mpairs = getPairs(msgml,0);
			
			Set<Pair<Integer,Integer>> gpairs = getPairs(gsgml,offset);
			
			for (Pair<Integer, Integer> pair : mpairs) {
				
				if (gpairs.contains(pair)){
					gpairs.remove(pair);
					tp++;
				} else {
					fp++;
				}
				
			}
			
			fn+=gpairs.size();
			
		}
		
		double precision = ((double)tp/(double)(fp+tp));
		double recall = ((double)tp/(double)(tp+fn));
		double fmeas = 2.0 * (precision * recall)/(precision+recall);
		
		
		System.out.println("Relation: " + relation);
		System.out.println(name + "-" + spl + "-TP: " + tp);
		System.out.println(name + "-" + spl + "-FP: " + fp);
		System.out.println(name + "-" + spl + "-FN: " + fn);
		System.out.println(name + "-" + spl + "-PR: " + precision);
		System.out.println(name + "-" + spl + "-RE: " + recall);
		System.out.println(name + "-" + spl + "-FM: " + fmeas);
		
		pW.writeExtractionPerformance(relation,name,spl,tp,fp,fn,precision,recall,fmeas,type);
		
	}

	private static int getOffset(SgmlDocument sgml, char ch) {
		
		char[] arr = sgml.getSignalText().toCharArray();
		
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == ch)
				return i;
			
		}
		
		return -1;
	}

	private static Set<Pair<Integer, Integer>> getPairs(SgmlDocument sgml,
			int offset) {
		
		Set<Pair<Integer,Integer>> ret = new HashSet<Pair<Integer,Integer>>();
		
		Iterator mite = sgml.iterator();
		
		while (mite.hasNext()){
			SgmlElement element = (SgmlElement)mite.next();
			ret.add(new Pair<Integer, Integer>(element.getStart()-offset, element.getEnd()-offset));
		}
		
		return ret;
		
	}

}
