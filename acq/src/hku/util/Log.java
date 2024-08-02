package hku.util;

import hku.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import hku.algo.*;

/**
 * @author fangyixiang
 * @date Jul 31, 2015
 */
public class Log {
	private static String fileName = Config.logFilePath;
	
//	public static void log(String msg) {
//		//step 1: read
//		List<String> logList = new ArrayList<String>();
//		try{
//			File file = new File(fileName);
//			if(file.exists()){
//				BufferedReader stdin = new BufferedReader(new FileReader(fileName));
//				String line = null;
//				while((line = stdin.readLine()) != null){
//					logList.add(line);
//				}
//				stdin.close();
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		//step 2: write
//		try {
//			Date date = new Date();
//			msg = date.toLocaleString() + "\t" + msg;
//						
//			BufferedWriter stdout = new BufferedWriter(new FileWriter(fileName));
//			for(String log:logList){
//				stdout.write(log);
//				stdout.newLine();
//			}
//			stdout.write(msg);
//			stdout.newLine();
//			
//			stdout.flush();
//			stdout.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void log(String msg) {
		try {
			Date date = new Date();
			String time = date.toLocaleString();
			
			BufferedWriter stdout = new BufferedWriter(new FileWriter(fileName, true));
			// stdout.write(time);
			// stdout.write("\t");
			stdout.write(msg);
			stdout.newLine();
			
			stdout.flush();
			stdout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logTNode(TNode tnode){
		String msg = "curCoreNum:" + tnode.getCore() + "\n";
		for(int id:tnode.getNodeSet())   msg += " " + id;
		msg += "\n";
		log(msg);
	}
	
	public static void main(String args[]) {
		Log.log("I love you");
	}
}
