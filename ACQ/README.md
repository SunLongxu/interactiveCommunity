## GICS-ACQ

### Description

This folder contains the code of the GICS framework and baseline method on the ACQ model, graph format transformation file and query generation file.

### Dependencies
Before using the code, make sure you have installed **[JAVA](https://www.oracle.com/hk/java/technologies/javase/javase8u211-later-archive-downloads.html)**: Version 1.8.0_381 (Java SE Runtime Environment).

### How to run the code
To test the code, execute the `run_gics.sh` file or `run_baseline.sh` file with the following command:


```
sh run_gics.sh
sh run_baseline.sh
```

### Dataset preparation
Before running the code, make sure you have prepared (1) a graph file; (2) a node keywords file; and (3) a ground-truth community file. 

The data format of graph file is "FromNodeId    ToNodeId" (sample file: /data/amazon/amazon_graph). 

The data format of keyword file is "NodeId1 KeywordId1 KeywordId2 KeywordId3..." (sample file: /data/amazon/amazon_keywords). 

The data format of ground-truth community file is "NodeId1	NodeId2	NodeId3..." splited by a tab (sample file: /data/amazon/amazon_cmty_5000). 

### Index and query preparation
The code will construct index by itself during the execution. 

The code will transform the graph files into required format by itself during the execution if there does not exist one (sample file: /data/amazon/amazon-x). 

The code will randomly generate query vertices by itself during the execution if there does not exist one (sample file: /data/amazon/amazon-query). 

### Results
The results will be saved in the **/acq/src/info/** folder once the program has completed.