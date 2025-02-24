package randomQuery5k;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class randomQuery5k {

    public static ArrayList<String> readFile(String FilePath){

        ArrayList<String> lines = new ArrayList<String>();
        int count = 0;

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(FilePath), "UTF-8");

            BufferedReader br = new BufferedReader(isr);
            
            try {

                String line = br.readLine();
                
                while (line != null) {

                    count++;
                    
                    lines.add(line);

                    if (count % 10000000 == 0) {
                        System.out.println(count);
                        
                    }

                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException ex)  
        {
            System.out.println("FileNotFoundException");
            // insert code to run when exception occurs
        } catch (Exception e) {
            System.out.println("Reading **.txt as bufferedReaderhappens");
            System.out.println(e);
        }
        System.out.println(count);

        return lines;
    }

    public static void main(String[] args) {

        Scanner myObj = new Scanner(System.in);  // Create a Scanner object

        String ds = myObj.nextLine();  // Read user input

        // hm: coreness of vertex; count: vertex number
        HashMap<Integer, Integer> core_index = new HashMap<Integer, Integer>();
        int max_cor = 0;

        // begin read in core index
        String FilePath = "../index/"+ds+"/"+ds+"_wcs_index.txt";
        ArrayList<String> lines = new ArrayList<String>(readFile(FilePath));
        System.out.println("read core index file done;");

        int lines_siz = lines.size();
        // String[] line = new String[2];
        for(int j = 0; j < lines_siz; j++){
            
            String[] line = lines.get(j).split(",");
            if (Integer.parseInt(line[0]) > max_cor) {
                max_cor = Integer.parseInt(line[0]);
            }
            core_index.put(Integer.parseInt(line[1]), Integer.parseInt(line[0]));

        }

        // cmty_group: arraylist used to store communities; 
        // begin read in communities line by line
        String CmtyFilePath = "../data/"+ds+"/"+ds+"_cmty_5000.txt";
        ArrayList<String> cmty_group = new ArrayList<String>(readFile(CmtyFilePath));
        System.out.println("read cmty file done;");
        


        // begin read in communities line by line
        String CmtyIDFilePath = "../data/"+ds+"/"+ds+"_cmty_id.txt";
        ArrayList<String> cmtyid_group = new ArrayList<String>(readFile(CmtyIDFilePath));
        System.out.println("read cmty id file done;");
        
        int id_siz = cmtyid_group.size();
        Integer[] cmty_q = new Integer[id_siz];
        ArrayList<ArrayList<String>> cmty_id = new ArrayList<ArrayList<String>>();
        
        String[] a = {};
        String[] b = {};
        for(int j = 0; j < id_siz; j++){
                    
            a = cmtyid_group.get(j).split("\\s+");
            cmty_q[j] = Integer.parseInt(a[0]);
            b = Arrays.copyOfRange(a, 1, a.length);
            ArrayList<String> idcmty = new ArrayList<String>(Arrays.asList(b));
            cmty_id.add(idcmty);

        }

        int defaultk = 2;
        int qnum = 8;
        // random generate different queries

            // for (int qnum = 0; qnum < 7; qnum++) {
            // for (int defaultk = 2; defaultk < 9; defaultk=defaultk+2) {
            
            ArrayList<String> arrli = new ArrayList<String>();
            // int query_num = (int) Math.pow(2, qnum);
            int query_num = qnum;
            
            // get true core and min core
       
            for (int i = 0; i < cmty_group.size(); i++) {

                // i: cmty id; min_cor: cmty vertex min coreness;
                int min_cor = max_cor;
                String[] cmty = cmty_group.get(i).split("\\s+");
                if (cmty.length > query_num) {

                    ArrayList<String> idset = new ArrayList<String>(cmty_id.get(i));
                    ArrayList<String> cp_cmty = new ArrayList<String>(Arrays.asList(cmty));
                    // siz is the largest number of query nodes
                    int siz = cmty.length;

                    // compute min_cor
                    for (int j = 0; j < siz; j++) {

                        String vtx_num = cmty[j];

                        int vtx = Integer.parseInt(vtx_num) + 1;
                        //System.out.println(vtx);
                        int cor = core_index.get(vtx);
                        //System.out.println(cor);
                        if (cor < min_cor) {
                            min_cor = cor;
                        }
                        if (min_cor == 1) {
                            break;
                        }
                    }
                    String res = i + "," + min_cor;

                    if (min_cor > 0 && min_cor < 11) {
                        
                    
                    int idnum = 0;
                    String query = "";
                    int new_min_core = max_cor;
                    

                    // shuffle and get queries
                    if (idset.size() > 1) {
                        idnum = 1;
                        Collections.shuffle(idset);
                        int query1 = Integer.parseInt(idset.get(0)) + 1;
                        if (new_min_core > core_index.get(query1)) {
                            new_min_core = core_index.get(query1);
                        }
                        // query = "," + query1;
                        query = Integer.toString(query1);
                        cp_cmty.remove(idset.get(0));
                    } 
                          
                    
                    Collections.shuffle(cp_cmty);

                    for (int y = 0; y < query_num - idnum; y++) {

                        int vtx_q = Integer.parseInt(cp_cmty.get(y)) + 1;
                        if(query == ""){
                           query = Integer.toString(vtx_q);
                        } else {
                        query = query + "," + vtx_q;
                        }
                        if (new_min_core > core_index.get(vtx_q)) {
                            new_min_core = core_index.get(vtx_q);
                        }
                    }
                    
                    if (new_min_core > defaultk -1 && new_min_core < 11) {
                        Random rand1 = new Random();
                        int queryCore = rand1.nextInt(new_min_core) + 1;
          
                        
                        arrli.add(res  + "," + queryCore  + "," + query);
                        // arrli.add(query);
                    }
                }
            }
            
            

            // write down quries
            try {

                BufferedWriter bw = new BufferedWriter(new FileWriter("../data/"+ds+"/"+ds+"_wcs_queries.txt"));
                // BufferedWriter bw = new BufferedWriter(new FileWriter("../data/"+ds+"/"+ds+"_k" + defaultk + ".txt"));
                // BufferedWriter bw = new BufferedWriter(new FileWriter("../data/lj/lj_queries_q" + qnum + ".txt"));
                // BufferedWriter bw = new BufferedWriter(new FileWriter("../data/"+ds+"/"+ds+"_q"+query_num+".txt"));
                // BufferedWriter bw = new BufferedWriter(new FileWriter("../data/"+ds+"/"+ds+"_q"+query_num+"_"+(int)Math.floor((double)(l+1)*perc*100.0)+"_dense.txt"));

                try {
                    Collections.shuffle(arrli);
                    int num = 100;
                    if (arrli.size() < num) {
                        num = arrli.size();
                    }
                    for (int index = 0; index < num; index++) {
                        bw.write(arrli.get(index));
                        bw.newLine();

                    }

                } finally {

                    bw.close();

                }
            } catch (Exception e) {
                System.out.println("Writing **.txt as bufferedReader break");
            }

        }
        }

}
