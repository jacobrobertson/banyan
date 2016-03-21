package com.robestone.species.tapestry.services;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.RewriteRuleApplicability;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriteContext;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

/**
 * Purely to help the app run under a context controlled by apache.
 */
public class NfsnContextURLRewriterRule implements URLRewriterRule {

    public Request process(Request request, URLRewriteContext context) {
        String path = request.getPath();
            
        Request origRequest = request;
        if (!path.startsWith("/")) {
        	path = "/" + path;
        }
        if (!path.startsWith("/banyan")) {
        	path = "/banyan" + path;
        }
    	request = new SimpleRequestWrapper(origRequest, path);
        return request;
         
    }

    public RewriteRuleApplicability applicability() {
        return RewriteRuleApplicability.OUTBOUND;
    }

}
