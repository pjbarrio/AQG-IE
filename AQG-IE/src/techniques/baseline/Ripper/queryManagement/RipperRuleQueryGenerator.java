package techniques.baseline.Ripper.queryManagement;

import java.util.ArrayList;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import execution.workload.querygeneration.QueryGenerator;

public class RipperRuleQueryGenerator implements QueryGenerator<String>{

	private TextQuery emptyQuery = new TextQuery("");

	public TextQuery generateQuery(String rule) {
		
		String aux = rule.substring(1,rule.length()-1).replaceAll(" ", "");
		
		String spl[] = aux.split("\\)=>");
		
		String[] values = spl[0].split("\\)and\\(");
		
		List<String> output = new ArrayList<String>();
		
		for (int i = 0; i < values.length; i++) {
			
			String[] v = values[i].split("=");
			
			if (v[1].equals("0")){
//				do Nothing 
			}
			else {
				output.add(v[0]); 
			}
		
		}
		
		if ("".equals(output))
			return emptyQuery ;
		
		return new TextQuery(output);
	}
	
}
