#!/bin/bash
# Define dataset name
Dataset=("amazon")
# Define the full path to the file to check
files=(
  "../index/${Dataset}/${Dataset}-ground.txt"
  "../data/${Dataset}/${Dataset}-graph.txt"
  "../data/${Dataset}/${Dataset}-mapper.txt"
  "../data/${Dataset}/${Dataset}-node.txt"
)
INDEXFILENAME="../index/${Dataset}/${Dataset}_wcs_index.txt"
IndexName=("newIndexGeneration")
QUERYFILENAME="../data/${Dataset}/${Dataset}-query.txt"

function executeFormatGraph() {
  # Navigate to the project directory
  cd src

  # Compile all Java files in the project
  javac hku/exp/util/transFormat.java
  
  if [ $? -eq 0 ]; then
    echo "Graph format transformation compilation successful, executing the program..."
    echo ${Dataset} | java hku.exp.util.transFormat
  else
    echo "Graph format transformation compilation failed, please check your code for errors."
    exit 1
  fi

  cd ./..
}

function executeFormatGraph() {
  # Navigate to the project directory
  cd src

  # Compile all Java files in the project
  javac hku/exp/util/transFormat.java
  
  if [ $? -eq 0 ]; then
    echo "Graph format transformation compilation successful, executing the program..."
    echo ${Dataset} | java hku.exp.util.transFormat
  else
    echo "Graph format transformation compilation failed, please check your code for errors."
    exit 1
  fi

  cd ./..
}

function executeIndex() {
  cd ./../WCS/
  javac ${IndexName}/${IndexName}.java
  if [ $? -eq 0 ]; then
    echo "Index construction compilation successful, executing the index..."
    echo ${Dataset} | java ${IndexName}.${IndexName}
  else
    echo "Index construction compilation failed, please check your code for errors."
    exit 1
  fi
  cd ./../ACQ/
}

function executeQuery() {
  # Navigate to the project directory
  cd src

  # Compile all Java files in the project
  javac hku/exp/util/GroundtruthReader.java
  
  if [ $? -eq 0 ]; then
    echo "Query generation compilation successful, executing the program..."
    echo ${Dataset} | java hku.exp.util.GroundtruthReader
  else
    echo "Query generation transformation compilation failed, please check your code for errors."
    exit 1
  fi
  
  cd ./..
}

function executeSearch() {
  # Navigate to the project directory
  cd src

  # Compile all Java files in the project
  javac hku/exp/gicsExp.java

  # Check if the compilation was successful
  if [ $? -eq 0 ]; then
    echo "Compilation successful, running the gics program on acq model..."
    java hku.exp.gicsExp
  else
    echo "Compilation failed, please check your code for errors."
    exit 1
  fi
  
  cd ./..
}

# Function to execute the script or command if any file is missing
function execute_script_if_missing() {
  echo "Running the script to check whether there is a graph file missing..."
}

# Flag to track if any file is missing
missing_file=false

# Check each file in the list
for file in "${files[@]}"; do
  if [ ! -f "$file" ]; then
    echo "File '$file' does not exist."
    missing_file=true
  fi
done

# If any file is missing, run the script
if [ "$missing_file" = true ]; then
  executeFormatGraph
else
  echo "All files exist. No need to run the script."
fi

# Check if the file exists
if [ ! -f "$INDEXFILENAME" ]; then
  # If the file does not exist, execute the first script
  executeIndex
else
  echo "File '$INDEXFILENAME' exists, skipping the index construction."
fi

if [ ! -f "$QUERYFILENAME" ]; then
  # If the file does not exist, execute the third script
  executeQuery
else
  echo "File '$QUERYFILENAME' exists, skipping the query generation."
fi

executeSearch
