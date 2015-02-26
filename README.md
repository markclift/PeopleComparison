# Using NLP to compare twitter users

The goal is to run Topic Modelling on a given list of Twitter users and identify those users closest to each other.

Mallet is used for Topic Modelling, Twitter4J to access the Twitter api, and Gephi to produce a network graph.

Start in PeopleComparison.java. You will need to edit twitter.properties to add your api authentication codes. The list of twitter usernames to test is just in an ArrayList at the top of the class.

The output is a twitter-graph.pdf file saved in the parent folder.

This is very rough at the moment...
