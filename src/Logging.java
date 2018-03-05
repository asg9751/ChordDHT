package edu.rit.CSCI652.impl;

/**
 * @author Amol Gaikwad
 * <p>
 * Logging utility
 */

public class Logging {

    static boolean on = true;

    public static void print(String s) {

        if (on)
            System.out.println(s);
    }

    public static void print(int s) {

        print(Integer.toString(s));
    }
}
