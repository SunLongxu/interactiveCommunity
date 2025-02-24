## GICS-ICSGNN

### Description

This folder contains the code of the GICS framework and baseline method on the ICSGNN model, and the user study version.

### Dependencies
Before use the code, make sure you have installed
- python >= 3.8
- pytorch >= 1.5.0
- [torch-geometric](https://pytorch-geometric.readthedocs.io/en/latest/notes/installation.html) >= 1.6.1 
- texttable >=1.6.3
- networkx >=2.4

### How to run the code
To test the main code, execute the `run_gics.sh` file or `run_baseline.sh` file with the following command:

```
sh run_gics.sh
sh run_baseline.sh
```

To test the user study code, execute the `run_userstudy_gics.sh` file or `run_userstudy_baseline.sh` file with the following command:

```
sh run_userstudy_gics.sh
sh run_userstudy_baseline.sh
```

### Dataset preparation
If you collect the network datasets with ground-truth from [SNAP](https://snap.stanford.edu/data/index.html#communities) dataset website, just put the .gz files into **/data/raw/** folder and run the program, it will unzip these data and transfer into .npy data by itself. Otherwize, you need to prepare a *graph* file and a *ground-truth community* file. 

The data format of graph file is "FromNodeId	ToNodeId" splited by a tab. 

The data format of ground-truth community file is "NodeId1	NodeId2	NodeId3..." splited by a tab. 

### Query preparation
The code will randomly  generate query vertices by itself during the execution. There is no need to generate query vertices separately. 

### Results
The results will be saved in the **/icsgnn/data/** folder once the program has been completed.