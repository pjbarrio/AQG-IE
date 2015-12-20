package sample.generation.relation.trec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import extraction.relationExtraction.RelationExtractionSystem;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateDocumentsList {

	public static String[] relations = {"PersonParty","CompanyLocation","FamilyRelation","PersonAttributes","Extinction",
		"PoliticalRelationship","EnvironmentalIssue","PersonTravel",
		"PersonCareer","ProductRecall","CompanyLaborIssues","VotingResult","CompanyLegalIssues",
		"PersonLocation","IPO","CompanyMeeting","NaturalDisaster","CandidatePosition","Quotation","CompanyAffiliates",
		"DiplomaticRelations","ContactDetails","AnalystRecommendation","Buybacks","PatentFiling","CompanyInvestment",
		"CompanyLayoffs","Conviction","Indictment","EmploymentChange","ConferenceCall","Bankruptcy","StockSplit",
		"Dividend","CompanyCompetitor","CompanyEmployeesNumber","Trial","CompanyNameChange","DelayedFiling",
		"PoliticalEndorsement","CreditRating","BusinessRelation","BonusSharesIssuance","Acquisition",
		"CompanyForceMajeure","CompanyProduct","PersonCommunication","ArmedAttack","CompanyUsingProduct",
		"IndicesChanges","CompanyEarningsAnnouncement","MusicAlbumRelease","CompanyTechnology",
		"CompanyExpansion","CompanyFounded","AnalystEarningsEstimate","PersonEducation","PatentIssuance",
		"JointVenture","Arrest","MovieRelease","PersonEmailAddress","FDAPhase","SecondaryIssuance","GenericRelations",
		"CompanyRestatement","EquityFinancing","ManMadeDisaster","ArmsPurchaseSale","MilitaryAction","ProductIssues",
		"Alliance","DebtFinancing",
		"CompanyTicker","CompanyReorganization","CompanyAccountingChange","Merger",
		"EmploymentRelation","ProductRelease","CompanyListingChange","PersonRelation","CompanyEarningsGuidance",
		"PollsResult","CompanyCustomer"};
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Second algorithm in the sequence. Continuation of Generate Split.
		
		String filesLocation = "/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/";
		
		String extractor = "OpenCalais";
		
		String collection = "TREC";
		
		int size = 5000;		
		
		boolean hasAll = true;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int splits = 1;
		
		int alts = 5;
		
		File allFiles = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/Extraction/allFiles.txt");

		List<String> alldocs = FileUtils.readLines(allFiles);

		
		for (int rel = 0; rel < relations.length; rel++) {
			
			File useful = new File(pW.getUsefulDocumentsForCollection(collection,relations[rel],extractor));
			
			List<String> rdfusefulFiles = FileUtils.readLines(useful);
			
			Set<String> toCheck = new HashSet<String>(rdfusefulFiles);			
			
			List<String> uselessFiles = new ArrayList<String>(alldocs.size() - rdfusefulFiles.size());
			
			for (String string : alldocs) {
				
				String newF = string.substring(2); //To remove "./"
				
				if (!toCheck.contains(newF)){
					uselessFiles.add(filesLocation + newF.replace(".rdf", ""));
				}
				
			}
			
			List<String> usefulFiles = new ArrayList<String>(rdfusefulFiles.size());
			
			for (String string : rdfusefulFiles) {
				usefulFiles.add(filesLocation + string.replace(".rdf", ""));
			}
			
			System.out.format("Usefuls %d - Useless %d", usefulFiles.size(),uselessFiles.size());
			
			for (int j = 1; j <= alts; j++) {
				
				System.out.println("Split: " + j);
				
				Collections.shuffle(usefulFiles);
				
				Collections.shuffle(uselessFiles);
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size;
					
					System.out.println("Size: " + realsize);
					
					String usefulSplit = pW.getUsefulDocumentExtractionForRelation(collection,relations[rel],realsize,j,extractor);
					
					String uselessSplit = pW.getUselessDocumentExtractionForRelation(collection,relations[rel],realsize,j,extractor);
								
					FileUtils.writeLines(new File(usefulSplit), usefulFiles.subList(0, Math.min(realsize, usefulFiles.size())));
					
					FileUtils.writeLines(new File(uselessSplit), uselessFiles.subList(0, Math.min(realsize, usefulFiles.size())));
					
					if (!hasAll){ //it will remove the ones that have been used. Because of probabities of choosing the same being low.
					
						usefulFiles = usefulFiles.subList(Math.min(realsize, usefulFiles.size()), usefulFiles.size());

					}
				}
				
			}				

			
		}
		
		
	}

}
