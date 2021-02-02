package com.canzhang.asmdemo.test;

/**
 * 字节码相关测试
 */
public class JavaCodeTest {

    public JavaCodeTest() {
        System.out.println("返回值："+test());
    }

    public int test() {
        int x;
        try{
            x=1;
            return x;//无异常场景，返回1
        }catch (Exception e){//再异常捕获范围内，返回2，并会执行finally
            x=2;
            return x;
        }finally {//出现异常（无论是否能捕获），跳转这里处理
            x=3;
        }

    }

}
