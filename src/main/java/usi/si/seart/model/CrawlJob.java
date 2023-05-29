package usi.si.seart.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CrawlJob crawlJob = (CrawlJob) o;
        return getId() != null && Objects.equals(getId(), crawlJob.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
