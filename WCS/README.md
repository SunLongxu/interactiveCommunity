## GICS-WCS

### Description

This folder contains the code of the GICS framework and baseline method on the WCS model, index construction, and query generation.

The **gics** folder contains the GICS framework implementation applied to the WCS model.

The **baseline** folder contains random recommendations and reruns the original algorithms in each iteration for WCS model.

The **newIndexGeneration** folder contains index generation code for WCS model.

The **cmtyId** folder contains the code used to prepare a set of candidate query vertices from the ground-truth community. It does this by filtering out vertices that belong to overlapping communities. This approach is intended to prevent the scenario where all the query vertices for a community come exclusively from the overlapping community. 

The **randomQuery5k** folder includes query generation code for WCS model.

### Dependencies
Before use the code, make sure you have installed [JAVA](https://www.oracle.com/hk/java/technologies/downloads/).

### How to run the code
To test the code, execute the `run_gics.sh` file or `run_baseline.sh` file with the following command:


```
sh run_gics.sh
sh run_baseline.sh
```

### Dataset preparation
Before running the code, make sure you have prepared (1) a weighted graph file; (2) a ground-truth community file. 

The data format of graph file is "FromNodeId,ToNodeId,EdgeWeight" (sample file: /data/amazon/amazon_wcs_weight). 

The data format of ground-truth community file is "NodeId1	NodeId2	NodeId3..." splited by a tab (sample file: /data/amazon/amazon_cmty_5000). 

### Index and query preparation
The code will construct index by itself if there does not exist one (sample file: /index/amazon/amazon_wcs_index). 

The code will randomly generate query vertices by itself during the execution if there does not exist one (sample file: /data/amazon/amazon_wcs_queries). 

### Results
The results will be saved in the **/data/amazon** folder once the program has completed.