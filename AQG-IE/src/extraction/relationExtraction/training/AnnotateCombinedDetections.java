package extraction.relationExtraction.training;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.JAWB;
import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.io.SgmlElement;
import org.mitre.jawb.io.SgmlTag;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.generic.GenericTask;

import com.google.gdata.util.common.base.Pair;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

import execution.workload.tuple.Tuple;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.rdf.RDFPESExtractor;

public class AnnotateCombinedDetections {

	private static final String TYPE_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String TYPE_TEXT = "type";
	
	private static String collection;
	private static int selectednumber;
	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Dataset/";
		
		String[][] relations = new String[][]{{"Indictment","Arrest","Trial"},{"VotingResult","PollsResult"}};
		
		collection = "TREC";
		
		selectednumber = 650;
		
		pW = PersistenceImplementation.getWriter();
		
		for (int i = 0; i < relations.length; i++) {
			
			String rpref = prefix + getName(relations[i]);
			
			File f = new File(pW.getSelectedUsefulDocumentsForCollection(collection,getName(relations[i]), selectednumber));
			
			List<String> lines = FileUtils.readLines(f);
			
			for (int j = 0; j < lines.size(); j++) {
				
				File fff = new File(lines.get(j));
				
				String file = "file://" + lines.get(j);
				
				Graph g = new GraphMem();
				
				ModelCom model = new ModelCom(g);
				
				try {
				
					model.read(file);
				
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
				ResIterator resIte = model.listResourcesWithProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","document"));
				
				String content = "";
				
				while (resIte.hasNext()){ //There's only one
					
					Resource res = resIte.next();
					
					content = res.getProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","document")).getObject().toString();
					
				}
				
				SgmlDocument doc = new SgmlDocument(content);
				
				for (int r = 0; r < relations[i].length; r++) {
					
					List<Pair<Tuple, String[]>> tuples = RDFPESExtractor.extract(model, relations[i][r]);
					
					for (int k = 0; k < tuples.size(); k++) {
						
						int offset = Integer.valueOf(tuples.get(k).getSecond()[3]);
						int length = Integer.valueOf(tuples.get(k).getSecond()[4]);
						
						try {
							
							doc.createContentTag(offset, offset+length, "DETECTION" + relations[i][r], true);
							
						} catch (Exception e) {
							System.out.println("bye");
						}
						
						System.out.println("Hi!");
					}
					
				}
				
				doc.writeSgml(new FileWriter(new File(rpref,fff.getName() + ".sgml")));
				
			}
			
		}

	}

	private static String getName(String[] relation) {
		
		String name = relation[0];
		
		for (int i = 1; i < relation.length; i++) {
			
			name += "-" + relation[i];
			
		}
		
		return name;
		
	}

}
