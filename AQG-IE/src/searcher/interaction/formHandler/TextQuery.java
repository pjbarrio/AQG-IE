package searcher.interaction.formHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import exploration.model.Document;
import exploration.model.enumerations.ExperimentEnum;

import searcher.Searcher;

public class TextQuery {

	private final int MAX_SIZE = 750;
	
	public static final TextQuery emptyQuery = new TextQuery("");
	private String text;
	private List<String> words;
	
	public TextQuery(String text){

		if (text.length() > MAX_SIZE){
			
			int lastIndex = text.substring(0,MAX_SIZE).lastIndexOf(' ');
			
			this.text = text.substring(0, lastIndex);
			
		}else{
			this.text = text;
		}
		words = Arrays.asList(this.text.split(" "));
	}
	
	public TextQuery(List<String> words){
		
		this.words = new ArrayList<String>();
		
		if (words.isEmpty()){
			text = "";
		}else{
			
			text = words.get(0);
			
			this.words.add(words.get(0));
			
			for (int i = 1; i < words.size() && text.length() < MAX_SIZE; i++) {
				
				if (text.length() + words.get(i).length() <= MAX_SIZE){
				
					text = text + " " + words.get(i);

					this.words.add(words.get(i));
				
				}
				
				
			}

		}
	}
	
	public String getText(){
		return text;
	}
	
	@Override
	public String toString() {
		return getText();
	}
	
	@Override
	public int hashCode() {
		return words.hashCode();
	}
	
	@Override
	public synchronized boolean equals(Object obj) {
		
		if (obj instanceof TextQuery){
			
			TextQuery other = (TextQuery)obj;
			
			boolean equ = (text.equals(other.text));
			
			if (equ)
				return true;
			
			return false;
			
		}
		return false;
	}

	public List<String> getWords() {
		return words;
	}
	
}
