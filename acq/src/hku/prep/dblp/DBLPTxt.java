package hku.prep.dblp;

import hku.prep.PrepUser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author yxfang
 *
 * @date 2015-7-19
 */
public class DBLPTxt {
//	private String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp.xml";
	private String path = "/home/fangyixiang/Desktop/CCS/dblp/dblp-ext.xml";

	//step 1: generate a txt file
	public void createTxt(){
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();  
		try {
			System.setProperty("entityExpansionLimit", "6400000");
			DocumentBuilder db = dbf.newDocumentBuilder();//
			Document doc = db.parse(new File(path));
			Element root = doc.getDocumentElement();//root�ڵ�
			NodeList nodeList = root.getChildNodes();
			
			System.out.println("nodeList.getLength():" + nodeList.getLength());
			
			String outPath = "/home/fangyixiang/Desktop/CCS/dblp/dblp-txt.txt";
			BufferedWriter stdout = new BufferedWriter(new FileWriter(outPath));
			
			for(int i = 0;i < nodeList.getLength();i ++){
				Node node = nodeList.item(i);
				String nodeName = node.getNodeName();
				
				//2015-7-19, we only consider papers. We may consider inproceedings, phd/master thesis, book
				if(nodeName.contains("article")){
//				if(nodeName.contains("inproceedings")){
					NodeList list = node.getChildNodes();
					
					String title = "";
					List<String> authorList = new ArrayList<String>();
					for(int j = 0;j < list.getLength();j ++){
						Node nd = list.item(j);
						if(nd.getNodeName().contains("title")){
							title = nd.getTextContent().trim();
						}else if(nd.getNodeName().contains("author")){
							authorList.add(nd.getTextContent().trim());
						}
					}
					
					//output the title and its authors
					if(authorList.size() > 0){//We skip papers without authors
						stdout.write(title);
						stdout.newLine();
						stdout.write(authorList.get(0));
						for(int j = 1;j < authorList.size();j ++){
							stdout.write("\t" + authorList.get(j));
						}
						stdout.newLine();
					}
				}
			}
			stdout.flush();
			stdout.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) {
		DBLPTxt txt = new DBLPTxt();
		txt.createTxt();
	}

}
