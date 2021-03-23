package com.canzhang.slow_method_lib;

public interface IBlockHandler {

    public void timingMethod(String method, int cost);

    public String dump();

    public void clear();

    public int threshold();

}
