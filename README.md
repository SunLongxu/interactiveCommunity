# Interactive Community Search Framework (GICS)

This repository contains the code, datasets, and sample queries of the paper **"A Flexible Framework for Query-oriented Interactive Community Search"**, which is submitted to PVLDB'25. 

### Introduction

![Interactive Community Search Framework](./framework.jpg)

The GICS framework contains three key components: *personalized adding/deleting recommendation*, *parameter auto-tuning*, and *fast partial refinement*. To evaluate the performance of the proposed framework, we implement the GICS framework on three representative community search models, including [:link:](https://www.vldb.org/pvldb/vol9/p1233-fang.pdf)*<span style="color:green">ACQ</span>*, [:link:](https://ieeexplore.ieee.org/document/9272333)*<span style="color:green">WCS</span>* and [:link:](https://www.vldb.org/pvldb/vol14/p1006-gao.pdf)*<span style="color:green">ICSGNN</span>*. 

### Project structure description

This project is organized into five key directories, where *<span style="color:green">ACQ</span>*, *<span style="color:green">ICSGNN</span>*, and *<span style="color:green">WCS</span>* contains code, scripts and detailed instructions on three models separately, *<span style="color:blue">data</span>* and *<span style="color:blue">index</span>* containing the sample dataset and generated index. Below is an overview of the main components:

```
├── ACQ
│   ├── README.md - Document including a short description, dependencies, instructions, and input/output data format.
│   ├── run_baseline.sh - Execution scripts for baseline methods
│   ├── run_gics.sh - Execution scripts for GICS methods
│   └── src - Source code 
├── ICSGNN
│   ├── README.md - Document including a short description, dependencies, instructions, and input/output data format.
│   ├── data - Input datasets using NumPy format
│   ├── log - Output data
│   ├── main.py - Source code for entry point
│   ├── run_baseline.sh - Execution scripts for baseline methods
│   ├── run_gics.sh - Execution scripts for GICS methods
│   ├── run_userstudy_baseline.sh - Execution user study scripts for baseline methods
│   ├── run_userstudy_gics.sh - Execution user study scripts for GICS methods
│   └── src - Source code
├── WCS
│   ├── README.md - Document including a short description, dependencies, instructions, and input/output data format.
│   ├── baseline - Source code for baseline methods
│   ├── gics - Source code for GICS methods
│   ├── newIndexGeneration - Source code for index generation
│   ├── cmtyId - Source code for query generation
│   ├── randomQuery5k - Source code for query generation
│   ├── run_baseline.sh - Execution scripts for baseline methods
│   └── run_gics.sh - Execution scripts for GICS methods
├── data
│   ├── README.md - Description for datasets and data source
│   ├── amazon - Amazon datasets and sample queries
│   └── raw - Unprocessed data sources
├── index - Generated index for fast querying/retrieval
├── framework.jpg - System architecture diagram
├── README.md - Project documentation
└── LICENSE
```

### Reproducibility 

- **Overall evaluation:** To reproduce Table 3 in the paper, run both the baseline and GICS Shell scripts (run_baseline.sh/run_gics.sh) in each of the model folders. 

- **A running example of user studies:** Dedicated scripts (run_userstudy_baseline.sh/run_userstudy_gics.sh) in ICSGNN for human-in-the-loop evaluations.

- **Method comparison:** Supports baseline vs. GICS framework in all models.

- **Support for new datasets:** The README.md file in each model folder illustrates the input data format, and query, index, and supplementary file generation methods.

## Dependencies

This project requires the following software to be installed:

- **Java**: Version 1.8.0_381 (Java SE Runtime Environment)
- **Python**: Version 3.9.12

Make sure to have the correct versions installed to ensure compatibility with the project. The detailed configuration of the environment is in the README.md files in separate model folders. The python packages and versions are listed in ICSGNN/requirements.txt.  
  
### Contacts

Feel free to contact cslxsun@comp.hkbu.edu.hk if you have any questions on reproducing the program.