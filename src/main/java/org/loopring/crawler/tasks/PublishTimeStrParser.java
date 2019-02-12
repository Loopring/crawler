/**
 * 
 */
package org.loopring.crawler.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.loopring.crawler.core.ValueParser;

/**
 * 以下各种补全数据仅为APP显示美观服务，补充数据并不是实际情况
 */
public class PublishTimeStrParser implements ValueParser {
	@Override
	public String parse(String publishTimeStr) {
		Date dateCurrent = new Date();
		SimpleDateFormat formatDefault = null;
		
		if (publishTimeStr == null) {
			formatDefault = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return formatDefault.format(dateCurrent.getTime());
		}

		@SuppressWarnings("serial")
		HashMap<String, String> patternMap = new HashMap<String, String>() {
			{
				put("hh:mm", "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");
				put("yyyy-MM-dd HH:mm:ss", "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
				put("yyyy-MM-dd HH:mm", "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
				put("yyyy/MM/dd HH:mm:ss", "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}");
				put("yyyy/MM/dd HH:mm", "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}");
				put("yyyy年MM月dd日", "^(\\d{4})年(\\d{2})月(\\d{2})日$");
			}
		};

		String key = "";
		for (Entry<String, String> entry : patternMap.entrySet()) {
			String pattern = entry.getValue();
			if (Pattern.matches(pattern, publishTimeStr)) {
				key = entry.getKey();
				break;
			}
		}

		switch (key) {
		case "hh:mm":
			formatDefault = new SimpleDateFormat("yyyy-MM-dd");
			return formatDefault.format(dateCurrent.getTime()) + " " + publishTimeStr + ":00";
		case "yyyy-MM-dd HH:mm:ss":
			return publishTimeStr;
		case "yyyy-MM-dd HH:mm":
			return publishTimeStr + ":00";
		case "yyyy/MM/dd HH:mm:ss":
			return publishTimeStr.replace("/", "-");
		case "yyyy/MM/dd HH:mm":
			return publishTimeStr.replace("/", "-") + ":00";
		case "yyyy年MM月dd日":
			formatDefault = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = formatDefault.format(dateCurrent.getTime());
			return publishTimeStr.replace("年", "-").replace("月", "-").replace("日", " ") + currentDate.substring(11);
		default:
			System.out.println("##### 出现新增时间格式 ##### " + publishTimeStr);
			formatDefault = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(formatDefault.format(dateCurrent.getTime()));
			return formatDefault.format(dateCurrent.getTime());
		}
	}
}
