package execution.trunk.chart;

import java.util.Arrays;
import java.util.List;

import org.apache.poi.hssf.record.UseSelFSRecord;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.enumerations.AlgorithmEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.DocumentPerQueryEnum;
import exploration.model.enumerations.InformationExtractionSystemEnum;
import exploration.model.enumerations.QueryPoolExecutorEnum;
import exploration.model.enumerations.RelationConfigurationEnum;
import exploration.model.enumerations.RelationshipTypeEnum;
import exploration.model.enumerations.SampleAlgorithmEnum;
import exploration.model.enumerations.SampleGeneratorEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.enumerations.VersionEnum;
import exploration.model.enumerations.WorkloadEnum;

public class SampleIdHandler {

	private static persistentWriter pW = PersistenceImplementation.getWriter();
	
	private static final int TUPLES = 353756;
	private static final int RELATIONS_SVM = 353757;
	private static final int RELATIONS_SVM_TASW = 353758;
	private static final int RULES = 353759;
	private static final int RULES_TASW = 353760;
	private static final int TUPLES_NO_LARGE_DOMAIN = 353761;
	private static final int TUPLES_NO_SMALL_DOMAIN = 353762;
	private static final int RELATIONS_IG = 353764;
	private static final int RELATIONS_IG_TASW = 353765;
	private static final int RELATIONS_CHI2 = 353791;
	private static final int RELATIONS_CHI2_TASW = 353792;

	public static QueryPoolExecutorEnum[] execution_names = {QueryPoolExecutorEnum.SIMPLE,QueryPoolExecutorEnum.CYCLIC,
		QueryPoolExecutorEnum.SMARTCYCLIC,QueryPoolExecutorEnum.OPPORTUNITY,QueryPoolExecutorEnum.QUOTA,QueryPoolExecutorEnum.OPPORTUNITY_NOF,QueryPoolExecutorEnum.OPPORTUNITY_NOF_CHANGEDM,QueryPoolExecutorEnum.QUOTA_NOF_CHANGEDM,QueryPoolExecutorEnum.FQXTRACT};

	public static QueryPoolExecutorEnum[] execution_names_fq = {QueryPoolExecutorEnum.FQXTRACT};

	
	public static QueryPoolExecutorEnum[] execution_names_for_varying_K = {QueryPoolExecutorEnum.OPPORTUNITY_FOR_K,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_K,QueryPoolExecutorEnum.QUOTA_FOR_K};

	public static QueryPoolExecutorEnum[] execution_names_for_varying_M = {QueryPoolExecutorEnum.OPPORTUNITY_FOR_M_1,QueryPoolExecutorEnum.OPPORTUNITY_FOR_M_3,QueryPoolExecutorEnum.OPPORTUNITY_FOR_M_4,QueryPoolExecutorEnum.OPPORTUNITY_FOR_M_5,
		QueryPoolExecutorEnum.SMARTCYCLIC_FOR_M_1,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_M_3,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_M_4,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_M_5, QueryPoolExecutorEnum.QUOTA_FOR_M_1,QueryPoolExecutorEnum.QUOTA_FOR_M_3,QueryPoolExecutorEnum.QUOTA_FOR_M_4,QueryPoolExecutorEnum.QUOTA_FOR_M_5};

	public static QueryPoolExecutorEnum[] execution_names_for_varying_N = {QueryPoolExecutorEnum.OPPORTUNITY_FOR_N_10,QueryPoolExecutorEnum.OPPORTUNITY_FOR_N_50,QueryPoolExecutorEnum.OPPORTUNITY_FOR_N_75,QueryPoolExecutorEnum.OPPORTUNITY_FOR_N_100, QueryPoolExecutorEnum.SMARTCYCLIC_FOR_N_10,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_N_50,
		QueryPoolExecutorEnum.SMARTCYCLIC_FOR_N_75,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_N_100, QueryPoolExecutorEnum.QUOTA_FOR_N_10,QueryPoolExecutorEnum.QUOTA_FOR_N_50,QueryPoolExecutorEnum.QUOTA_FOR_N_75,QueryPoolExecutorEnum.QUOTA_FOR_N_100};

	
	
