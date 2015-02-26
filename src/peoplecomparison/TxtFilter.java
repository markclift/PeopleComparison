package peoplecomparison;

/**
 * Created by Mark Clift on 20/02/15.
 */

import java.io.*;
import java.util.*;

/** A simple file filter */
class TxtFilter implements FileFilter {

    private List<String> names;

    public TxtFilter(List<String> names) {
        super();
        this.names = names;
    }

    /**
     * Tests whether the string representation of the file
     * ends with "_Tweets.txt". Note that {@ref FileIterator}
     * will only call this filter if the file is not a directory,
     * so we do not need to test that it is a file.
     */
    public boolean accept(File file) {
        for (String name : names) {
            if (file.toString().endsWith(name + "_Tweets.txt")) {
                return true;
            }
        }
        return false;
    }
}