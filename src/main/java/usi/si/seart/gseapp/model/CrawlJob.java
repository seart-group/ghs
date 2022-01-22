package usi.si.seart.gseapp.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