	public static QueryPoolExecutorEnum[] execution_names_for_varying_P = {QueryPoolExecutorEnum.OPPORTUNITY_FOR_P_002,QueryPoolExecutorEnum.OPPORTUNITY_FOR_P_005,QueryPoolExecutorEnum.OPPORTUNITY_FOR_P_01,QueryPoolExecutorEnum.OPPORTUNITY_FOR_P_015,QueryPoolExecutorEnum.OPPORTUNITY_FOR_P_025,
		QueryPoolExecutorEnum.SMARTCYCLIC_FOR_P_002,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_P_005,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_P_01,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_P_015,QueryPoolExecutorEnum.SMARTCYCLIC_FOR_P_025,
		QueryPoolExecutorEnum.QUOTA_FOR_P_002,QueryPoolExecutorEnum.QUOTA_FOR_P_005,QueryPoolExecutorEnum.QUOTA_FOR_P_01,QueryPoolExecutorEnum.QUOTA_FOR_P_015,QueryPoolExecutorEnum.QUOTA_FOR_P_025};

	public static QueryPoolExecutorEnum[] execution_names_for_varying_MP = {QueryPoolExecutorEnum.OPPORTUNITY_FOR_MP_005, QueryPoolExecutorEnum.OPPORTUNITY_FOR_MP_01, QueryPoolExecutorEnum.OPPORTUNITY_FOR_MP_015, QueryPoolExecutorEnum.OPPORTUNITY_FOR_MP_020,
		QueryPoolExecutorEnum.SMARTCYCLIC_FOR_MP_005, QueryPoolExecutorEnum.SMARTCYCLIC_FOR_MP_01, QueryPoolExecutorEnum.SMARTCYCLIC_FOR_MP_015, QueryPoolExecutorEnum.SMARTCYCLIC_FOR_MP_020, 
		QueryPoolExecutorEnum.QUOTA_FOR_MP_005, QueryPoolExecutorEnum.QUOTA_FOR_MP_01, QueryPoolExecutorEnum.QUOTA_FOR_MP_015, QueryPoolExecutorEnum.QUOTA_FOR_MP_020};
	
	public static DocumentPerQueryEnum[] limits = {DocumentPerQueryEnum._10,DocumentPerQueryEnum._20,DocumentPerQueryEnum._30,
		DocumentPerQueryEnum._40,DocumentPerQueryEnum._50};
	
	public static WorkloadEnum[] workloads = {WorkloadEnum.PERSONCAREER,WorkloadEnum.NATURALDISASTER,WorkloadEnum.MANMADEDISASTER,WorkloadEnum.PERSONTRAVEL,
		WorkloadEnum.VOTINGRESULT,WorkloadEnum.INDICTMENT_ARREST_TRIAL};

	public static QueryPoolExecutorEnum[] execution_names_for_no_filter = {QueryPoolExecutorEnum.OPPORTUNITY_NOF,QueryPoolExecutorEnum.QUOTA_NOF,QueryPoolExecutorEnum.OPPORTUNITY_NOF_CHANGEDM,QueryPoolExecutorEnum.QUOTA_NOF_CHANGEDM};

	public static QueryPoolExecutorEnum[] execution_names_cyclic = {QueryPoolExecutorEnum.CYCLIC};

	public static QueryPoolExecutorEnum[] execution_names_schecriteria = {QueryPoolExecutorEnum.CYCLIC, QueryPoolExecutorEnum.OPPORTUNITY_NOF, QueryPoolExecutorEnum.QUOTA_NOF_CHANGEDM};
	
	public static final RelationConfigurationEnum[] relationConfs = {RelationConfigurationEnum.PERSON_S_CAREER_CRF,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S, RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S, RelationConfigurationEnum.PERSON_S_LOCATION_S,
		RelationConfigurationEnum.POLITICALEVENT_CRF_PERSON_S,RelationConfigurationEnum.CHARGE_CRF_PERSON_S};

	public static final RelationshipTypeEnum[] relationshipTypes = {RelationshipTypeEnum.PERSONCAREER, RelationshipTypeEnum.NATURALDISASTER,RelationshipTypeEnum.MANMADEDISASTER, RelationshipTypeEnum.PERSONTRAVEL, RelationshipTypeEnum.VOTINGRESULT, RelationshipTypeEnum.INDICTMENTARRESTTRIAL};
	
	public static final SampleGeneratorEnum[] sampleGenerators = {SampleGeneratorEnum.SIMPLE, SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY,SampleGeneratorEnum.RR_GLOBAL_SAME_CARDINALITY};

