package demo.model.vo;

import java.io.Serializable;

import com.test.commons.annotation.Input;

public class Test4 implements Serializable {
	@Input("id_") private Long pk;
	@Input private String col1;
	@Input private String col2;
	@Input private String col3;

	public Test4() {}
	
	public Test4(Long pk, String col1, String col2, String col3) {
		this.pk = pk;
		this.col1 = col1;
		this.col2 = col2;
		this.col3 = col3;
	}

	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getCol1() {
		return col1;
	}

	public void setCol1(String col1) {
		this.col1 = col1;
	}

	public String getCol2() {
		return col2;
	}

	public void setCol2(String col2) {
		this.col2 = col2;
	}

	public String getCol3() {
		return col3;
	}

	public void setCol3(String col3) {
		this.col3 = col3;
	}

	@Override
	public String toString() {
		return "Test4 [pk=" + pk + ", col1=" + col1 + ", col2=" + col2
				+ ", col3=" + col3 + "]";
	}
}
