package peoplecomparison;

/**
 * Created by Mark Clift on 20/02/15.
 */

import java.io.*;
import java.util.*;

/** A simple file filter */
class AgeFilter implements FileFilter {

    // Age of file in hours
    private int age = 0;

    public AgeFilter(int age) {
        super();
        this.age = age;
    }

    public void setAge(int newAge) {
        this.age = newAge;
    }

    /**
     * Tests whether the file is younger than a certain specified age and ends with "_Tweet.txt"
     */
    public boolean accept(File file) {
        long diff = new Date().getTime() - file.lastModified();
        if ((diff < age * 60 * 60 * 1000) && (file.toString().endsWith("_Tweets.txt"))) {
            return true;
        }
        return false;
    }
}