	public static String getSimpleName(int configuration) {

		int parameter = pW.getSampleConfigurationBaseParameter(configuration);

		switch (parameter) {

			case TUPLES:
	
				return "TUPLES";
	
			case RELATIONS_SVM:
	
				return "RELATIONS_SVM";
	
			case RELATIONS_SVM_TASW:
	
				return "RELATIONS_SVM_TASW";
	
			case RULES:
	
				return "RULES";
	
			case RULES_TASW:
	
				return "RULES_TASW";
	
			case TUPLES_NO_LARGE_DOMAIN:
	
				return "TUPLES_NO_LARGE_DOMAIN";
	
			case TUPLES_NO_SMALL_DOMAIN:
	
				return "TUPLES_NO_SMALL_DOMAIN";
	
			case RELATIONS_IG:
	
				return "RELATIONS_IG";
	
			case RELATIONS_IG_TASW:
	
				return "RELATIONS_IG_TASW";
	
			case RELATIONS_CHI2:
	
				return "RELATIONS_CHI2";
	
			case RELATIONS_CHI2_TASW:
	
				return "RELATIONS_CHI2_TASW";
	
			default:
				return null;
		}

	}

	public static List<Integer> getSampleGeneration(SampleAlgorithmEnum algorithm,
			WorkloadEnum workload,
			RelationConfigurationEnum relconf, boolean countAll,
			SampleGeneratorEnum sg, boolean useAll, VersionEnum version,
			QueryPoolExecutorEnum queryPoolExecutor,DocumentPerQueryEnum documentPerQuery, int maxQueries,  int useless, InformationExtractionSystemEnum infExtSys) {
		
		return pW.getSampleConfiguration(getSampleAlgorithm(algorithm),getWorkload(workload),getRelationConfiguration(relconf),countAll,
				getSampleGenerator(sg),useAll,getVersion(version),getQueryPoolExecutor(queryPoolExecutor),getDocumentPerQuery(documentPerQuery), maxQueries, useless, getInformationExtractionSystem(infExtSys));
		
	}

//	public static List<Integer> getSampleGeneration(SampleAlgorithmEnum algorithm,
//			WorkloadEnum workload,
//			RelationConfigurationEnum relconf, boolean countAll,
//			SampleGeneratorEnum sg, boolean useAll, VersionEnum version,
//			QueryPoolExecutorEnum queryPoolExecutor,DocumentPerQueryEnum documentPerQuery, int maxQueries, int useless, InformationExtractionSystemEnum infExtSys) {
//		
//		return pW.getSampleConfiguration(getSampleAlgorithm(algorithm),getWorkload(workload),getRelationConfiguration(relconf),countAll,
//				getSampleGenerator(sg),useAll,getVersion(version),getQueryPoolExecutor(queryPoolExecutor),getDocumentPerQuery(documentPerQuery), maxQueries, useless, getInformationExtractionSystem(infExtSys));
//		
//	}

	
	private static int getInformationExtractionSystem(
			InformationExtractionSystemEnum infExtSys) {
		
		switch (infExtSys) {
		case DB_RET_BONG:
			return 17;
		case DB_RET_SPK:
			return 18;
		case DB_RET_SSK:
			return 19;
		case DB_RET_DG:
			return 20;			
		default:
			return -1;
		}
		
	}

	private static int getDocumentPerQuery(
			DocumentPerQueryEnum documentPerQuery) {
		
		switch (documentPerQuery) {
		case _10:
			
			return 10;
		case _20:
			
			return 20;
		case _30:
			
			return 30;
		case _40:
			
			return 40;
		case _50:
			
			return 50;
		case _100:
                        return 100;
		case _300:
                        return 300;
		case _500:
                        return 500;
		case _1000:
			return 1000;
			
		default:
			return -1;
		}
		
	}

