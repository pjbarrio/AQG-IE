package domain.caching.candidatesentence.tool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import opennlp.tools.util.InvalidFormatException;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import edu.columbia.cs.ref.algorithm.feature.generation.FeatureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.EntityBasedChunkingFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPPartOfSpeechFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPTokenizationFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.SpansToStringsConvertionFG;
import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.impl.BagOfNGramsKernel;
import edu.columbia.cs.ref.model.core.impl.DependencyGraphsKernel;
import edu.columbia.cs.ref.model.core.impl.OpenInformationExtractionCore;
import edu.columbia.cs.ref.model.core.impl.ShortestPathKernel;
import edu.columbia.cs.ref.model.core.impl.SubsequencesKernel;
import edu.columbia.cs.ref.model.feature.impl.SequenceFS;
import edu.columbia.cs.ref.model.re.Model;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import etxt2db.api.ClassificationExecutor;
import etxt2db.api.ClassificationModel;
import etxt2db.serialization.ClassificationModelSerializer;
import extraction.net.extractors.ClassificationBasedExtractor;
import extraction.net.extractors.EntityExtractor;
import extraction.net.extractors.StanfordNLPBasedExtractor;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.LocalCachedRelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

public class RelationConfiguration {

	private static Map<String, Integer> tagsTable;

	
	private static final int MANMADEDISASTERRELATION = 7;
	private static final int NATURALDISASTERRELATION = 8;
	private static final int VOTINGRESULTRELATION = 9;
	private static final int PERSONTRAVELRELATION = 10;
	private static final int CHARGERELATION = 11;
	private static final int PERSONCAREERRELATION = 12;

	private static final int PCCONF = 1;
	private static final int NDCONF = 2;
	private static final int MMDCONF = 3;
	private static final int PTCONF = 4;
	private static final int VRCONF = 5;
	private static final int IATCONF = 6;
	private static final String LOC_TAG = "LOCATION";
	private static final String MMD_TAG = "MANMADEDISASTER";
	private static final String ND_TAG = "NATURALDISASTER";
	private static final String PER_TAG = "PERSON";
	private static final String PE_TAG = "POLITICALEVENT";
	private static final String CA_TAG = "CAREER";
	private static final String IAT_TAG = "CHARGE";

	private static final int SPK = 1;
	private static final int SSK = 2;
	private static final int BNG = 3;
	private static final int DG = 4;
	private static final int OIE = 5;
	private static final String MMDNAME = "ManMadeDisaster";
	private static final String NDNAME = "NaturalDisaster";
	private static final String VRNAME = "VotingResult";
	private static final String PTNAME = "PersonTravel";
	private static final String PCNAME = "PersonCareer";
	private static final String IATNAME = "Indictment-Arrest-Trial";
	private static final int PERSON = 1;
	private static final int LOCATION = 2;
	private static final int MANMADEDISASTER = 3;
	private static final int NATURALDISASTER = 4;
	private static final int CHARGE = 5;
	private static final int POLITICALEVENT = 6;
	private static final int CAREER = 7;

	public static final int MANMADEDISASTEREXP = 1;
	public static final int NATURALDISASTEREXP = 2;
	public static final int POLITICALEVENTEXP = 3;
	public static final int PERSONLOCATION = 4;
	public static final int CHARGEEXP = 5;
	public static final int CAREEREXP = 6;

	private static final int MMDWLD = 19;
	private static final int NDWLD = 18;
	private static final int VRWLD = 21;
	private static final int PTWLD = 20;
	private static final int PCWLD = 17;
	private static final int IATWLD = 22;

