package searcher.interaction.formHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class POSTInteractionForm extends InteractionForm {

	public POSTInteractionForm(String formFile, String encoding){
		super(formFile,encoding);
	}
	
	public POSTInteractionForm(String formFile) {
		super(formFile);
	}

	@Override
	public String generateParameters(TextQuery query, List<String> inputNames) {
		
		//first is always the text search form
		
		try {
			
			String data = URLEncoder.encode(inputNames.get(0), getEncoding()) + "=" + URLEncoder.encode(query.getText(), getEncoding());
			
			for (int i = 1; i < inputNames.size(); i++) {
				
				String value = getValue(inputNames.get(i),getProcessedBefore(inputNames, i));
				
				if (value!=null && !value.trim().equals("")){
				
					data+= "&" + URLEncoder.encode(inputNames.get(i), getEncoding()) + "=" + URLEncoder.encode(value, getEncoding());
				
				}
			}
			
			return data;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub

		return null;

	}

	private int getProcessedBefore(List<String> inputNames, int i) {
		
		int count = 0;
		
		for (int j = 1; j < i; j++) {
			
			if (inputNames.get(j).equals(inputNames.get(i)))
				count++;
			
		}
		
		return count;
		
	}
}
