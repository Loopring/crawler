/**
 * 
 */
package org.loopring.crawler.tasks;

import org.loopring.crawler.core.ValueParser;

public class LoopringBlogImageUrlParser implements ValueParser {
    @Override
    public String parse(String imageUrlStr) {
    	String prefix = "background-image: url(";
    	
    	if(imageUrlStr == null) {
    		return null;
    	} 

        if(imageUrlStr.startsWith(prefix) && imageUrlStr.endsWith(")")) {
            return "https://blogs.loopring.org" + imageUrlStr.substring(prefix.length(), imageUrlStr.length()-1);
        }
        
        return null;

    }
}