	private static int getQueryPoolExecutor(
			QueryPoolExecutorEnum queryPoolExecutor) {
		
		switch (queryPoolExecutor) {
		case SIMPLE: return 1;
		case CYCLIC: return 2;
		case SMARTCYCLIC: return 364;//return 3;
		case OPPORTUNITY: return 764;//return 4;
		case QUOTA: return 864;//return 5;
		case OPPORTUNITY_NOF: return 1207;//return 4;
		case QUOTA_NOF: return 1208;//return 5;
		case OPPORTUNITY_NOF_CHANGEDM: return 1209;//return 4;
		case QUOTA_NOF_CHANGEDM: return 1210;//return 5;
		case FQXTRACT: return 1211;//return 5;
		case OPPORTUNITY_FOR_K: return 538;
		case SMARTCYCLIC_FOR_K: return 138;
		case QUOTA_FOR_K: return 938;
		case OPPORTUNITY_FOR_M_1: return 438;
		case OPPORTUNITY_FOR_M_3: return 538;
		case OPPORTUNITY_FOR_M_4: return 638;
		case OPPORTUNITY_FOR_M_5: return 738;
		case  SMARTCYCLIC_FOR_M_1: return 38;
		case SMARTCYCLIC_FOR_M_3: return 138;
		case SMARTCYCLIC_FOR_M_4: return 238;
		case SMARTCYCLIC_FOR_M_5: return 438;
		case  QUOTA_FOR_M_1: return 838;
		case QUOTA_FOR_M_3: return 938;
		case QUOTA_FOR_M_4: return 1038;
		case QUOTA_FOR_M_5: return 1138;
		case OPPORTUNITY_FOR_N_10: return 513;
		case OPPORTUNITY_FOR_N_50: return 538;
		case OPPORTUNITY_FOR_N_75: return 563;
		case OPPORTUNITY_FOR_N_100: return 588;
		case  SMARTCYCLIC_FOR_N_10: return 113;
		case SMARTCYCLIC_FOR_N_50: return 138;
		case  SMARTCYCLIC_FOR_N_75: return 163;
		case SMARTCYCLIC_FOR_N_100: return 188;
		case  QUOTA_FOR_N_10: return 913;
		case QUOTA_FOR_N_50: return 938;
		case QUOTA_FOR_N_75: return 963;
		case QUOTA_FOR_N_100: return 988;
		case  OPPORTUNITY_FOR_P_002: return 533;
		case OPPORTUNITY_FOR_P_005: return 538;
		case OPPORTUNITY_FOR_P_01: return 543;
		case OPPORTUNITY_FOR_P_015: return 548;
		case OPPORTUNITY_FOR_P_025: return 553;
		case  SMARTCYCLIC_FOR_P_002: return 133;
		case SMARTCYCLIC_FOR_P_005: return 138;
		case SMARTCYCLIC_FOR_P_01: return 143;
		case SMARTCYCLIC_FOR_P_015: return 148;
		case SMARTCYCLIC_FOR_P_025: return 153;
		case  QUOTA_FOR_P_002: return 933;
		case QUOTA_FOR_P_005: return 938;
		case QUOTA_FOR_P_01: return 943;
		case QUOTA_FOR_P_015: return 948;
		case QUOTA_FOR_P_025: return 953;
		case  OPPORTUNITY_FOR_MP_005: return 537;
		case  OPPORTUNITY_FOR_MP_01: return 538;
		case  OPPORTUNITY_FOR_MP_015: return 539;
		case  OPPORTUNITY_FOR_MP_020: return 540;
		case  SMARTCYCLIC_FOR_MP_005: return 137;
		case  SMARTCYCLIC_FOR_MP_01: return 138;
		case  SMARTCYCLIC_FOR_MP_015: return 139;
		case  SMARTCYCLIC_FOR_MP_020: return 140;
		case  QUOTA_FOR_MP_005: return 937;
		case  QUOTA_FOR_MP_01: return 938;
		case  QUOTA_FOR_MP_015: return 939;
		case  QUOTA_FOR_MP_020: return 940;
		case R_CYCLIC_100: return 1212;
		case R_CYCLIC_200: return 1213;
		case R_CYCLIC_300: return 1214;
		case R_CYCLIC_400: return 1215;
		case R_CYCLIC_500: return 1216;
		case CYCLIC_100: return 1217;
		case CYCLIC_200: return 1218;
		case CYCLIC_300: return 1219;
		case CYCLIC_400: return 1220;
		case CYCLIC_500: return 1221;
		default:
			
			return -1;
		}
		
	}

	private static int getVersion(VersionEnum version) {
		
		switch (version) {
		case INDEPENDENT:
			
			return 1;

		case DEPENDENT:
			return 2;
			
		default:
			return -1;
		}
		
	}

	private static int getSampleGenerator(SampleGeneratorEnum sg) {
		
		switch (sg) {
		case SIMPLE:
			
			return 1;

		case GLOBAL_SAME_CARDINALITY:
			return 2;
		case CLUSTER_SAME_CARDINALITY:
			return 3;
		case GLOBAL_PROPORTIONAL:
			return 4;
		case CLUSTER_PROPORTIONAL:
			return 5;
		case RR_GLOBAL_SAME_CARDINALITY:
			return 6;
		case RR_CLUSTER_SAME_CARDINALITY:
			return 7;
		case RR_GLOBAL_PROPORTIONAL:
			return 8;
		case RR_CLUSTER_PROPORTIONAL:
			return 9;	
		case CLUSTER_CLOSENESS_SAME_CARDINALITY:
			return 10;
		case CLUSTER_CLOSENESS_PROPORTIONAL:
			return 11;
		case RR_CLUSTER_CLOSENESS_SAME_CARDINALITY:
			return 12;
		case RR_CLUSTER_CLOSENESS_PROPORTIONAL:
			return 13;
	
			
		default:
			
			return -1;
		}
		
	}

