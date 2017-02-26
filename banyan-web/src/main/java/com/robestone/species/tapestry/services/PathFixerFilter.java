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
		String origPath = path;

		//*
		System.out.println("hreq.getContextPath() = " + hreq.getContextPath());
		System.out.println("hreq.getAttributeNames() = " + hreq.getAttributeNames());
		System.out.println("hreq.getPathInfo() = " + hreq.getPathInfo());
		System.out.println("hreq.getQueryString() = " + hreq.getQueryString());
		System.out.println("hreq.getRequestURI() = " + hreq.getRequestURI());
		System.out.println("hreq.getAttributeNames() = " + hreq.getAttributeNames());
		//*/
		

		if (path.equals("/")) {
			path = "/search";
		}

        if (path.startsWith("/banyan")) {
        	path = path.substring(7);
        }

		// /search/search.tree/icons/open_children.png
        if (!path.startsWith("/assets")) {
            int pos = path.indexOf("/icons/");
            if (pos > 0) {
            	path = path.substring(pos);
            }
            pos = path.indexOf("/style/");
            if (pos > 0) {
            	path = path.substring(pos);
            }
            pos = path.indexOf("/js/");
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

        String queryString = hreq.getQueryString();

        /* these hidden links are making google complain
         * 
         * Can apply to any page, not just examples
			/examples.navigationbar.showchildren
			/examples.navigationbar.hidechildren
			/examples.navigationbar.focus
			/examples.navigationbar.close
			  
			might also have t:ac=lQ0.13 at the end
         */
        int navBarLinkPos = path.indexOf(".navigationbar.");
        if (navBarLinkPos > 0) {
        	int formPos = path.indexOf(".navigationbar.form");
        	if (formPos < 0) {
        		path = path.substring(0,  navBarLinkPos);
        		queryString = null;
        	}
        }
        
        
        // these are hidden links that google is finding - need to fix so google won't complain
        // change these -- hreq.getQueryString() = t:ac=lQ1.3
        // /search.detail?t:ac=2rE2rLxUkKbl
        // /search.tree?t:ac=2rE2rLxUkKbl
        // to this
        // /search/search.tree/2rE2rLxUkKbl
        if (queryString != null && queryString.startsWith("t:ac=")) {
        	path = "/search.tree/" + queryString.substring(5);
        	queryString = null;
        }
        
		if (origPath != path) {
			request = new RequestWrapper((HttpServletRequest) request, path, queryString);
		}
		chain.doFilter(request, response);
	}

	private class RequestWrapper extends HttpServletRequestWrapper {
		private String path;
		private String queryString;
	    public RequestWrapper(HttpServletRequest request, String path, String queryString) {
	        super(request);
	        this.path = path;
	        this.queryString = queryString;
	    }
	    @Override
	    public String getServletPath() {
	    	return path;
	    }
	    @Override
	    public String getQueryString() {
	    	return queryString;
	    }
	}
	
}
