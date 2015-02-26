# Using NLP to compare twitter users

The goal is to run Topic Modelling on a given list of Twitter users and identify those users closest to each other.

Mallet is used for Topic Modelling, Twitter4J to access the Twitter api, and Gephi to produce a network graph.

Start in PeopleComparison.java. You will need to edit twitter.properties to add your api authentication codes. The list of twitter usernames to test is just in an ArrayList at the top of the class. If you don't change anything, it will run as is, as it will search the cached tweets in the tweets directory and have no need to poll the api.

An example output is included here - the twitter-graph.pdf file in the parent folder (which will be overwritten when run).

This is very rough at the moment...
