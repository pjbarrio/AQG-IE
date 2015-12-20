package sample.generation.cluster;

import java.util.Map;
import java.util.Set;

public interface ClusterGenerator {

	public void addDatabase(String dbName);

	public Map<String, Set<String>> getClusters();

}
