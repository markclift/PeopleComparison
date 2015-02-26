package peoplecomparison;

import cc.mallet.pipe.*;
import cc.mallet.topics.*;
import cc.mallet.types.*;
import twitter4j.*;

import java.io.*;
import java.util.*;

/**
 * Our main class for scraping information about people, translating that into Topic Models and comparing them.
 *
 * Created by Mark Clift on 19/02/15.
 */
public class PeopleComparison {

    // Put your test Twitter handles in this list
    private static final List<String> screenNames = Arrays.asList("NYTimeskrugman", "thisisafakenameandwontwork", "zerohedge","katyperry","justinbieber","jtimberlake", "TheEllenShow", "Cristiano", "messi10stats", "BillGates", "FCBarcelona", "DalaiLama", "pmarca", "elonmusk", "bhorowitz", "ariannahuff", "marissamayer", "joshuatopolsky", "charlesarthur", "jeffweiner", "rupertmurdoch");
    // Where to cache the tweet txt files
    private static final String TWEET_DIRECTORY = "./././././tweets/";
    // What to export our final graph as
    private static final String EXPORT_FORMAT = "pdf";
    // Where to export our final graph
    private static final String EXPORT_PATH = "./././././twitter-graph.pdf";
    // The minimum threshold for relationships to display in the graph
    private static final double minWeightToDisplay = 0.3;
    // The alpha to use in Topic Modelling
    private static final double alpha = 0.6;
    // Run the model for 50 iterations and stop (this is for testing only, for real applications, use 1000 to 2000 iterations)
    private static final int iterations = 500;

    /**
     * @param args String[]
     */
    public static void main(String[] args) {

        // Instantiate people from our given list of names
        List<Person> people = new ArrayList<Person>();
        for (String screenName : screenNames) {
            people.add(new Person(screenName));
        }

        // If we don't already have cached information then go get it
        Cache cache = new Cache(TWEET_DIRECTORY);
        Iterator<Person> it = people.iterator();
        while (it.hasNext()) {
            Person person = it.next();
            try {
                if (!cache.isCached(person.getName())) {
                    // Read and store Twitter info
                    person.setTwitterFeed(TwitterUtil.getTweets(person.getName()));
                    cache.addTwitterFeed(person);

                    // TODO: Store other info, e.g. LinkedIn
                    // person.setLinkedInInfo(LinkedInUtil.getLinkedInInfo(person.getName()));
                } else {
                    person.setTwitterFeed(cache.getTwitterFeed(person));
                }
            }
            catch (TwitterException e) {
                it.remove();
                System.out.println("Skipping " + person.getName() + ", failed to get timeline from Twitter");
            }
        }

        // Do the LDA model
        Pipe pipe = MalletUtil.getPipe();
        InstanceList instances = new InstanceList(pipe);
        for (Person person : people) {
            if (person.getTwitterFeed() != null) instances.addThruPipe(new Instance(person.getTwitterFeed(), null, person.getName(), null));
        }
        ParallelTopicModel model = MalletUtil.doTopicModel(instances, alpha, iterations);

        // Use Mallet's KL-Divergence algorithm to find nearest neighbours (lower numbers are closer)
        double[][] similarities = MalletUtil.findSimilarities(model);

        // Build a graph showing what we've discovered
        System.out.println("\nBuilding graph using inverse of similarity:");
        GraphBuilder builder = new GraphBuilder();
        for (int i = 0; i < people.size(); i++) {
            builder.setSize(people.get(i).getName(), people.size());
            builder.setColor(people.get(i).getName(), 0, 0, 0.9f);
            for (int j = i + 1; j < people.size(); j++) {
                float weight = (float) ((float) 1/similarities[i][j]);
                // Only add high-weight relationships so we don't have loads of annoying irrelevant lines on the graph
                if (weight > minWeightToDisplay) builder.addUndirectedRelation(people.get(i).getName(), people.get(j).getName(), weight);
            }
        }
        try {
            builder.export(EXPORT_FORMAT,EXPORT_PATH);
        } catch (IOException e) {
            System.out.println("Failed to export graph");
            e.printStackTrace();
            System.exit(-1);
        }

    }

}