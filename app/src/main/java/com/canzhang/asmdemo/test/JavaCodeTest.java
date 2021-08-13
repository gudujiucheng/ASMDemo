package com.canzhang.asmdemo.test;

/**
 * 字节码相关测试
 *
 * 下面这个是个在线编译网站  很好用
 * https://javap.yawk.at/#NDJVKw
 */
public class JavaCodeTest {
    private void div(int a, int b) {
        try{
            int c = a / b;
            System.out.println(c);
        }catch (Exception e){

        }

    }
}
