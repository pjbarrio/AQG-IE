package contentsummary.generator;


public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String stopWords = "../LuceneProject/stopWords.txt";
		
		String database = "UsNews";
		String dbclass = "General";
		
		OptimizedContentSummaryGenerator csg = new OptimizedContentSummaryGenerator(stopWords, database + "-OCS.txt");
		
		csg.setValues(true, 0, "");
		
		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/"+ dbclass +"/"+ database +"/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/"+ dbclass +"-" + database, true, true,false);
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Movie/Hollywoodreporter/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Movie-HolliywoodReporter", true, true);
		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Business/Bloomberg/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Business-Bloomberg", true, true);

//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Business/Forbes/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Business-Forbes", true, true);
	
//		csg.setValues(false, 982, "/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist/www.economist.com/vote/report_abuse/483105?page=0&nid=15580253&token=bcbb74287bc411fe76cd528054e2f2e0&sort=asc");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Business-TheEconomist", true, true);
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Trip/People/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Trip-People", true, true);
		
//		csg.setValues(false, 320, "/proj/db/NoBackup/pjbarrio/sites/Trip/TMZ/www.tmz.com/tag/david+beckham/page/2/index.html");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Trip/TMZ/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Trip-TMZ", true, true);
		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/General/CNN/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/General-CNN", true, true);
		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/General/UsNews/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/General-UsNews", true, true);
		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Trip/Variety/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Trip-Variety", true, true);
		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Business/Bloomberg", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Business-Bloomberg", true, true);

//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Movie/Hollywoodreporter/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Movie-HollywoodReporter", true, true);

//		csg.setValues(false, 803, "/proj/db/NoBackup/pjbarrio/sites/Movie/CinemaBlend/www.cinemablend.com/news.php?tag=will gluck");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Movie/CinemaBlend", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Movie-CinemaBlend", true, true);
		
//		csg.setValues(false, 261, "/proj/db/NoBackup/pjbarrio/sites/Trip/Variety/www.variety.com/profiles/Company/main/2061443/IMEC Productions.html?dataSet=1");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Trip/Variety/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Trip-Variety", true, true);

//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Business-TheEconomist", true, true);

		
//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Movie/ComingSoon/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Movie-ComingSoon", true, true);

//		csg.setValues(false, 902, "/proj/db/NoBackup/pjbarrio/sites/General/CBSNews/www.cbsnews.com/htdocs/politics/campaign2008/money/candidates_money.html");
//		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/General/CBSNews/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/General-CBSNews", true, true,false);

//		csg.setValues(true, 0, "");
//		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Movie/ReelMovieNews/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Movie-ReelMovieNews", true, true);


//		csg.setValues(true, 0, "");
		
//		csg.generateContentSummary("/proj/db/NoBackup/pjbarrio/sites/Trip/TheCelebrityCafe/", "/local/pjbarrio/Files/Research-Dataset/LuceneSites/Trip-TheCelebrityCafe", true, true);

		
	}
}
