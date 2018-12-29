/**
 * 
 */
package org.loopring.crawler.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.loopring.crawler.core.ValueParser;

/**
 *
 */
public class EightBitFlashPublishTimeStrParser implements ValueParser {
    @Override
    public String parse(String publishTimeStr) {    	
    	if(publishTimeStr == null) {
    		return null;
    	} 
    	
    	//时间格式hh:mm  
    	String pattern = "^([1-9]|1[0-2]|0[1-9]){1}(:[0-5][0-9]){1}$"; 
    	boolean isMatch = Pattern.matches(pattern, publishTimeStr);
    	if (isMatch) {           
    		Date date0 = new Date();
    		SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd");
        	System.out.println(format0.format(date0.getTime()));
    		return format0.format(date0.getTime()) + " " + publishTimeStr;
    	}
    	
    	return publishTimeStr;

    }
}
