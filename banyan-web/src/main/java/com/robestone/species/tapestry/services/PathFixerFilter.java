package com.robestone.species.tapestry.services;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PathFixerFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		/*
			IconPathFilter.getPathInfo.null
			IconPathFilter.getContextPath./banyan
			IconPathFilter.getLocalName.192.168.2.101
			IconPathFilter.getPathTranslated.null
			IconPathFilter.getQueryString.null
			IconPathFilter.getRequestURI./banyan/search/icons/detail_first.png
			IconPathFilter.getServletPath./search/icons/detail_first.png
			IconPathFilter.getRequestURL.http://99.198.168.89:8080/banyan/search/icons/detail_first.png
		*/
		HttpServletRequest hreq = (HttpServletRequest) request;
		String path = hreq.getServletPath();

		// /search/search.tree/icons/open-children.png
        if (!path.startsWith("/assets")) {
            int pos = path.indexOf("/icons/");
            if (pos > 0) {
            	path = path.substring(pos);
            }
        }

		// /search/search.tree/search.tree/TrF.11213
        if (path.startsWith("/search/search.")) {
        	path = path.substring(7);
        }
        if (path.startsWith("/search.tree/search.tree/")) {
        	path = path.substring(12);
        }
        
    	request = new RequestWrapper((HttpServletRequest) request, path);
		chain.doFilter(request, response);
	}

	private class RequestWrapper extends HttpServletRequestWrapper {
		private String path;
	    public RequestWrapper(HttpServletRequest request, String path) {
	        super(request);
	        this.path = path;
	    }
	    @Override
	    public String getServletPath() {
	    	return path;
	    }
	}
	
}
