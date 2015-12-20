package extraction.collectionProcessor;

import java.io.File;

import extraction.collectionProcessor.local.NYTCorpusDocument;
import extraction.collectionProcessor.local.NYTCorpusDocumentParser;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class ExtractNYTCollection {

	public static void main(String[] args) {
		
		
//		File prefix = new File("/local/pjbarrio/Files/Downloads/NYTTest/NYTTest");
//		
//		File toSave = new File("/local/pjbarrio/Files/Downloads/NYTTest/NYTTestExtraction");

		//Once I have identified those that are broken, I run:
		// mkdir ../NYTTestFixed
		//cp `find | grep '1811897.xml\|1715181.xml\|1174129.xml\|1086595.xml\|0644677.xml\|0975838.xml\|0874564.xml\|1056702.xml\|1513078.xml\|1494199.xml\|1810201.xml\|0760416.xml\|0964893.xml\|1142532.xml\|1666699.xml\|0759628.xml\|1540364.xml\|0971421.xml\|1516189.xml\|1755278.xml\|1124886.xml\|0665084.xml\|1017052.xml\|1260670.xml\|1051196.xml\|1819787.xml\|1683790.xml\|0748093.xml\|0806990.xml\|0914226.xml\|1647882.xml\|1081352.xml\|1643632.xml\|1723954.xml\|0705936.xml\|1685672.xml\|0689086.xml\|0813443.xml\|1645462.xml\|0875549.xml\|1748934.xml\|0667104.xml\|1698003.xml\|1162721.xml\|1008709.xml\|0751093.xml\|1064505.xml\|0653850.xml\|1529263.xml\|0679034.xml\|0802002.xml\|1842565.xml\|0744041.xml\|0962566.xml\|0986083.xml\|0583518.xml\|1217866.xml\|0911245.xml\|1685643.xml\|0781410.xml\|0811425.xml\|1062733.xml\|1673799.xml\|1209061.xml\|1523176.xml\|1166306.xml\|0605062.xml\|0721628.xml\|1228697.xml\|1851198.xml\|0881394.xml\|0613887.xml\|1701137.xml\|1092138.xml\|1142185.xml\|1516147.xml\|1162280.xml\|1652965.xml\|1670224.xml\|1691487.xml\|0651769.xml\|0828959.xml\|1002332.xml\|1709163.xml\|0595910.xml\|0662157.xml\|0854788.xml\|1068069.xml\|1640220.xml\|1709960.xml\|0883129.xml\|0967653.xml\|1813870.xml\|1191055.xml\|1671965.xml\|1675519.xml\|1489994.xml\|1720460.xml\|0956341.xml\|1241596.xml\|1813662.xml\|1833965.xml\|1774812.xml\|0656849.xml\|0833642.xml\|1286501.xml\|0926865.xml\|1290929.xml\|1668449.xml\|1216332.xml\|0853616.xml\|1753584.xml\|1661668.xml\|1491951.xml\|1689068.xml\|1826485.xml\|1247477.xml\|1309590.xml\|0648989.xml\|1067349.xml\|1071920.xml\|1640033.xml\|1473452.xml\|0582433.xml\|0709870.xml\|0792560.xml\|0883433.xml\|0691897.xml\|0713774.xml\|1332709.xml\|0941543.xml\|1778467.xml\|1175753.xml\|1678471.xml\|1347959.xml\|1460930.xml\|1234956.xml\|1849688.xml\|1645133.xml\|0808314.xml\|0915118.xml\|1239298.xml\|1025311.xml\|1520964.xml\|1344421.xml\|1517246.xml\|0762898.xml\|0629448.xml\|1680761.xml\|1835993.xml\|0829677.xml\|1691864.xml\|0905849.xml\|1170014.xml\|1704381.xml\|0610245.xml\|1540936.xml\|1824796.xml\|0991938.xml\|1467625.xml\|1643084.xml\|0602890.xml\|0975995.xml\|1812630.xml\|1018679.xml\|1525009.xml\|1302905.xml\|0728203.xml\|1641776.xml\|0718175.xml\|0626246.xml\|1838250.xml\|0663914.xml\|1170153.xml\|0583327.xml\|1333226.xml\|1168391.xml\|1267993.xml\|1789197.xml\|0819250.xml\|0870830.xml\|1677246.xml\|1453224.xml\|1724868.xml\|1332414.xml\|1209953.xml'` ../NYTTestFixed
		
		File prefix = new File("/local/pjbarrio/Files/Downloads/NYTTest/NYTTestFixed");
		
		File toSave = new File("/local/pjbarrio/Files/Downloads/NYTTest/NYTTestFixed");
		
		
//		File prefix = new File("/local/pjbarrio/Files/Downloads/NYTValidationSplitPlain/");
//		
//		File toSave = new File("/local/pjbarrio/Files/Downloads/NYTValidationSplitPlainExtraction/");
		
		
		//Once I have identified those that are broken, I run:
		// mkdir ../NYTValidationSplitPlainFixed
		// cp `find | grep '1632168.xml\|0022020.xml\|0144283.xml\|1624710.xml\|1606304.xml\|0073003.xml\|0463891.xml\|0549160.xml\|0293880.xml\|1594195.xml\|0367814.xml\|0020878.xml\|0277805.xml\|0306820.xml\|1635289.xml\|0539079.xml\|1626514.xml\|0469402.xml\|0432393.xml\|1621023.xml\|0227092.xml\|0550778.xml\|0391627.xml\|0241471.xml\|0324856.xml\|0572476.xml\|0100274.xml\|0029570.xml\|1630014.xml\|0078786.xml\|0379103.xml\|0358201.xml\|0269225.xml\|1617582.xml\|1619262.xml\|1589247.xml\|0384405.xml\|1633503.xml\|0090784.xml\|1631645.xml\|0398270.xml\|1548966.xml\|0295834.xml\|0004688.xml\|0157569.xml\|0437967.xml\|0358097.xml\|0413097.xml\|0326732.xml\|0465408.xml\|1623046.xml\|1625807.xml\|0561096.xml\|1548287.xml\|0185691.xml\|0555970.xml\|0195904.xml\|0174835.xml\|1628295.xml\|0165319.xml\|0340065.xml\|1592660.xml\|0484372.xml\|0255427.xml\|1636982.xml\|0100910.xml\|0002183.xml\|0482432.xml\|0326022.xml\|1599756.xml\|0295059.xml\|0274929.xml\|0303821.xml\|1638463.xml\|0574583.xml\|0236752.xml\|0118934.xml\|0531084.xml\|1583369.xml\|0090472.xml\|0185824.xml\|0425587.xml\|0554728.xml\|1600174.xml\|0523940.xml\|0574920.xml\|0469938.xml\|0032113.xml\|0334628.xml\|0076010.xml\|0307939.xml\|0240201.xml\|1626515.xml\|0107949.xml\|0041924.xml\|0254002.xml\|0332203.xml\|0299012.xml\|0487664.xml\|0412632.xml\|0534953.xml'` ../NYTValidationSplitPlainFixed
		
//		File prefix = new File("/local/pjbarrio/Files/Downloads/NYTValidationSplitPlainFixed2");
//		
//		File toSave = new File("/local/pjbarrio/Files/Downloads/NYTValidationSplitPlainFixed");

		
//		File prefix = new File("/local/pjbarrio/Files/Downloads/NYTTrain/");
//		
//		File toSave = new File("/local/pjbarrio/Files/Downloads/NYTTrainExtraction/");
//----------
//		Once I have identified those that are broken, I run:
//		mkdir ../NYTTrainFix/
//		cp `find | grep '1359655.xml\|1363112.xml\|1364000.xml\|1366871.xml\|1373481.xml\|1378255.xml\|1382275.xml\|1389545.xml\|1391320.xml\|1403313.xml\|1420613.xml\|1434692.xml\|1444656.xml\|1446670.xml\|1449729.xml'` ../NYTTrainFix/
//		
//		File prefix = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/NYTTrainFix");
//		
//		File toSave = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/NYTTrainFix");
		
		OpenCalaisRelationExtractor oCRE = new OpenCalaisRelationExtractor("/proj/dbNoBackup/pjbarrio/Exp/src/extraction/calaisParams.xml");
		
		process(oCRE,prefix,toSave);
	
	}

	private static void process(OpenCalaisRelationExtractor oCRE, File toExtractFrom, File toSaveIn) {
		
		
		File[] files = toExtractFrom.listFiles();
		
		for (int i = 0; i < files.length; i++) {

			File f = new File(toSaveIn,files[i].getName() + OpenCalaisRelationExtractor.SUFFIX);
			if (!f.exists())
				oCRE.ConcurrentProcess(files[i], f);
			else
				System.out.println("exists...");
			
		}
		
	}

	
	
}
