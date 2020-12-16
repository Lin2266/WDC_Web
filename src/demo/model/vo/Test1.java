package demo.model.vo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import javax.persistence.Column;

/**
 * VO (value object),
 * 對應 table: demo_test1
 */
@SuppressWarnings("serial")
public class Test1 implements Serializable {
	//借用 JPA 的 @Column annotation 以供 JdbcTemplate+BeanRowMapper 工具能自動將每筆查詢結果置入對應的 VO 物件中
    @Column(name="id_") private Long id;
    @Column private Integer col1;
    @Column private String col2;
    @Column private Date col3;
    @Column private String col4;
    private String[] col5;
    private String col6;
    private Integer[] col7; //for demo only, 仍應以使用 Integer 型態為佳
    
    //與 db table 對應的 VO 必須具備 default 建構式
    public Test1() {}
    
    //為了方便特設的建構式而已
    public Test1(Long id, Integer col1, String col2, Date col3, String col4, String[] col5, String col6, Integer[] col7) {
    	this.id = id;
        this.col1 = col1;
        this.col2 = col2;
        this.col3 = col3;
        this.col4 = col4;
        this.col5 = col5;
        this.col6 = col6;
        this.col7 = col7;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCol1() {
        return col1;
    }
    
    public void setCol1(Integer col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public Date getCol3() {
        return col3;
    }

    public void setCol3(Date col3) {
        this.col3 = col3;
    }

    public String getCol4() {
        return col4;
    }

    public void setCol4(String col4) {
        this.col4 = col4;
    }

	public String[] getCol5() {
		return col5;
	}

	public void setCol5(String[] col5) {
		this.col5 = col5;
	}

	public String getCol6() {
		return col6;
	}

	public void setCol6(String col6) {
		this.col6 = col6;
	}

	public Integer[] getCol7() {
		return col7;
	}

	public void setCol7(Integer[] col7) {
		this.col7 = col7;
	}

	@Override
	public String toString() {
		return "Test1 [id=" + id + ", col1=" + col1 + ", col2=" + col2
				+ ", col3=" + col3 + ", col4=" + col4 + ", col5="
				+ Arrays.toString(col5) + ", col6=" + col6 + ", col7="
				+ Arrays.toString(col7) + "]";
	}
}
