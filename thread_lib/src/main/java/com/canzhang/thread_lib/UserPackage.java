package com.canzhang.thread_lib;

import java.util.ArrayList;

public class UserPackage {
    private static ArrayList<String> packageList = new ArrayList<>();

    static void buildPackageList() {
        packageList.add("--");
    }

    public static ArrayList<String> getPackageList() {
        return packageList;
    }
}
