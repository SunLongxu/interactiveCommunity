#!/bin/bash
# Define dataset name
Dataset=("amazon")
# Define the full path to the file to check
INDEXFILENAME="../index/${Dataset}/${Dataset}_wcs_index.txt"
IDFILENAME="../data/${Dataset}/${Dataset}_cmty_id.txt"
QUERYFILENAME="../data/${Dataset}/${Dataset}_wcs_queries.txt"

# Define variables for index construction
IndexName=("newIndexGeneration")
IdName=("cmtyId")
QueryName=("randomQuery5k")
SearchName=("kCoreConnectedMinWeight")

function executeIndex() {
  javac ${IndexName}/${IndexName}.java
  if [ $? -eq 0 ]; then
    echo "Index construction compilation successful, executing the index..."
    echo ${Dataset} | java ${IndexName}.${IndexName}
  else
    echo "Index construction compilation failed, please check your code for errors."
    exit 1
  fi
}

function executeCmtyId() {
  javac ${IdName}/${IdName}.java
  if [ $? -eq 0 ]; then
    echo "Community Id generation compilation successful, executing the program..."
    echo ${Dataset} | java ${IdName}.${IdName}
  else
    echo "Community Id generation compilation failed, please check your code for errors."
    exit 1
  fi
}

function executeQuery() {
  javac ${QueryName}/${QueryName}.java
  if [ $? -eq 0 ]; then
    echo "Query generation compilation successful, executing the program..."
    echo ${Dataset} | java ${QueryName}.${QueryName}
  else
    echo "Query generation compilation failed, please check your code for errors."
    exit 1
  fi
}

function executeSearch() {
  # Navigate to the project directory
  cd baseline

  # Compile the Java code
  javac ${SearchName}/${SearchName}.java

  # Check if the compilation was successful
  if [ $? -eq 0 ]; then
    echo "Search compilation successful, running the baseline program on wcs model..."
    echo ${Dataset} | java ${SearchName}.${SearchName}
  else
    echo "Compilation failed, please check your code for errors."
    exit 1
  fi
}

# Check if the file exists
if [ ! -f "$INDEXFILENAME" ]; then
  # If the file does not exist, execute the first script
  executeIndex
else
  echo "File '$INDEXFILENAME' exists, skipping the index construction."
fi

if [ ! -f "$IDFILENAME" ]; then
  # If the file does not exist, execute the second script
  executeCmtyId
else
  echo "File '$IDFILENAME' exists, skipping generation of community id node."
fi

if [ ! -f "$QUERYFILENAME" ]; then
  # If the file does not exist, execute the third script
  executeQuery
else
  echo "File '$QUERYFILENAME' exists, skipping the query generation."
fi

executeSearch
