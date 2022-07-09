# GitLogTest

### Introduction

This project is a software for the application of Machine Learning to Software Engineering created for the Software Engineering 2 exam at the University of Rome Tor Vergata.
The software allows, starting from two projects available on GitHub, to build a dataset containing the performances of classifiers such as NaiveBayes, RandomForest and Ibk using Weka API.


N.B. the software calculates the performance of the classifiers by applying feature selection with backward search and undersampling, and evaluates them as the following techniques vary for Cost sensitivity:
No cost sensitivity;
Sensitive learning;
Sensitive threshold.


### How to use it
To use this software, clone the repository locally, access the configuration file present in src / main / resources and called configuration.csv.
Edit the file with your local configurations following the order shown in the header.
The csv file can contain only one line (excluding the header), a multi-configuration is not allowed.
The project has only been tested on Linux devices.
