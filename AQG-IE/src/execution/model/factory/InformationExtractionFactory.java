package execution.model.factory;

import java.util.HashMap;
import java.util.Map;

import com.google.gdata.util.common.base.Pair;

import utils.persistence.persistentWriter;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.InformationExtractionSystemEnum;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

public class InformationExtractionFactory {

	private static Map<String, RelationExtractionSystem> cached;

	public static RelationExtractionSystem generateInstance(int idInformationExtractionId, WorkloadModel workload, persistentWriter pW, int relationConfiguration) {
		
		Pair<String,String> enum_name = pW.getInformationExtractionDescription(idInformationExtractionId, relationConfiguration);
				
		RelationExtractionSystem ret = getCached().get(enum_name.getSecond());
		
		if (ret != null){
			return ret;
		}
		
		switch (InformationExtractionSystemEnum.valueOf(enum_name.getFirst())) {
		
		case OPEN_CALAIS:
			
			ret = new OCRelationExtractionSystem(pW);

			break;
			
		default:
			
			ret = new TupleRelationExtractionSystem(pW,relationConfiguration, idInformationExtractionId,true,false) {
			}; //XXX see if I need to change the cached value ...
			
			break;

		}

		getCached().put(enum_name.getSecond(),ret);
		
		return ret;
	}

	private static Map<String,RelationExtractionSystem> getCached() {
		
		if (cached == null){
			cached = new HashMap<String, RelationExtractionSystem>();
		}
		return cached;
	}

}
