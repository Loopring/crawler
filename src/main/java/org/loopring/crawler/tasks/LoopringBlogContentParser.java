/**
 * 
 */
package org.loopring.crawler.tasks;

import org.loopring.crawler.core.ValueParser;

/**
 * @author yangli
 *
 */
public class LoopringBlogContentParser implements ValueParser {
    @Override
    public String parse(String content) {
    	
    	if(content == null) {
    		return null;
    	} 

    	return content.replaceAll("<img src=\\\"", "<img src=\"https://blogs.loopring.org");

    }
}
