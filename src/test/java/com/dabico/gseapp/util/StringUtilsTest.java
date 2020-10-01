package com.dabico.gseapp.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    private static final String emptyStr = "";
    private static final String testStr = "This Is A Test String";

    @Test
    public void testRemoveFromStart(){
        String newStr = StringUtils.removeFromStart(testStr,5);
        Assert.assertEquals(testStr,"This Is A Test String");
        Assert.assertEquals(newStr,"Is A Test String");
    }

    @Test
    public void testRemoveFromEnd(){
        String newStr = StringUtils.removeFromEnd(testStr,7);
        Assert.assertEquals(testStr,"This Is A Test String");
        Assert.assertEquals(newStr,"This Is A Test");
    }

    @Test
    public void testRemoveFromStartAndEnd(){
        String newStr = StringUtils.removeFromStartAndEnd(testStr,5,7);
        Assert.assertEquals(testStr,"This Is A Test String");
        Assert.assertEquals(newStr,"Is A Test");
    }

    @Test
    public void testRemoveFromEmptyString(){
        String newStr1 = StringUtils.removeFromStart(emptyStr,5);
        String newStr2 = StringUtils.removeFromEnd(emptyStr,7);
        String newStr3 = StringUtils.removeFromStartAndEnd(emptyStr,5,7);
        Assert.assertEquals(emptyStr,"");
        Assert.assertEquals(newStr1,"");
        Assert.assertEquals(newStr2,"");
        Assert.assertEquals(newStr3,"");
    }

    @Test
    public void testRemoveNothing(){
        String newStr1 = StringUtils.removeFromStart(testStr,0);
        String newStr2 = StringUtils.removeFromEnd(testStr,0);
        String newStr3 = StringUtils.removeFromStartAndEnd(testStr,0,0);
        Assert.assertEquals(testStr,"This Is A Test String");
        Assert.assertEquals(testStr,newStr1);
        Assert.assertEquals(testStr,newStr2);
        Assert.assertEquals(testStr,newStr3);
    }
}
