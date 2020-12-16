package demo.model.vo;

import java.io.Serializable;

import com.test.commons.annotation.Input;

public class Test5 implements Serializable {
	@Input("id_") private Long pk;
	@Input private String cola;
	@Input private String colb;

	public Test5() {}
	
	public Test5(Long pk, String cola, String colb) {
		this.pk = pk;
		this.cola = cola;
		this.colb = colb;
	}
	
	public Long getPk() {
		return pk;
	}

	public void setPk(Long pk) {
		this.pk = pk;
	}

	public String getCola() {
		return cola;
	}

	public void setCola(String cola) {
		this.cola = cola;
	}

	public String getColb() {
		return colb;
	}

	public void setColb(String colb) {
		this.colb = colb;
	}

	@Override
	public String toString() {
		return "Test5 [pk=" + pk + ", cola=" + cola + ", colb=" + colb + "]";
	}
}
