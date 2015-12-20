package utils.id;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharSequenceUtils;

import exploration.model.Database;

import utils.persistence.persistentWriter;

public class Idhandler {

	private static final String TABLE_SEPARATOR = ",";
	private File file;
	private BufferedReader br;
	private Hashtable<String, Long> ids;
	private Hashtable<Long, String> reverseIds;
	private int size;
	private String line;
	private String[] pair;
	private persistentWriter pW;
	private Database database;
	private long lastId;
	private boolean cachIds;
	public Idhandler(String file) throws IOException {
		this(file,false);
	}

	public Idhandler(String file, boolean usesReverse) throws IOException {
		this.file = new File(file);
		ids = new Hashtable<String, Long>();
		reverseIds = new Hashtable<Long, String>();
		if (this.file.exists())
			initialize(usesReverse);
	}
	
	public Idhandler(Database database, persistentWriter pW, boolean usesReverse){
		this(database,pW,usesReverse,false);
	}
	
	public Idhandler(Database database, persistentWriter pW, boolean usesReverse, boolean cachIds){
		
		this.cachIds = cachIds;
		
		ids = new Hashtable<String, Long>();
		
		reverseIds = new Hashtable<Long, String>();
		
		this.pW = pW;
		
		this.database = database;
		
		ids = pW.getDocumentsTable(database);

		lastId = -1;
		
		for (Entry<String,Long> entry : ids.entrySet()) {
			
			if (usesReverse)
				reverseIds.put(entry.getValue(), entry.getKey());
						
			if (entry.getValue() > lastId)
				lastId = entry.getValue();
		}
		
		lastId++;
		
	}
	
	private synchronized void initialize(boolean usesReverse) throws IOException {
			
		br = new BufferedReader(new FileReader(file));
		
		line = br.readLine();
		
		size = 0;
		
		while (line!=null){
			
			size++;
			
			pair = Idhandler.parseLine(line);
			
			ids.put(pair[0],Long.valueOf(pair[1]));
			if (usesReverse)
				reverseIds.put(Long.valueOf(pair[1]),pair[0]);
			
			line = br.readLine();
		}
		
		br.close();
		
	}

	public synchronized static String[] parseLine(String line) {
		
		String[] pair = new String[2];
		
		int ind = line.lastIndexOf(TABLE_SEPARATOR);
		
		pair[0] = line.substring(0, ind);
		
		pair[1] = line.substring(ind+1);
		
		return pair;
	}

	public synchronized Long get(String document) {
		
		Long id = ids.get(document);
		
		if (id == null){
			id = this.addDocument(document);
		}
		
		return id;
	}

	public synchronized Long addDocument(String document) {
		
		long id = lastId+1;
		
		if (cachIds){

			pW.prepareDocument(database.getId(),id,document);

			
		}else{
			
			pW.insertDocument(database.getId(),id,document);

			
		}

		getIds().put(document,id);
		
		reverseIds.put(id, document);
		
//		try {
//			FileUtils.write(file, new String(wop + TABLE_SEPARATOR + id + "\n"), true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		lastId++;
		
		return id;
	
	}

	private synchronized Hashtable<String, Long> getIds() {
		
		if (ids == null){
			ids = new Hashtable<String, Long>();
		}
		return ids;
	}

//	public void printFile() throws IOException {
//		
//		BufferedWriter be = new BufferedWriter(new FileWriter(file));
//		
//		for (Enumeration<String> e = ids.keys();e.hasMoreElements();){
//			
//			String key = e.nextElement();
//			
//			Long l = ids.get(key);
//			
//			be.write(key + TABLE_SEPARATOR + l + "\n");
//			
//		}
//		
//		be.close();
//	}

	public synchronized String getDocument(Long value) {
		
		return reverseIds.get(value);
	
	}

	public synchronized long getSize(){
		return size;
	}
	
	public synchronized Set<String> getDocuments(){
		
		return ids.keySet();
	
	}

	public void clear() {
		
		ids.clear();
		reverseIds.clear();
		
	}
}
