/*
 * ExtractionSystem.java
 *
 * Created on January 28, 2005, 11:25 PM
 */

package execution.informationExtraction;

import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.DocumentHandle;


/**
 *
 * @author  Alpa
 */


public interface ExtractionSystem {
  public Tuple[] execute(Document handles[]);
  public Tuple[] execute(Document handle);
}
