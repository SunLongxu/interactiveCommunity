package hku;

/**
 * @author fangyixiang
 * @date Jul 20, 2015
 */
public class Config {
	public static double kwFreq = 0.01;//consider all the words globally
	public static int topKw = 20;//consider the keywords of each user locally
	
	//stem file paths
	public static String stemFile = "./stemmer.lowercase.txt";
	public static String stopFile = "./stopword.txt";

	//data paths
	public static String dataFilePath = "../../data/";
	
	//query parameters
	public static int k = 6;//the degree constraint

	public static int kwLength = -1;
	
	//the # of queryId examples
	public static int qIdNum = 300;
	
	//save parameters
	public static int ccsSizeThreshold = 50;//community size
	
	//log path
	public static String logFilePath = "./info/log" + System.currentTimeMillis();
	
	
	//CODICIL parameter
	public static int clusterK = 2500;//the number of clusters
	
}
