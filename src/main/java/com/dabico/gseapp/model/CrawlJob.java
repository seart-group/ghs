package com.dabico.gseapp.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "crawl_job")
@Entity
public class CrawlJob {
    
    @Id
    @GeneratedValue
    @Column(name = "crawl_id")
    Long id;

    @Column(name = "crawled")
    Date crawled;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "language_id")
    SupportedLanguage language;
}
