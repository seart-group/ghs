package usi.si.seart.gseapp.db_access_service;

import java.util.Date;

public interface ApplicationPropertyService {
    Boolean getEnabled();
    void setEnabled(Boolean value);
    Long getScheduling();
    void setScheduling(Long value);
    Date getStartDate();
    void setStartDate(Date value);
}
