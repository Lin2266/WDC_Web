package com.test.commons.ars2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.test.commons.util.FileUtil;
import com.test.commons.util.StrUtil;

/**
 * 把多個檔案組成 ars2 檔, 供前端列印軟體送印.
 * <br>
 * 格式: [檔名長度,16進位,4碼][檔名1][內容長度,16進位,8碼][內容][檔名長度,16進位,4碼][檔名2][內容長度,16進位,8碼][內容]...
 * <br>
 * 用例:
 * <pre><code> &#64;Resource(name="arsPrintBuilderBean")
 * private ArsPrintBuilderBean arsPrintBuilderBean;
 * ...
 * 
 *    try (ArsPrint ars = this.arsPrintBuilderBean.build("output.ars2")) {
 *        ars.append(file1);
 *        ars.append(file2);
 *        ...
 *        ars.close(); //不先 close() 而取輸出檔, 恐怕得到殘缺不全的內容
 *        File outputFile = ars.getOutputFile(); //輸出檔 *.ars2
 *    }
 * </code></pre>
 */
public class ArsPrint implements AutoCloseable {
	/** 輸出檔案之副檔名 */
	public static final String EXT_NAME = ".ars2";
	
	private File outputFile;
	private OutputStream output;
	private boolean closed;
	
	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		if(!outputFile.getName().endsWith(EXT_NAME))
			throw new IllegalArgumentException("filename of argument 'outputFile' not end with '" + EXT_NAME + "'");
		this.outputFile = outputFile;
	}
	
	/** 加入一文件檔案到 ars2 檔 */
	synchronized public ArsPrint append(final File inputFile) {
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
			return append(inputFile.getName(), in);
		} catch(IOException ie) {
			throw new RuntimeException(ie.getMessage(), ie);
		}
	}

	/**
	 * 加入一文件檔內容到 ars2 檔
	 * @param filename 被加入的檔案之檔名(不含路徑)
	 * @param input 被加入的檔案內容
	 */
	synchronized public ArsPrint append(final String filename, final InputStream input) {
		try {
			final OutputStream out = getOutput();
			final byte[] filenameBytes = filename.getBytes("UTF-8");
			out.write(StrUtil.alignRight(Integer.toHexString(filenameBytes.length), 4, '0').getBytes()); //檔名長度(byte 數)
			out.write(filenameBytes); //檔名
	
			//單一檔要讀進 buffer 才能得出正確的檔案大小 (TODO: 大檔案須先寫至暫存檔案)
			try (ByteArrayOutputStream out1 = new ByteArrayOutputStream()) {
				FileUtil.dump(input, out1);
				final int fileLength = out1.size();
				out.write(StrUtil.alignRight(Integer.toHexString(fileLength), 8, '0').getBytes()); //檔案內容長度(byte 數)
				out.write(out1.toByteArray()); //檔案內容
			}
			return this;
		} catch(IOException ie) {
			throw new RuntimeException(ie.getMessage(), ie);
		}
	}

	@Override
	synchronized public void close() {
		if(this.output != null) {
			try {
				this.output.flush();
				this.output.close();
				this.output = null;
				this.closed = true;
			} catch(IOException ie) {
				throw new RuntimeException(ie.getMessage(), ie);
			}
		}
	}
	
	synchronized private OutputStream getOutput() {
		if(this.closed)
			throw new IllegalStateException("outputFile stream already closed");
		if(this.output != null)
			return this.output;
		
		if(this.outputFile == null)
			throw new IllegalStateException("outputFile property not specified first");
		try {
			this.output = new BufferedOutputStream(new FileOutputStream(this.outputFile));
		} catch(IOException ie) {
			throw new RuntimeException(ie.getMessage(), ie);
		}
		return this.output;
	}
}
