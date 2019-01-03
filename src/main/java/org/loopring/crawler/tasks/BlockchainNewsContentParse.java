/**
 * 
 */
package org.loopring.crawler.tasks;

import org.loopring.crawler.core.ValueParser;

/**
 *
 */
public class BlockchainNewsContentParse implements ValueParser {
	@Override
	public String parse(String content) {
		if (content == null) {
			return null;
		}
		int pos = content.indexOf("Related Items:");
		if(pos > 0) {
		   return content.substring(0, pos);
		}
		return content;
	}
}