	private static int getRelationConfiguration(
			RelationConfigurationEnum relconf) {
		
		switch (relconf) {
		case PERSON_S_CAREER_CRF:
			
			return 1;
			
		case NATURALDISASTER_SVM_LOCATION_S:
			return 2;
		case MANMADEDISASTER_CRF_LOCATION_S:
			return 3;
		case PERSON_S_LOCATION_S:
			return 4;
		case POLITICALEVENT_CRF_PERSON_S:
			return 5;
		case CHARGE_CRF_PERSON_S:
			return 6;

		default:
			return -1;
		}
		
	}

	private static int getWorkload(WorkloadEnum workload) {
		
		switch (workload) {
		case PERSONCAREER:
			
			return 17;
			
		case NATURALDISASTER:
			return 18;
			
		case MANMADEDISASTER:
			return 19;
			
		case PERSONTRAVEL:
			return 20;
			
		case VOTINGRESULT:
		
			return 21;
		case INDICTMENT_ARREST_TRIAL:
		
			return 22;
		default:
			
			return -1;
		}
		
	}

	private static int getSampleAlgorithm(SampleAlgorithmEnum algorithm) {
		
		switch (algorithm) {
		case TUPLES_50:
			
			return 353756;

		case RELATIONS_SVM:
			
			return 353757;
			
		case RELATIONS_SVM_TASW:
			
			return 353758;
			
		case RULES:
			
			return 353759;
		
		case RULES_TASW:
			
			return 353760;
			
		case TUPLES_50_NO_LARGE_DOMAIN:
			
			return 353761;
			
		case TUPLES_50_NO_SMALL_DOMAIN:
			
			return 353762;
			
		case RELATIONS_IG:
			
			return 353764;
			
		case RELATIONS_IG_TASW:
			
			return 353765;
			
		case RELATIONS_CHI2:
			
			return 353791;
			
		case RELATIONS_CHI2_TASW:
			
			return 353792;
			
		case TUPLES_10:
			return 353794;
		
		case TUPLES_10_NO_SMALL_DOMAIN:
			return 353795;
			
		case TUPLES_10_NO_LARGE_DOMAIN:
			return 353796;
			
		case TUPLES_20:
			return 353797;
		
		case TUPLES_20_NO_SMALL_DOMAIN:
			return 353798;
			
		case TUPLES_20_NO_LARGE_DOMAIN:
			return 353799;
		
		case TUPLES_30:
			return 353800;
		
		case TUPLES_30_NO_SMALL_DOMAIN:
			return 353801;
			
		case TUPLES_30_NO_LARGE_DOMAIN:
			return 353802;
		
		case TUPLES_40:
			return 353803;
		
		case TUPLES_40_NO_SMALL_DOMAIN:
			return 353804;
			
		case TUPLES_40_NO_LARGE_DOMAIN:
			return 353805;
			
		case SIGNIFICANT_PHRASES:
			
			return 353811;
			
		case SIGNIFICANT_PHRASES_TASW:
			
			return 353812;
			
		case USELESS_COLLECTION:
			return 353810;
			
		default:
			
			return -1;
		}
		
	}

	public static int getClusterFunctionId(ClusterFunctionEnum cluster) {
		
		switch (cluster){
		case CLASSIFICATION:
			return 1;
			
		case CLOSENESS_CLASSIFICATION:
			return 3;
		default:
			return -1;
		}
		
	}

	public static int getSimilarityFunctionId(
			SimilarityFunctionEnum similarity) {
		switch (similarity){
		case MANUAL_CLUSTER:
			return 5;
		default:
			return -1;
		}
	}

	public static int getRelationshipTypeId(RelationshipTypeEnum relation) {
		
		switch (relation){
		
		case PERSONCAREER:
			return 1;
		case NATURALDISASTER:
			return 2;
		case MANMADEDISASTER:
			return 3;
		case PERSONTRAVEL:
			return 4;
		case VOTINGRESULT:
			return 5;
		case INDICTMENTARRESTTRIAL:
			return 6;		
		default:
			return -1;
		}
		
	}

}
