package techniques.baseline.Tuples.seedGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

import utils.FileHandlerUtils;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;

public class SeedTupleGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		databaseWriter pW = (databaseWriter)PersistenceImplementation.getWriter();

		int group = Integer.valueOf(args[0]);

		int idWorkload = Integer.valueOf(args[1]); //17 to 22

		int idInformationExtractionSystem = Integer.valueOf(args[2]); //20 for instance
		
		int idRelationConfiguration = Integer.valueOf(args[3]); //1 for instance

		RelationExtractionSystem res = new TupleRelationExtractionSystem(pW,idRelationConfiguration, idInformationExtractionSystem,true,false);
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/}; 

		List<Database> databases = pW.getSamplableDatabases(group);

		String resName = res.getName();

		int[] seedTupleNumber = {1,2,3,4,5};

		WorkloadModel wm = pW.getWorkloadModel(idWorkload);

		for (int i = 0; i < version.length; i++) {

			Version ver = Version.generateInstance(version[i], wm);					

			for (int j = 0; j < databases.size(); j++) {

				Database db = databases.get(j);

				System.out.println(db.getId() +  "-" + version[i] + "-" + idWorkload);

				List<String> tuples = new ArrayList<String>(loadTuples(db,pW.getInitialMatchingTuplesWithSourcesFile(db, ver, wm, resName).getAbsolutePath()));
				
				for (int k = 0; k < seedTupleNumber.length; k++) {

					String file = pW.getSeedTuples(db.getName(),version[i],wm,seedTupleNumber[k],resName);

					Collections.shuffle(tuples);

					FileUtils.writeLines(new File(file), tuples);

				}

			}

		}

	}

	private static Collection<String> loadTuples(Database db, String tuplesFile) throws IOException {

		Set<String> s = new HashSet<String>();

		Hashtable<Document, ArrayList<Tuple>> tuples = TuplesLoader.loadDocumenttuplesTuple(db,tuplesFile);

		for (Enumeration<Document> e = tuples.keys(); e.hasMoreElements();){

			for (Tuple tuple : tuples.get(e.nextElement())) {

				s.add(tuple.toString());

			}

		}

		return s;

	}

}
