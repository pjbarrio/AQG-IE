package online.documentHandler.contentExtractor.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tools.ant.filters.StringInputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import exploration.model.enumerations.ContentExtractionSystemEnum;

import online.documentHandler.contentExtractor.ContentExtractor;

public class TikaContentExtractor extends ContentExtractor {

	private Parser parser;
	private Metadata metadata;
	private static String name = ContentExtractionSystemEnum.TIKA_CONTENT_EXTRACTOR.name();
	
	public TikaContentExtractor(){
		parser = new AutoDetectParser();
		metadata = new Metadata();
		
	}
	
	@Override
	public synchronized String extractContent(String content){
		
		try {
			ContentHandler handler = new BodyContentHandler(-1);
			parser.parse(new StringInputStream(content),handler,metadata,new ParseContext());
			return handler.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
		
		return "";
		
		
	}

	@Override
	public String getName() {
		return name ;
	}
	
	@Override
	public ContentExtractor newInstance() {
		return new TikaContentExtractor();
	}
	
	public static void main(String[] args) throws IOException {
		
		List<String> lines = FileUtils.readLines(new File("files.txt"));
		
		System.setOut(new PrintStream(new FileOutputStream("/dev/null")));
		
		try {
			
			for (String string : lines) {

				String cont = FileUtils.readFileToString(new File(string));
				
				String content = new TikaContentExtractor().extractContent(cont);
				
				System.err.println(string + " - " + new File(string).length() + " - " + content.length());
				
				System.out.println();

				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public ContentExtractionSystemEnum getEnum() {
		return ContentExtractionSystemEnum.TIKA_CONTENT_EXTRACTOR;
	}
}
