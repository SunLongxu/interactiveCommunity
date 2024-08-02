package hku.exp.util;
import java.io.*;
import java.util.*;
/**
 * @author fangyixiang
 * @date Oct 13, 2015
 * read the queryId
 */
public class QueryIdReader {
	
	private ArrayList<QueryObj> list = new ArrayList<>();
	
	public List<QueryObj> read(String fileName){
		try{
			BufferedReader stdin = new BufferedReader(new FileReader(fileName));
			String line = null;
			while((line = stdin.readLine()) != null){
				String[] s = line.split(" ");
				int qid = Integer.parseInt(s[2]);
				// int qCore = 10;
				// if (Integer.parseInt(s[0])<10) {
				// 	qCore = Integer.parseInt(s[0]);
				// }
				
				int qCore = Integer.parseInt(s[1]);
				int[] qCmty = new int[s.length-3];
				for (int i = 3; i < s.length; i++) {
					qCmty[i-3] = Integer.parseInt(s[i]);
				}
				QueryObj qC = new QueryObj(qid, qCore, qCmty);
				
				list.add(qC);
			}
			stdin.close();
		}catch(Exception e){e.printStackTrace();}
		return list;
	}
}
