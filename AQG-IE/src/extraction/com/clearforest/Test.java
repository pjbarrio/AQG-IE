package extraction.com.clearforest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;



public class Test {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		OpenCalaisRelationExtractor oCRE = null;//new OpenCalaisRelationExtractor("calaisParams.xml", "/proj/db/NoBackup/pjbarrio/OCOutput/ProductRelease.table");
		
		String input = "In what may otherwise be considered a cynical attempt to use a gay voice in the so-called \"liberal media\" to scare up some campaign dollars, a recent letter to supporters by Senator Scott Brown (R-MA) points to an obscure blog to level the claim that MSNBC anchor Rachel Maddow may run against him.\n\nAnd, of course, he needs your money to make sure that doesn\'t happen.\n\nPointing to UniversalHub, a relatively small Boston-area blog, Brown claims a key Democratic Party official is trying to recruit the Massachusetts-residing news anchor to run for Kennedy\'s former seat, cautioning that liberals will almost surely draft \"someone like Rachel Maddow\" to challenge Republicans in 2012.\n\n\"[Democrats] want a rubberstamp who will vote for their plans to expand government, increase debt and raise taxes, He wrote. \"Someone like Rachel Maddow. I\u00E2\u20AC\u2122m sure she\u00E2\u20AC\u2122s a nice person \u00E2\u20AC\u201D I just don\u00E2\u20AC\u2122t think America can afford her liberal politics.\"\n\nThe post was based on a tweet at Maddow by Massachusetts Democratic Party chairman John Walsh. It reads: \"Some are talking about you running vs Scott Brown in \'12. I\'m Chair of MA Dem Party. My email is johnewalsh@Comcast.net cell-617-650-9311\"";
		
		String result = null;//oCRE.IEProcess(input,"aux.txt");
		
		writeOutput(result,"output.rdf");
		
//		parseRDF(result);
	
		ArrayList<ArrayList<String>> tuples = null;//oCRE.extractTuples("PersonCareer");
		
		for (ArrayList<String> arrayList : tuples) {
			System.out.println("TUPLE: ");
			for (String string : arrayList) {
				System.out.println(string);
			}
		}
		
		String mainText = null;//oCRE.extractMainContent();
		
		System.out.println(mainText);
		
	}

	

	

	

	

	private static void writeOutput(String result, String outputFile) {

		try {
			
			new BufferedWriter(new FileWriter(new File(outputFile))).write(result);
		
		} catch (IOException e) {
		
			e.printStackTrace();
			return;
		
		}
		
	}

	private static void parseRDF(String result) {
	/*	
		ModelCom m = new ModelCom(new GraphMem());
		
		Model m2 = m.read(new StringReader(result), null);
		
		NodeIterator list = m2.listObjectsOfProperty(new PropertyImpl("http://s.opencalais.com/1/pred/person"));
		
		while (list.hasNext()){
			RDFNode obj = list.next();
			
			System.out.println(obj);
			
		}
	*/
		
		ModelCom m = new ModelCom(new GraphMem());
		
		Model m2 = m.read(new StringReader(result), null);

		StmtIterator iter = m2.listStatements();
		
		while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();     // get the subject
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode object = stmt.getObject();      // get the object
            System.out.println("\n\nSubject: " + subject + "\nPredicate: " + predicate.toString() + 
                    "\nObject: " + object);
        }
	}
	
		
	
}