package de.adorsys.datasafe.simple.adapter.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 25.05.18 at 16:32.
 */
public class LogStringFrame {
    private List<String> list = new ArrayList<>();

    public void add(String line) {
        list.add(line);
    }

    public String toString() {
        int max = 0;
        for (String line : list) {
            if (line.length() > max) max = line.length();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("***" + fill("", max, "*") + "***\n");
        sb.append("*  " + fill("", max, " ") + "  *\n");

        for (String line : list) {
            sb.append("*  " + fill(line, max, " ") + "  *\n");
        }

        sb.append("*  " + fill("", max, " ") + "  *\n");
        sb.append("***" + fill("", max, "*") + "***\n");
        return sb.toString();

    }

    private String fill(String start, int length, String el) {
        while(start.length() < length) {
            start = start + el;
        }
        return start;
    }
}
