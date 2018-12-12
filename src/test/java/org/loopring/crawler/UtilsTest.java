package org.loopring.crawler;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void testFingerPrint() {
        String src = "http://www.tianyancha.com/company/8348582";
        String fp = Utils.fingerPrint(src);
        System.out.println("fp: " + fp);
        assertThat(fp).isEqualTo("VnhKJNI4TjWPU2y3Qw8J67F6U2E=");
    }

}
