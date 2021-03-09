package havis.test.suite.common;

import havis.test.suite.common.Diff;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DiffTest {

	@Test
	public void getDiff()
	{
		String a = "";
		String b = "";
		Diff d = new Diff(a,b);
		String result = d.getDiff(0);
		Assert.assertNull(result);
		
        a = "";
        b = "x";
        d = new Diff(a, b);
        result = d.getDiff(0);
        Assert.assertEquals(result, "+ ...x...");
        
        a = "x";
        b = "";
        d = new Diff(a, b);
        result = d.getDiff(0);
        Assert.assertEquals(result, "- ...x...");
		
        a = "abcdef";
        b = "abcXefghijklmn";
        d = new Diff(a, b);
        result = d.getDiff(0);
        Assert.assertEquals(result, "- ...d..."+System.getProperty("line.separator")+"+ ...X...");
        
        result = d.getDiff(1);
        Assert.assertEquals(result, "- ...cde..."+System.getProperty("line.separator")+"+ ...cXe...");
        
        result = d.getDiff(20);
        Assert.assertEquals(result, "- ...abcdef..."+System.getProperty("line.separator")+"+ ...abcXefghijklmn...");
		
	}

}
