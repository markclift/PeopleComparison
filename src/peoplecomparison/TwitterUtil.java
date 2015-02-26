package peoplecomparison;

import twitter4j.*;
import twitter4j.auth.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Utility class, can't be instantiated.
 * Sets up Twitter authorisation and polls the API for tweets of a given username.
 * Also provides a method to clean the tweets (removing URLs etc).
 *
 * Created by Mark Clift on 19/02/15.
 */
public final class TwitterUtil {

    private static final String twitterPropFile = "twitter.properties";
    private static Twitter twitter;
    private static Properties prop;

    private TwitterUtil() {
    }

    /**
     * Takes a Twitter username and returns a list of Mallet Statuses
     * Also creates a text file on the hard drive with all the tweets (called "username_Tweets.txt)
     * TODO: Move this method to non-utility class and setup for concurrency?
     */
    public static List<Status> getTweets(String screenName) throws TwitterException {
        int pagenum = 1;
        List<Status> statuses = new ArrayList();
        if (twitter == null) setupTwitter();
        int maxPages = Integer.parseInt(prop.getProperty("maxPages"));

        while (pagenum<maxPages) {
            int size = statuses.size();
            Paging page = new Paging(pagenum++, 100);
            statuses.addAll(twitter.getUserTimeline(screenName, page));
            if (statuses.size() == size) break;
        }
        System.out.println(statuses.size() + " of @" + screenName + "'s statuses retrieved");

        return statuses;
    }

    /* Sets up the authorisation of the API */
    public static void setupTwitter() {
        if (twitter == null) {
            prop = new Properties();
            InputStream inputStream = TwitterUtil.class.getResourceAsStream(twitterPropFile);
            if (inputStream != null) {
                try {
                    prop.load(inputStream);
                } catch (IOException e) {
                    System.out.println("Property file '" + twitterPropFile + "' not found in the classpath");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            String CONSUMER_KEY = prop.getProperty("CONSUMER_KEY");
            String CONSUMER_SECRET = prop.getProperty("CONSUMER_SECRET");
            String ACCESS_TOKEN = prop.getProperty("ACCESS_TOKEN");
            String ACCESS_TOKEN_SECRET = prop.getProperty("ACCESS_TOKEN_SECRET");
            try {
                twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                AccessToken oathAccessToken = new AccessToken(ACCESS_TOKEN,ACCESS_TOKEN_SECRET);
                twitter.setOAuthAccessToken(oathAccessToken);
            } catch (Exception e) {
                System.out.println("Can't get OAuth2 token");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /* Removes URLs, users and "RT" from a tweet */
    public static String cleanTweet(String tweetText)
    {
        // Remove URLs
        while (tweetText.contains("http")) {
            int indexOfHttp = tweetText.indexOf("http");
            int endPoint = (tweetText.indexOf(" ", indexOfHttp) != -1) ? tweetText.indexOf(" ", indexOfHttp) : tweetText.length();
            String url = tweetText.substring(indexOfHttp, endPoint);
            tweetText = tweetText.replace(url,"");
        }

        // Remove Users
        String patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(tweetText);
        while (matcher.find()) {
            tweetText = tweetText.replaceAll(matcher.group(),"").trim();
        }

        // Remove "RT:"
        tweetText = tweetText.replaceAll("RT:","").trim();

        return tweetText;
    }
}