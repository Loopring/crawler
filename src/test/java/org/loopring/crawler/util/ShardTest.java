package org.loopring.crawler.util;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.context.annotation.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;

import org.loopring.crawler.Main;
import org.loopring.crawler.config.JpaConfig;
import org.loopring.crawler.service.ClusterService;
import org.loopring.crawler.repos.WatchedLinkRepo;
import org.loopring.crawler.models.Link;
import org.loopring.crawler.models.WatchedLink;
import org.loopring.crawler.models.system.CrawlerInstance;
import org.loopring.crawler.repos.system.CrawlerInstanceRepo;
import org.loopring.crawler.test.TestAppContext;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestAppContext.class, JpaConfig.class})
public class ShardTest {

    @Autowired
    private CrawlerInstanceRepo crawlerInstanceRepo;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private WatchedLinkRepo watchedLinkRepo;

    @Test
    public void testShard() {

        String linkUuids1 = "r4bar/bcy7DBlrsoTyvXxBHwOS8=, pcq+smQJx5tp2JR9N0HTky2W2vo=, qKyJRvw+MghCmPsrZP5kc9vJf98=, VK+OBQg8LC/+vkv98hAU+ZnrFzY=, at8QzForQFlW0/d1KkdcXBmwsJg=, XFgeyM0NhM8fRNDSPvGaEtZ7QJs=, bqswWfaP7UsFejNr6GXZiNhDu8I=, RidWguOCmjCTYopsavY8UraedVM=, /fC7Xe3J88leVAsXbUw2fgv49qo=, X1opUcyeX3dkAEBrPUIUMJCwseU=, x/LeNMtwoVrXQJ9pmqMC2YhNVYs=, 9OWRLp2MiJ8ihEIcaK9fHkkw5SU=, 04N6wU2LzlLp74kbLH6vR4SLZe4=, pHanS+sjNlEz8vpavCHl6thXdPM=, wMv1jLdSxcxIeQLB2iBSEcBzEwU=, cLUbXl1RWbEC5D7gY8atefyns4Q=, 5HABKqbisaec3aiHRCcO3ntfiEA=, VRoPbyL2WiwU7udBQuLO4JrUc3I=, clKQPCyizPIsCpeUvu/cF2F269E=, qmUOyelzPGxJ+QVo0n8e9OQIPlI=, Z8VhjnYpVNcgWqlXB+Zq0zqK+yo=, HmSMtqQRXb1NfUhfyxHP+2RxuTw=, Xhy7/O+zusq5kIWBmTLiAySusDc=, ifDZj9myd/ytgg5sQTxwx3drg8E=, AVc3uNVBsH+/kJK153JbCmloDdk=, jtcX03BSc2lxVwFfthlIGRBNNlM=, nX/tR3L0iB33xWl6k7bSVrvOdYo=, Pwag9J70j/+NS3mdsM50iTzewUE=, i4ruPDK6njbgre4WIJI8+9uO7lk=, BEF1Q47kKsewDSaOY/rrVC3Vxy8=";

        String[] instanceIds = new String[]{"fa018ca3-f9b8-44ec-9f7e-9e7db426bf48", "130050b5-d69a-46ad-b27e-89836a0f3bb4"};

        String args0 = "test";
        CrawlerInstance ci1 = new CrawlerInstance(instanceIds[0], "");
        ci1.setProgramArgs0(args0);

        CrawlerInstance ci2 = new CrawlerInstance(instanceIds[1], "");
        ci2.setProgramArgs0(args0);

        List<CrawlerInstance> crawlersInDB = new ArrayList<>();
        crawlersInDB.add(ci1);
        crawlersInDB.add(ci2);

        // long currMillis = System.currentTimeMillis();
        // Timestamp updateTimeCondition = new Timestamp(currMillis - 10 * 10000);
        // List<CrawlerInstance> crawlersInDB = crawlerInstanceRepo.findTop256ByProgramArgs0AndUpdateTimeAfter(args0, updateTimeCondition);
        // List<WatchedLink> linkList = watchedLinkRepo.findTop30ByTaskNameAndStatusLessThan("tianyancha-register-info-link-creator", Link.STATUS_PROCESSING);
        Shard<CrawlerInstance> shard = new Shard<>(crawlersInDB);

        // List<String> crawlerUuids = new ArrayList<>();
        // for (CrawlerInstance ci : crawlersInDB) {
        //     crawlerUuids.add(ci.getUuid());
        // }
        // log.info("crawler uuids: {}", crawlerUuids);

        String[] uuids = linkUuids1.split(",");

        for (String uuid : uuids) {
            CrawlerInstance ci = shard.getShardInfo(uuid);
            log.info("assigned to : {}", ci.getUuid());
        }

        assertThat(true).isEqualTo(true);
    }

    @Data
    class Machine {
        private final Long id;
        private final String name;
    }

    @Test
    public void testHash() {
        System.out.println("test hash...");

        Machine m1 = new Machine(1L, "m001");
        Machine m2 = new Machine(2L, "m002");
        Machine m3 = new Machine(3L, "m003");
        Machine m4 = new Machine(4L, "m004");
        List<Machine> machineList = new ArrayList<>();
        machineList.add(m1);
        machineList.add(m2);
        machineList.add(m3);
        machineList.add(m4);

        Shard<Machine> shard = new Shard(machineList);

        int m1Count = 0;
        int m2Count = 0;
        int m3Count = 0;
        int m4Count = 0;
        for (int i = 0; i < 50; i++) {
            String uuid = UUID.randomUUID().toString();

            Machine m = shard.getShardInfo(uuid);
            if (m.getId() == 1) {
                m1Count ++;
            } else if (m.getId() == 2) {
                m2Count ++;
            } else if (m.getId() == 3) {
                m3Count ++;
            } else if (m.getId() == 4) {
                m4Count ++;
            } else {
                System.out.println("ERROR.");
            }

            System.out.println("m: " + m);
            assertThat(machineList.contains(m)).isEqualTo(true);
        }

        System.out.println("m1Count: " + m1Count + "; m2Count: " + m2Count + "; m3Count: " + m3Count + "; m4Count: " + m4Count);
    }

}
