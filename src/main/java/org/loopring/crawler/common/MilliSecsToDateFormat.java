package org.loopring.crawler.common;

import java.util.Date;
import java.text.SimpleDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.loopring.crawler.core.ValueParser;

@Slf4j
public class MilliSecsToDateFormat implements ValueParser {
    private static final SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public String parse(String milliSecsStr) {
        try {
            long millis = Long.parseLong(milliSecsStr);
            Date d = new Date(millis);
            return dateSDF.format(d);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "";
        }
    }

}
