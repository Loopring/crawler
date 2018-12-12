package org.loopring.crawler.models;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.loopring.crawler.Utils;

import lombok.Data;

//import javax.persistence.Temporal;
//import javax.persistence.TemporalType;

@Entity
@Table(name = "time_cursor")
@Data
public class TimeCursor extends BasicModel {

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "source_entity")
    private String sourceEntity;

    @Column(name = "target_entity")
    private String targetEntity;

    @Column(name = "time_cursor")
    private Timestamp timeCursor;

    public String createUuid() {

        String source = taskName + "|" + sourceEntity + "|" + targetEntity;
        return Utils.fingerPrint(source);
    }
}