	public static int getExtractor(int entity) {
		
		if (entity == RelationConfiguration.MANMADEDISASTEREXP){
			return 3;
		}
		if (entity == RelationConfiguration.NATURALDISASTEREXP){
			return 4;
		}
		if (entity == RelationConfiguration.POLITICALEVENTEXP){
			return 3;
		}
		if (entity == RelationConfiguration.PERSONLOCATION){
			return 15;
		}
		if (entity == RelationConfiguration.CAREEREXP){
			return 3;
		}
		if (entity == RelationConfiguration.CHARGEEXP){
			return 3;
		}
		
		return -2;
		
	}
	
	public static EntityExtractor createEntityExtractor(persistentWriter pW, int entity) throws ClassCastException, IOException, ClassNotFoundException {

		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";

		ClassificationModelSerializer serial = new ClassificationModelSerializer();
		
		if (entity == RelationConfiguration.MANMADEDISASTEREXP){
			
			//Load ManMadeDisaster
			
			String relation = "ManMadeDisaster";
			int split = 5;
			String technique = "CRF";
			int informationExtractionId = 3;
			
			ClassificationModel mmd = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + technique + "-reloaded.bin");
			ClassificationExecutor mmdexec = new ClassificationExecutor();
		
			return new ClassificationBasedExtractor(mmdexec,mmd,informationExtractionId,getTagsTable(pW),getTags(relation));
		
		} else if (entity == RelationConfiguration.NATURALDISASTEREXP){
		//Load NaturalDisaster
		
			String relation = "NaturalDisaster";
			int split = 5;
			String technique = "SVM";
			int informationExtractionId = 4;
			
			ClassificationModel nd = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + technique + "-reloaded.bin");
			ClassificationExecutor ndexec = new ClassificationExecutor();
		
