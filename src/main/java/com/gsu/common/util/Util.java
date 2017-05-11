package com.gsu.common.util;

import java.io.File;
import java.util.*;

/**
 * Created by cnytync on 07/11/2016.
 */
public class Util {

    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<>();

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesForFolder(fileEntry));
            } else {
                if (fileEntry.getAbsolutePath().contains(".pdf"))
                    files.add(fileEntry.getAbsolutePath());
            }
        }

        return files;
    }

    public static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> sortMapByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<Map.Entry<K, V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }
}
