package usi.si.seart.gseapp.db_access_service;

import java.util.Date;

public interface ApplicationPropertyService {
    Boolean getEnabled();
    void setEnabled(Boolean value);
    Date getStartDate();
    void setStartDate(Date value);
    Long getCrawlScheduling();
    void setCrawlScheduling(Long value);
    Long getCleanUpScheduling();
    void setCleanUpScheduling(Long value);
    Long getCacheEvictScheduling();
    void setCacheEvictScheduling(Long value);
    Integer getPageSize();
    void setPageSize(Integer pageSize);
}
