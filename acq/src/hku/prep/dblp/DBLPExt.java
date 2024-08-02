package hku.prep.dblp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * @author fangyixiang
 * @date Jul 19, 2015
 * Create a new file, by filtering information that is not about article, author and titles
 */
public class DBLPExt {

	public static void main(String[] args) {
		try{
			String inPath = "/home/fangyixiang/Desktop/CCS/dblp/dblp.xml";
			String outPath = "/home/fangyixiang/Desktop/CCS/dblp/dblp-ext.xml";
			BufferedReader stdin = new BufferedReader(new FileReader(inPath));
			BufferedWriter stdout = new BufferedWriter(new FileWriter(outPath));
			
			stdout.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");stdout.newLine();
			stdout.write("<!DOCTYPE dblp SYSTEM \"dblp.dtd\">");			stdout.newLine();
			stdout.write("<dblp>");											stdout.newLine();
			
			String line = null;
			int count = 0;
			while((line =  stdin.readLine()) != null){
				if(line.contains("<article")|| line.contains("<inproceedings") 
					|| line.contains("<book") || line.contains("<proceedings") 
					|| line.contains("<incollection") || line.contains("<www")
					|| line.contains("<phdthesis") || line.contains("<mastersthesis")
					|| line.contains("<author") || line.contains("<title")
					
					|| line.contains("</article>")|| line.contains("</inproceedings") 
					|| line.contains("</book") || line.contains("</proceedings") 
					|| line.contains("</incollection") || line.contains("</www")
					|| line.contains("</phdthesis") || line.contains("</mastersthesis")
					|| line.contains("</author") || line.contains("</title")){
					
//					System.out.println(line);
					stdout.write(line);
					stdout.newLine();
				}
			}
			stdin.close();
			
			stdout.write("");		stdout.newLine();
			stdout.write("</dblp>");stdout.newLine();
			stdout.flush();
			stdout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
