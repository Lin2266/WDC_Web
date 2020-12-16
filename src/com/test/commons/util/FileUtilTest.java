package com.test.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.junit.Before;
import org.junit.Test;

public class FileUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	//@Test
	public void testGzip() {
		File from = new File("/tmp/甲骨文.pdf");
		FileUtil.gzip(from);
	}
	
	//@Test
	public void testGunzip() {
		File from = new File("/tmp/甲骨文.pdf.gz");
		File dest = new File("/tmp/out.pdf");
		FileUtil.gunzip(from, dest);
	}
	
	//@Test
	public void testZipFile() throws Exception {
		File[] files = { 
				new File("/tmp/aaa/節能"), 
			};
		File output = new File("/tmp/out.zip");
		FileUtil.zip(output, files, true);
	}
	
	//@Test
	public void testUnzipFile() throws Exception {
		File in = new File("/tmp/out.zip");
		File outDir = new File("/tmp/bbb");
		File[] out = FileUtil.unzip(in, outDir);
		for(File f : out)
			System.out.println(f.getAbsolutePath());
	}
	
	//@Test
	public void testCopyAs() {
		//File from = new File("/tmp/工作內容說明書_謝岳佐.doc");
		File from = new File("/opt/tomcat7");
		//File dest = new File("/tmp/aaa.out");
		File dest = new File("/tmp/bbb");
		FileUtil.copyAs(from, dest);
		System.out.println("testCopyAs(): " + from.getAbsolutePath() + " => " + dest.getAbsolutePath());
		//人工比對
	}
	
	//@Test
	public void testCopyUnder() {
		File from = new File("/opt/tomcat7");
		File dest = new File("/tmp/bbb");
		File ret = FileUtil.copyUnder(from, dest);
		System.out.println("testCopyUnder(): " + from.getAbsolutePath() + " => " + ret.getAbsolutePath());
	}
	
	//@Test
	public void testMoveAs() {
		File from = new File("/tmp/bbb/tomcat7");
		File dest = new File("/tmp/ccc/tomcat");
		File ret = FileUtil.moveAs(from, dest);
		System.out.println("testMoveAs(): " + from.getAbsolutePath() + " => " + ret.getAbsolutePath());
	}
	
	//@Test
	public void testDelete() {
		boolean ret = FileUtil.delete("/tmp/bbb/tomcat7");
		System.out.println("testDelete(): " + ret);
	}
	
	//@Test
	public void testIsEqual() {
		File f1 = new File("/tmp/甲骨文.pdf");
		File f2 = new File("/tmp/out.pdf");
		System.out.println("testIsEqual(): " + FileUtil.isEqual(f1, f2));
	}
	
	//@Test
	public void testDump() throws Exception {
		InputStream in = null;
		OutputStream out = null, out1 = null;
		
		try {
			File f1 = new File("/home/raymond/tmp/test.pdf");
			File f2 = new File("/tmp/a1.pdf");
			File f3 = new File("/tmp/a2.pdf");
			in = new FileInputStream(f1);
			out = new FileOutputStream(f2);
			out1 = new FileOutputStream(f3);
			long n = FileUtil.dump(in, out, out1);
			in.close();
			in = null;
			out.close();
			out = null;
			out1.close();
			out1 = null;
			System.out.println("testDump(): read " + n + " bytes, write " + f2.getAbsolutePath() + ", " + f3.getAbsolutePath());
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
			if(out != null) try { out.close(); } catch(Throwable t) {}
			if(out1 != null) try { out1.close(); } catch(Throwable t) {}
		}
	}
	
	//@Test
	public void testDump1() throws Exception {
		Reader in = null;
		Writer out = null;
		
		try {
			File f1 = new File("/home/raymond/tmp/情境檢查.txt");
			File f2 = new File("/tmp/aaa.txt");
			in = new FileReader(f1);
			out = new FileWriter(f2);
			long n = FileUtil.dump(in, out);
			in.close();
			in = null;
			out.close();
			out = null;
			
			System.out.println("testDump1(): write " + f2.getAbsolutePath() + " (" + n + " chars)");
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
			if(out != null) try { out.close(); } catch(Throwable t) {}
		}
	}
	
	//@Test
	public void testDump2() throws Exception {
		Reader in = null;
		Writer out = null, out1 = null;
		
		try {
			File f1 = new File("/home/raymond/tmp/情境檢查.txt");
			File f2 = new File("/tmp/aaa.txt");
			File f3 = new File("/tmp/aaa1.txt");
			in = new FileReader(f1);
			out = new FileWriter(f2);
			out1 = new FileWriter(f3);
			long n = FileUtil.dump(in, out, out1);
			in.close();
			in = null;
			out.close();
			out = null;
			
			System.out.println("testDump2(): read " + n + " chars, write to " + f2.getAbsolutePath() + ", " + f3.getAbsolutePath());
		} finally {
			if(in != null) try { in.close(); } catch(Throwable t) {}
			if(out != null) try { out.close(); } catch(Throwable t) {}
			if(out1 != null) try { out1.close(); } catch(Throwable t) {}
		}
	}
}
