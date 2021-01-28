package usi.si.seart.gseapp.repository.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Operator {
    AND("and "),
    AS("as "),
    BETWEEN("between "),
    DISTINCT("distinct "),
    LIKE("like "),
    OR("or "),
    ON("on "),

    COMMA(", "),
    NONE("");

    protected String value;
}
