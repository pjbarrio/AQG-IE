package extraction.relationExtraction.training;

import gov.nist.atlas.Annotation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.generic.GenericTask;

public class AIFLoader {

	public static void main(String[] args) throws MalformedURLException, IOException {
		
//		String relation = "PersonTravel";
//		String task = "persontravel";
		
//		String relation = "ManMadeDisaster";
//		String task = "manmadedisaster";
		
//		String relation = "NaturalDisaster";
//		String task = "natdisrelation";
		
//		String relation = "Indictment-Arrest-Trial";
//		String task = "charges";
		
		String relation = "VotingResult-with-PoliticalEvent";
		String task = "votingresult";
		
//		String relation = "PersonCareer";
//		String task = "personcareer";
		
		File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Dataset/"+relation+"/AIF/");
		
		Task t = GenericTask.getInstance(new File("/home/pjbarrio/Software/Callisto/tasks/org.mitre.example."+task+".jar"));
		
		File[] files = folder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		
		Map<String,Integer> freq = new HashMap<String, Integer>();
		
		int totalDetections = 0;
		int totalMine = 0;
		int totalSingle = 0;
		
		for (int i = 0; i < files.length; i++) {
			
			AWBDocument document = null;
			
			try {
				
				document = AWBDocument.fromAIF(files[i].toURI(), t);
				
			} catch (Exception e) {
				
				System.out.println(files[i]);
				
				continue;
				
			}
			
			Iterator<PhraseTaggingAnnotation> it = document.getAllAnnotations();
			
			freq.clear();
			
			while (it.hasNext()){
				
				PhraseTaggingAnnotation ann = it.next();
				if (!ann.getAnnotationType().getName().startsWith("DETECTION")){
				
					
					Object value = ann.getAttributeValue("RelationId");
					
					if (value == null){
						totalSingle++;
						continue;
					}
					String rId = value.toString().trim();	
						
					if (rId.equals("")){
						System.out.println("Empty: " + files[i].getName());
					}

					Integer f = freq.remove(rId);
					
					if (f == null){
						f = 0;
					}
					
					freq.put(rId, f+1);
					
				}else{
					totalDetections++;
				}

//				Sees if the content can be retrieved.
				
				try{
				ann.getTextExtent();
				} catch (Exception e) {
					System.out.println("No Content: " + files[i].getName());
					break;
				}

				
				
			}
			
			for (Entry<String,Integer> entry : freq.entrySet()) {
				
				if (entry.getValue() != 2){ //More or less than one attribute share the same relation Id
					
					System.out.println("Review RIds: " + files[i].getName());
					
				} else {
					
					totalMine++;
				}
				
			}
			
		}
		
		System.out.println("Relation: " + relation);
		System.out.println("Total Detections: " + totalDetections);
		System.out.println("Total Mine: " + totalMine);
		System.out.println("Total Single: " + totalSingle);
	}
	
}
