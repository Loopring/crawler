package org.loopring.crawler.models;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "coin")
@Data
public class Coin extends BasicModel {

    @Column(columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String symbol;

    @Override
    public boolean isValid() {
        return symbol != null && !"".equals(symbol);
    }
}
