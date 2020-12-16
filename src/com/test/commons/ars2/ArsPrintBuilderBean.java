package com.test.commons.ars2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 協助產生 ArsPrint 物件.<br>
 * 本工具可登錄於 Spring context 中, 並設置屬性值(如 defaultBaseTempDir 等).
 */
public class ArsPrintBuilderBean {
	private File defaultBaseTempDir;

	public String getDefaultBaseTempDir() {
		return (this.defaultBaseTempDir == null) ? null : this.defaultBaseTempDir.getAbsolutePath();
	}

	public void setDefaultBaseTempDir(String defaultBaseTempDir) {
		this.defaultBaseTempDir = new File(defaultBaseTempDir);
	}
	
	public ArsPrint build(final String outputFilename) {
		try {
			final File f = new File(outputFilename);
			final boolean isOutFilenameAbsolute = f.isAbsolute();
			File outputFile = null;
			if(isOutFilenameAbsolute) { 
				outputFile = f;
			} else {
				if(this.defaultBaseTempDir == null)
					throw new IllegalStateException("property 'defaultBaseTempDir' not specified");
				final File tmpDir = Files.createTempDirectory(this.defaultBaseTempDir.toPath(), "arsprint-").toFile();
				tmpDir.mkdirs();
				outputFile = new File(tmpDir, outputFilename);
			}
			
			return build(outputFile);
		} catch(IOException ie) {
			throw new RuntimeException(ie.getMessage(), ie);
		}
	}
	
	public ArsPrint build(final File outputFile) {
		final ArsPrint ars = new ArsPrint();
		ars.setOutputFile(outputFile);
		return ars;
	}

	@Override
	public String toString() {
		return "ArsPrintBuilderBean [defaultBaseTempDir=" + defaultBaseTempDir + "]";
	}
}
