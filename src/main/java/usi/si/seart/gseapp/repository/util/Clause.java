package usi.si.seart.gseapp.repository.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Clause {
    SELECT("select "),
    FROM("from "),
    WHERE("where "),
    HAVING("having "),
    GROUP_BY("group by "),
    ORDER_BY("order by ");

    protected String value;
}
