# needminer

A machine learning tool for the classification of Tweets on whether they include customer need data.

The components are implemented using tools that are publicly available and free of charge.
The implementation is designed to be used by researchers with limited programming skills and can be extended with little effort to become a simple to use graphical user interface for daily usage.

It consists of the following modules:
* Data retrieval from three different Tweet data sources.
* Data Storage for acquired Tweets, meta data and machine learning features .
* Export adapters for Tweet content to enable external descriptive coding.
* Data enhancement modules allow for efficient feature detection to enrich Tweets with e.g. language information.
* Account tagging mechanism to flag user accounts as institutional.
* Filter modules to exclude subsets of data based on boolean criteria.
* Tweet tagging tool enabling participants in lab sessions to tag a Tweet regarding customer need presence for the following labeling of the Tweet as ”need” or ”no need”.
* Statistical analysis modules to get insights about raw and processed Tweet data. Preprocessing modules to modify Tweet content on the fly to optimize classification accuracy.
* Classification algorithms to train classification models to predict the class (”need” or ”no need”) of unseen Tweets.
* Experiments to benchmark several different preprocessing and algorithm combinations.


--------

Developed at KSRI - Karlsruhe Service Research Institute, located at KIT - Karlsruhe Institute of Technology.
