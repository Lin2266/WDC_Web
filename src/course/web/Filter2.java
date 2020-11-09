package course.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;


public class Filter2 implements Filter {


	public void init(FilterConfig fConfig) throws ServletException {
		
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.println("Befor Filter2");
		chain.doFilter(request, response);
		System.out.println("After Filter2");
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}



}
