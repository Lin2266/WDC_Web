package jdcs3c000;

import dcstools.*;


public class jdcs3c000Bean {
	private String[] Cname = { "rowid", "name", "cat", "psid", "rstp", "word", "sno", "listid1", "list1", "listid2", "list2", "listcnt1", "listcnt2", "mod", "batno", "subj", "refg", "rdate", "date",
			"v_filename", "flmyr", "yy1", "mm1", "dd1", "p_filename", "secur", "tfdt", "dpid", "psid", "tqm1", "tqm2", "type", "qty", "meas", "cat2", "grsno", "sndt", "clan", "sel", "exsubj",
			"memo" };
	private String[] Cdata = new String[Cname.length];

	public jdcs3c000Bean() {
		for(int i = 0; i < Cname.length; i++)
			Cdata[i] = "";
	}

	public String getI(String n) {
		for(int i = 0; i < Cname.length; i++) {
			if(n.equals(Cname[i])) {
				if(Cdata[i].equals(""))
					return "''";
				else
					return Cdata[i];
			}
		}
		return null;
	}

	public String getS(String n) {
		for(int i = 0; i < Cname.length; i++) {
			if(n.equals(Cname[i])) {
				if(Cdata[i].equals(""))
					return "''";
				else
					return "'" + dcstools.CutLinefeed.ChgQuote(Cdata[i]) + "'";
			}
		}
		return null;
	}

	public String get(String n) {
		for(int i = 0; i < Cname.length; i++) {
			if(n.equals(Cname[i]))
				return Cdata[i];
		}
		return null;
	}

	public void set(String n, String x) {
		dcstools.Decode de = new dcstools.Decode();
		for(int i = 0; i < Cname.length; i++) {
			if(n.equals(Cname[i])) {
				if(x != null)
					Cdata[i] = de.Decode(x);
				else
					Cdata[i] = "";
				break;
			}
		}
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder().append("jdcs3c000Bean [");
		for(int i = 0; i < this.Cname.length; i++)
			s.append(this.Cname[i]).append("=").append(this.Cdata[i]).append(", ");
		s.append("]");
		return s.toString();
	}
}
