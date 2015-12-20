package extraction.relationExtraction.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import online.documentHandler.contentExtractor.ContentExtractor;

import org.apache.commons.io.FileUtils;

import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.com.clearforest.CalaisLocator;
import extraction.com.clearforest.OpenCalaisRelationExtractor;
import extraction.relationExtraction.RelationExtractionSystem;

public class OCRelationExtractionSystem extends RelationExtractionSystem {

	public static final String licenseID = "y2k3xz3rwfpzg2rzd764rm96";
	
	
//	public static final String licenseID = "shf6uggkkp2au4jexwej9hbt";
	
	
	public static final String paramsFile = "/proj/dbNoBackup/pjbarrio/Exp/src/extraction/calaisParams.xml";

	private static final long WAITING_TIME = 250;

	private String paramsXML;

	private RDFRelationExtractor rdfExtractor;
	
	private OCRelationExtractionSystem(Database db,persistentWriter pW, String[] relations, Map<Document,String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor) {
		super(db,pW,relations,extractionTable,extractionFolder, contentExtractor);
		
		try {
			paramsXML = FileUtils.readFileToString(new File(paramsFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.rdfExtractor = new RDFRelationExtractor();
		
	}

	public OCRelationExtractionSystem(persistentWriter pW) {
		super(pW);
	}

	@Override
	protected RelationExtractionSystem createInstance(Database db,persistentWriter pW,
			Map<Document,String> extractionTable, File extractionFolder, ContentExtractor contentExtractor, String... relations) {
		
		return new OCRelationExtractionSystem(db,pW, relations,extractionTable,extractionFolder, contentExtractor);
		
	}

	@Override
	public String getName() {
		return "OpenCalais";
	}

	@Override
	protected void extract(String content, Writer writer) {
		
			String result = "";
			
			try {
				
				if (!content.trim().isEmpty()){
				
					int attemptsTotal = 3;
	
					int attempts = 0;
	
					do{
					
						Thread.sleep(3000 * attempts);
						
						Thread.sleep(WAITING_TIME);
						
						System.out.println("extracting...");
					
						result = new CalaisLocator().getcalaisSoap().enlighten(licenseID , content, paramsXML);
					
						attempts++;
						
					} while (result.startsWith("<Error") && attempts <= attemptsTotal); 
				
				}
				
				writer.write(result);
			
				return;
				
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			writer.write("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String getExtractedFormat() {
		return "rdf";
	}

	@Override
	protected List<Tuple> extractProcessed(String relation, String identifier) {
		
		String content;
		try {
			content = FileUtils.readFileToString(new File(identifier));
			return rdfExtractor.extract(relation,content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ArrayList<Tuple>(0);
		
	}

	@Override
	protected List<Tuple> extractRecentlyProcessed(String relation,
			String content) {
		return rdfExtractor.extract(relation,content);
	}

	@Override
	protected void _clear() {
		; //nothing to clean
		
	}

	protected String generateId(Database database, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor) {
		
		//OC does not need the relation.
		
		return new String(database.getId()+"-"+interactionPersister.getName()+"-"+contentExtractor.getName());
		
	}

	@Override
	protected int getId() {
		return 1;
	}

	@Override
	protected String generateId(Database website, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id) {
		return "1";
	}
	
}
