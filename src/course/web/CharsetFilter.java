package course.web;

import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * 在Servlet運作前，由這個Filter類別統一設定編碼字元集
 * @author Administrator
 */
@WebFilter(filterName = "CharsetFilter", urlPatterns = {"*.jsp", "*.view", "*.do"},
        dispatcherTypes = {DispatcherType.FORWARD, DispatcherType.REQUEST, DispatcherType.ERROR},
        initParams = {@WebInitParam(name = "charset", value = "utf-8")})
public class CharsetFilter implements Filter {
    /**
     * 
     */
    private FilterConfig filterConfig;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String charset = filterConfig.getInitParameter("charset");
        if (charset==null) charset = "utf-8";
        
        request.setCharacterEncoding(charset);
        request.getParameterNames();
                
        response.setCharacterEncoding(charset);
        response.getWriter();
        
        chain.doFilter(request, response);
        
    }

    @Override
    public void destroy() {
        
    }
}
