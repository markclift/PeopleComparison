package peoplecomparison;

import java.io.*;
import java.util.*;

/**
 * Created by Mark Clift on 20/02/15.
 */
public class Cache {

    // Our list of names we already have info for
    private ArrayList<String> cachedNames;
    // Temporary argument to specify whether we want to override cache (just set to NO for the time being)
    private boolean overrideCache = false;
    // How long do we want the cache to be valid for? (in hours)
    private int cacheDuration = 240;
    // The directory to cache
    private String directory;
    // A customer filter to search for all "_Tweets.txt" files in a directory
    private AgeFilter ageFilter;

    public Cache(String directory){
        this.directory = directory;
        ageFilter = new AgeFilter(cacheDuration);
        buildCache();
    }

    public boolean isCached(String name) {
        if (!overrideCache && cachedNames.contains(name)) return true;
        else return false;
    }

    public void addTwitterFeed(Person person) {
        try {
            // Check directory exists and add it if not
            File dir = new File(this.directory);
            if (!dir.exists()) dir.mkdir();
            // Create tweets file in the directory
            FileWriter writer = new FileWriter(this.directory + person.getName() + "_Tweets.txt");
            writer.write(person.getTwitterFeed());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTwitterFeed(Person person) {
        String twitterFeed = null;
        try {
            twitterFeed = new Scanner(new FileReader(this.directory + person.getName() + "_Tweets.txt")).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't read from cache");
            e.printStackTrace();
            System.exit(-1);
        }
        return twitterFeed;
    }

    public void rebuildCache(int newDuration){
        this.cacheDuration = newDuration;
        ageFilter.setAge(newDuration);
        buildCache();
    }

    public void rebuildCache() {
        buildCache();
    }

    public void setCacheDuration(int newDuration) {
        this.cacheDuration = newDuration;
    }

    private void buildCache(){
        cachedNames = new ArrayList<String>();
        File dir = new File(directory);
        if (dir.exists()) {
            File[] fileArr = dir.listFiles(ageFilter);
            for (File file : fileArr) {
                String fileName = file.getName();
                cachedNames.add(fileName.substring(0, fileName.length() - 11)); // Remove the "_Tweets.txt"
            }
        }
    }
}
