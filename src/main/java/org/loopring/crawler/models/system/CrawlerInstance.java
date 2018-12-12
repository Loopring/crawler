package org.loopring.crawler.models.system;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "crawler_instance")
public class CrawlerInstance {

    public static final String PROGRAM_TYPE_COLLECTOR = "collector";
    public static final String PROGRAM_TYPE_PROCESSOR = "processor";

    public CrawlerInstance() { }

    public CrawlerInstance(String uuid, String ip) {
        this.uuid = uuid;
        this.ip = ip;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false, precision = 20, scale = 0)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column
    private String ip;

    @Column(name="program_args0")
    private String programArgs0;

    @Column(name = "insert_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp insertTime;

    @Column(name = "update_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = true)
    private Timestamp updateTime;

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CrawlerInstance)) return false;
        CrawlerInstance other = (CrawlerInstance) o;
        if (this.uuid == null) return false;
        return this.uuid.equals(other.getUuid());
    }

}
