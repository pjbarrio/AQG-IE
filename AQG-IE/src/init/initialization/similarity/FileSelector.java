package init.initialization.similarity;

import java.io.File;

public class FileSelector {

	public static final int MAIN_PAGE = 1;
	public static final int RESULT_PAGE = 2;
	public static final int DEEP_RESULT_PAGE = 3;
	private String type;
	private File prefix;

	public FileSelector(File prefix, String type) {
		this.prefix = prefix;
		this.type = type;
	}

	public File getWordFrequency(Integer index,int level){
		switch (level) {
		case MAIN_PAGE:
			return getMainPage(index);
		case RESULT_PAGE:
			File ret = getSearchPage(index);
			
			if (!ret.exists())
				return getMainPage(index);
			
			return ret;
			
		case DEEP_RESULT_PAGE:
			ret = getExtractionPage(index);
			if (!ret.exists())
				return getMainPage(index);
			return ret;
		default:
			break;
		}
		
		return null;
	}
	
	private File getMainPage(Integer index) {
		
		return new File(prefix, type + "/" + index);
		
	}

	private File getSearchPage(Integer index){
		
		int last = 9;
		
		File ret = new File(prefix,"afterSearch/" + type + "/" + index + "/0-"+last+".txt");
		
		while (last>=0 && !ret.exists()){
			last--;
			ret = new File(prefix,"afterSearch/" + type + "/" + index + "/0-"+last+".txt");
		}
		
		return ret;
	}
	
	private File getExtractionPage(Integer index){
		
		int last = 9;
		
		File ret = new File(prefix,"afterExtract/" + type + "/" + index + "/0-"+last+".txt");
		
		while (last>=0 && !ret.exists()){
			last--;
			ret = new File(prefix,"afterExtract/" + type + "/" + index + "/0-"+last+".txt");
		}
		
		return ret;
		
	}
	
}
