package com.dabico.gseapp.repository.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Join {
    REGULAR("join "),
    INNER("inner join "),
    LEFT("left join "),
    RIGHT("right join ");

    protected String value;
}
