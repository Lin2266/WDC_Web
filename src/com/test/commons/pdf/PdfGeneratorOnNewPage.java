package com.test.commons.pdf;

public abstract class PdfGeneratorOnNewPage {
	public static final int NORMAL = 0;
	public static final int SUPPRESS_NEW_PAGE = 1;
	
	public int beforeNewPageForDrawTextFlow(PdfGenerator pdf) { return NORMAL;}
	
	public int afterNewPageForDrawTextFlow(PdfGenerator pdf) { return NORMAL; }
}