			return new ClassificationBasedExtractor(ndexec, nd, informationExtractionId, getTagsTable(pW),getTags(relation));
		} else if (entity == RelationConfiguration.POLITICALEVENTEXP){
		//Load VotingResult
		
			String relation = "VotingResult";
			int split = 5;
			String technique = "CRF";
			int informationExtractionId = 3;
		
			ClassificationModel vr = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + technique + "-reloaded.bin");
			ClassificationExecutor vrexec = new ClassificationExecutor();
		
			return new ClassificationBasedExtractor(vrexec, vr, informationExtractionId, getTagsTable(pW),getTags(relation));
		} else if (entity == RelationConfiguration.PERSONLOCATION){
			
			//Load Person-Location
			String relation = "Person-Location";
			int informationExtractionId = 15;
			String classifier = "model/english.all.3class.distsim.crf.ser.gz";
			
			return new StanfordNLPBasedExtractor(informationExtractionId, getTagsTable(pW),getTags(relation), CRFClassifier.getClassifier(new File(classifier)));

			
		}  else if (entity == RelationConfiguration.CHARGEEXP){
			
			//Load Indictment-Arrest-Trial
			String relation = "Indictment-Arrest-Trial";
			int split = 5;
			String technique = "CRF";
			int informationExtractionId = 3;
			
			ClassificationModel vr = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + technique + "-reloaded.bin");
			ClassificationExecutor vrexec = new ClassificationExecutor();
		
			return new ClassificationBasedExtractor(vrexec, vr, informationExtractionId, getTagsTable(pW),getTags(relation));
			
		}
		
		else if (entity == RelationConfiguration.CAREEREXP){
			
			//Load Career
			String relation = "PersonCareer";
			int split = 5;
			String technique = "CRF";
			int informationExtractionId = 3;
			
			ClassificationModel vr = serial.deserializeClassificationModel(prefix+relation+"-IE/"+split+"/" + technique + "-reloaded.bin");
			ClassificationExecutor vrexec = new ClassificationExecutor();
		
			return new ClassificationBasedExtractor(vrexec, vr, informationExtractionId, getTagsTable(pW),getTags(relation));
			
		}
		
		//End
		
		return null;
		
	}
	
	private static Map<String,Integer> getTagsTable(persistentWriter pW) {
		
		if (tagsTable == null){
			tagsTable = pW.getEntityTypeTable();
		}
		
		return tagsTable;
	}
	
	private static List<String> getTags(String relation) {
		
		if (relation.equals("ManMadeDisaster"))
			return Arrays.asList(new String[]{"MANMADEDISASTER"});
		if (relation.equals("NaturalDisaster"))
			return Arrays.asList(new String[]{"NATURALDISASTER"});
		if (relation.equals("Indictment-Arrest-Trial"))
			return Arrays.asList(new String[]{"CHARGE"});
		if (relation.equals("VotingResult"))
			return Arrays.asList(new String[]{"POLITICALEVENT"});
		if (relation.equals("PersonCareer"))
			return Arrays.asList(new String[]{"CAREER"});
		if (relation.equals("Person-Location"))
			return Arrays.asList(new String[]{"PERSON","LOCATION"});
		return null;
	}
	
	public static int[] getEntitiesForExp(int entityExp) {
		
		if (entityExp == RelationConfiguration.MANMADEDISASTEREXP){
		
			return new int[]{3};
		}
		if (entityExp == RelationConfiguration.NATURALDISASTEREXP){
			return new int[]{4};
		}
		if (entityExp == RelationConfiguration.POLITICALEVENTEXP){
			return new int[]{6};
		}
		if (entityExp == RelationConfiguration.PERSONLOCATION){
			return new int[]{1,2};
		}
		if (entityExp == RelationConfiguration.CAREEREXP){
			return new int[]{7};
		}
		if (entityExp == RelationConfiguration.CHARGEEXP){
			return new int[]{5};
		}
		
		return null;
	}
	
	public static String getType(int relationConf) {
		
		switch (relationConf) {
		case MMDCONF:
			
			return "ManMadeDisaster";

		case NDCONF:
			
			return "NaturalDisaster";
			
		case PCCONF:
			
			return "PersonCareer";

		case PTCONF:
			
			return "PersonTravel";

		case IATCONF:
			
			return "Indictment-Arrest-Trial";

		case VRCONF:
			
			return "VotingResult";
			
		default:
			return null;
		}
		
	}

	public static Set<String> getTags(int relationConf) {

		Set<String> ret = new HashSet<String>();
		
		//has the extractorId,entityId
		if (relationConf == MMDCONF){ //ManMadeDisaster
			
			ret.add(LOC_TAG);
			ret.add(MMD_TAG);
			
		}
		if (relationConf == 2){ //NaturalDisaster
			
			ret.add(LOC_TAG);
			ret.add(ND_TAG);
			
		}
		if (relationConf == 5){ //VotingResult
			
			ret.add(PER_TAG);
			ret.add(PE_TAG);
			
		}
		if (relationConf == 4){ //PersonTravel
			
			ret.add(PER_TAG);
			ret.add(LOC_TAG);
			
		}
		if (relationConf == 1){ //PersonCareer
			
			ret.add(PER_TAG);
			ret.add(CA_TAG);
			
		}
		if (relationConf == 6){ //Indictment-Arrest-Trial
			
			ret.add(PER_TAG);
			ret.add(IAT_TAG);
			
		}

		return ret;
		
	}
	
	public static int getRelationConf(int relationexperiment) {
		
		if (relationexperiment == MANMADEDISASTERRELATION){
			return MMDCONF;
		}
		if (relationexperiment == NATURALDISASTERRELATION){
			return NDCONF;
		}
		if (relationexperiment == VOTINGRESULTRELATION){
			return VRCONF;
		}
		if (relationexperiment == PERSONTRAVELRELATION){
			return PTCONF;
		}
		if (relationexperiment == PERSONCAREERRELATION){
			return PCCONF;
		}
		if (relationexperiment == CHARGERELATION){
			return IATCONF;
		}
		
		return -1;
		
	}

	
	public static int[][] getEntities(int relationConf) { //returns the most popular first
		//has the extractorId,entityId
		if (relationConf == MMDCONF){ //ManMadeDisaster
			return new int[][]{new int[]{15, 2},new int[]{3, 3}};
		}
		if (relationConf == NDCONF){ //NaturalDisaster
			return new int[][]{new int[]{15, 2},new int[]{4, 4}};
		}
		if (relationConf == VRCONF){ //VotingResult
			return new int[][]{new int[]{15, 1},new int[]{3, 6}};
		}
		if (relationConf == PTCONF){ //PersonTravel
			return new int[][]{new int[]{15, 1},new int[]{15, 2}};
		}
		if (relationConf == PCCONF){ //PersonCareer
			return new int[][]{new int[]{15, 1},new int[]{3, 7}};
		}
		if (relationConf == IATCONF){ //Indictment-Arrest-Trial
			return new int[][]{new int[]{15, 1},new int[]{3, 5}};
		}
		
		return null;
	}

	public static StructureConfiguration generateStructureConfiguration(
			int infEsys) throws InvalidFormatException, IOException {

		FeatureGenerator<SequenceFS<Span>> tokenizer = new OpenNLPTokenizationFG("en-token.bin");
		FeatureGenerator<SequenceFS<Span>> fgChunk = new EntityBasedChunkingFG(tokenizer);
		FeatureGenerator<SequenceFS<String>> fgChuckString = new SpansToStringsConvertionFG(fgChunk);
		FeatureGenerator<SequenceFS<String>> fgPOS = new OpenNLPPartOfSpeechFG("en-pos-maxent.bin",fgChuckString);
		StructureConfiguration sc;
		switch (infEsys) {
		case SPK:
			
			sc = new StructureConfiguration(new ShortestPathKernel());
			
			sc.addFeatureGenerator(fgPOS);
			
			break;

		case SSK:
			
			sc = new StructureConfiguration(new SubsequencesKernel());
			
			sc.addFeatureGenerator(fgPOS);
			
			break;
			
		case BNG:
			
			sc = new StructureConfiguration(new BagOfNGramsKernel());
			
			sc.addFeatureGenerator(fgPOS);
			
			break;
			
		case DG:
			
			sc = new StructureConfiguration(new DependencyGraphsKernel());
			
			sc.addFeatureGenerator(fgPOS);
			
			break;
			
		case OIE:
			
			sc = new StructureConfiguration(new OpenInformationExtractionCore());
		
			break;
			
		default:
			
			return null;
			
		}
	
		return sc;
		
	}

	public static int getInformationExtractionSystemId(int infEsys) {

		switch (infEsys) {
		case SPK:

			return 7;

		case SSK:

			return 8;

		case BNG:

			return 6;

		case DG:

			return 9;

		case OIE:

			return 16;

		default:
			return -1;
		}

	}

	public static Model generateRelationExtractionSystem(persistentWriter pW, int informationExtractionId, int relationConf) {
		
		String fileModel = pW.getRelationExtractionModelsPrefix() + pW.getRESFileModel(informationExtractionId,pW.getRelationshipType(relationConf));
		
		try {
			return(Model) edu.columbia.cs.ref.tool.io.SerializationHelper.read(fileModel);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getRelationNameFromConf(int relationConf) {
		
		if (relationConf == MMDCONF){
			return MMDNAME;
		}
		if (relationConf == NDCONF){
			return NDNAME;
		}
		if (relationConf == VRCONF){
			return VRNAME;
		}
		if (relationConf == PTCONF){
			return PTNAME;
		}
		if (relationConf == PCCONF){
			return PCNAME;
		}
		if (relationConf == IATCONF){
			return IATNAME;
		}
		
		return null;
		
	}
	
	public static String getRelationName(int relation) {
		
		if (relation == MANMADEDISASTERRELATION){
			return MMDNAME;
		}
		if (relation == NATURALDISASTERRELATION){
			return NDNAME;
		}
		if (relation == VOTINGRESULTRELATION){
			return VRNAME;
		}
		if (relation == PERSONTRAVELRELATION){
			return PTNAME;
		}
		if (relation == PERSONCAREERRELATION){
			return PCNAME;
		}
		if (relation == CHARGERELATION){
			return IATNAME;
		}
		
		return null;
		
	}

	public static int getCachedInformationExtractionSystemId(int infEsys) {
		
		switch (infEsys) {
		case SPK:

			return 18;

		case SSK:

			return 19;

		case BNG:

			return 17;

		case DG:

			return 20;

		default:
			return -1;
		}
		
	}

	public static int getCachedInformationExtractionSystemSource(int infEsys) {
		
		switch (infEsys) {
		case 18:

			return SPK;

		case 19:

			return SSK;

		case 17:

			return BNG;

		case 20:

			return DG;

		default:
			return -1;
		}
		
	}
	
	public static Integer getExperiment(int entity) {
		switch (entity){
		case PERSON:
			return PTCONF;
		case LOCATION:
			return PTCONF;
		case MANMADEDISASTER:
			return MMDCONF;
		case NATURALDISASTER:
			return NDCONF;
		case CHARGE:
			return IATCONF;
		case POLITICALEVENT:
			return VRCONF;
		case CAREER:
			return PCCONF;
		default:
			return -1;
		}
	}

	public static Integer getForExtractor(int entity) {
		switch (entity){
		case PERSON:
			return PERSONLOCATION;
		case LOCATION:
			return PERSONLOCATION;
		case MANMADEDISASTER:
			return MANMADEDISASTEREXP;
		case NATURALDISASTER:
			return NATURALDISASTEREXP;
		case CHARGE:
			return CHARGEEXP;
		case POLITICALEVENT:
			return POLITICALEVENTEXP;
		case CAREER:
			return CAREEREXP;
		default:
			return -1;
		}
	}
	
	public static int getInformationExtractionBaseIdFromTuples(
			int informationExtractionId) {
		
		switch (informationExtractionId){
		case 17: //BONG
			return 6;
		case 18: //SPK
			return 7;
		case 19: //SSK
			return 8;
		case 20: //DG
			return 9;
		default:
			return -1;
		}
		
	}

	public static String getExtractorName(int informationExtractionId) {
		switch (informationExtractionId){
		case 17: //BONG
			return "BONG";
		case 18: //SPK
			return "SPK";
		case 19: //SSK
			return "SSK";
		case 20: //DG
			return "DG";
		default:
			return null;
		}
	}
	
	public static RelationExtractionSystem getRelationExtractionSystem(
			int relationExperiment, persistentWriter pW, int ieSystem,boolean cached, boolean takeCareOfSaving, int db, ContentExtractor ce) {
		
		String[] relationsExtracted = new String[]{RelationConfiguration.getRelationName(relationExperiment)};		

		int relationConf = getRelationConf(relationExperiment);
		
		RelationExtractionSystem tr = new TupleRelationExtractionSystem(pW, relationConf, ieSystem,cached,takeCareOfSaving); //Person Career
		
		System.out.println("Loading Works...");
		
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);

		return tr.createInstance(pW.getDatabaseById(db), interactionPersister , ce, relationsExtracted);
		
	}

	public static int getWorkload(int relationExperiment) {
		if (relationExperiment == MANMADEDISASTERRELATION){
			return MMDWLD;
		}
		if (relationExperiment == NATURALDISASTERRELATION){
			return NDWLD;
		}
		if (relationExperiment == VOTINGRESULTRELATION){
			return VRWLD;
		}
		if (relationExperiment == PERSONTRAVELRELATION){
			return PTWLD;
		}
		if (relationExperiment == PERSONCAREERRELATION){
			return PCWLD;
		}
		if (relationExperiment == CHARGERELATION){
			return IATWLD;
		}
		
		return 0;
	}

	

	
}
