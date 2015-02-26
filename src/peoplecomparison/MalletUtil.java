package peoplecomparison;

import cc.mallet.pipe.*;
import cc.mallet.topics.*;
import cc.mallet.types.*;
import cc.mallet.util.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Utility class, cannot be instantiated.
 * Provides methods to build a pipe, run a topic model and find similarities between instances
 *
 * Some helpful Mallet online documentation here:
 * http://mallet.cs.umass.edu/import-devel.php
 * http://mallet.cs.umass.edu/topics-devel.php
 *
 * Created by Mark Clift on 19/02/15.
 */
public final class MalletUtil {

    private static Pipe pipe;

    private MalletUtil() {
    }

    public static Pipe getPipe() {
        if (pipe == null) buildPipe();
        return pipe;
    }

    /*
     * Take a list of instances and run LDA on them
     */
    public static ParallelTopicModel doTopicModel (InstanceList instances, double alpha, int iterations) {

        // Set the number of topics to be a minimum of 5 (for very low numbers of documents) and a maximum of 50 (for high numbers of documents)
        int numTopics;
        if (instances.size() < 5) numTopics = 5;
        else if (instances.size() < 10) numTopics = instances.size();
        else if (instances.size() < 500) numTopics = 10 + (int)((0.09*instances.size())+0.5); // Linearly smooth from 5 to 50 topics
        else numTopics = 50;

        System.out.println("Using " + numTopics + " topics");

        // Create a model with X topics, alpha_t = 0.2, beta_w = 0.01
        // Note that the first parameter is passed as the sum over topics, while
        // the second is the parameter for a single dimension of the Dirichlet prior.
        ParallelTopicModel model = new ParallelTopicModel(numTopics, alpha, 0.01);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine statistics after every iteration.
        model.setNumThreads(2);

        model.setNumIterations(iterations);
        try {
            model.estimate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // From now on we're just printing interesting information to the console

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions
        System.out.println("Topic\tTop 5 Words with weightings");
        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t\t", topic);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            System.out.println(out);
        }

        // Print out topic matrix by document, starting with a header
        System.out.println("\nListing topic weights by document");
        StringBuilder sb = new StringBuilder();
        for (int topic = 0; topic < numTopics; topic++) {
            sb.append("Topic" + topic + "\t");
        }
        System.out.printf("%-15s %s%n", "", sb);

        for (int inst = 0; inst < instances.size(); inst++) {
            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            String name = (String)instances.get(inst).getName();
            double[] topicDist = model.getTopicProbabilities(inst);
            for (int topic = 0; topic < numTopics; topic++) {
                out.format("%.3f\t", topicDist[topic]);
            }
            System.out.printf("%-15s %s%n", name, out);
        }

        return model;

    }

    /*
     * Take a model where LDA has been run and figure out the similarities
     * between the documents in it, based on their topic distributions
     */
    public static double[][] findSimilarities(ParallelTopicModel model) {

        int totInstances = model.getData().size();

        // Our final matrix of all similarities
        double[][] sims = new double[totInstances][totInstances];

        System.out.println("\nFinding document similarities");
        // First print matrix header
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int instID = 1; instID < totInstances; instID++) {
            out.format("%-15s", model.getData().get(instID).instance.getName());
        }
        System.out.printf("%-15s %s%n", "", out);
        for (int instID1 = 0; instID1 < totInstances-1; instID1++) {
            out = new Formatter(new StringBuilder(), Locale.US);
            for (int tabs = 0; tabs < instID1; tabs++) out.format("%-15s", "");
            double[] topicDist1 = model.getTopicProbabilities(instID1);
            for (int instID2 = instID1+1; instID2 < totInstances; instID2++) {
                double[] topicDist2 = model.getTopicProbabilities(instID2);
                sims[instID1][instID2] = Maths.klDivergence(topicDist1, topicDist2);
                out.format("%-15s", String.format("%.3f", sims[instID1][instID2] ));
            }
            System.out.printf("%-15s %s%n", model.getData().get(instID1).instance.getName(), out);
        }

        return sims;
    }

    private static void buildPipe() {
        ArrayList pipeList = new ArrayList();

        // If reading from files
        // pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers,
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        //Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");\b[A-Za-z]{3}[A-Za-z]*\b
        Pattern tokenPattern = Pattern.compile("\\b[A-Za-z]{3}[A-Za-z]*\\b");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field:
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        //pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        // THERE'S A BUG WITH THIS - IGNORE FOR NOW
        //pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
        //pipeList.add(new PrintInputAndTarget());

        pipe = new SerialPipes(pipeList);
    }

}