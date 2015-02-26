package peoplecomparison;

import twitter4j.*;

import java.util.*;

/**
 * Person object - stores all twitter and other personal info
 *
 * Created by Mark Clift on 19/02/15.
 */
public class Person {

    private String name;
    private String twitterFeed;
    private String linkedInInfo;

    public Person(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String getTwitterFeed() {
        return twitterFeed;
    }

    public void setTwitterFeed(List<Status> statuses) {
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for(Status status : statuses) {
            String statusText = TwitterUtil.cleanTweet(status.getText());
            out.format("%s", statusText + " ");
        }
        this.twitterFeed = out.toString();
    }

    public void setTwitterFeed(String feed) {
        this.twitterFeed = feed;
    }

    public String getLinkedInInfo() {
        return linkedInInfo;
    }

    public void setLinkedInInfo(String li) {
        this.linkedInInfo = li;
    }
}