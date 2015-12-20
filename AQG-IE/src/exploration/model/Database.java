package exploration.model;

public abstract class Database {

	private int id;
	private String name;
	private int size;
	private String type;
	private String modelType;

	public Database(int id, String name, int size, String type, String modelType) {
		
		this.id = id;
		this.name = name;
		this.size = size;
		this.type = type;
		this.modelType = modelType;
		
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public double getSize() {
		return size;
	}

	public int getId() {
		return id;
	}

	public String getModelType() {
		return modelType;
	}

	public abstract boolean isGlobal();

	public abstract boolean isSearchable();
	
	public abstract boolean isCrossable();
	
	public abstract boolean isOnline();
		
	public boolean equals(Object o){
		  
		return id == ((Database)o).id;
	  
	}

	public abstract boolean isCluster();
	
	public int hashCode(){
		return Integer.valueOf(id).hashCode();
	}

	public abstract String getIndex();


}
