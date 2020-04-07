package com.dabico.gseapp.repository.util;

import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
public class JPAQueryBuilder {
    private StringBuilder select;
    private StringBuilder from;
    private StringBuilder join;
    private StringBuilder where;
    private StringBuilder groupBy;
    private StringBuilder having;
    private StringBuilder orderBy;

    public String build(){
        return select.toString() +
               from.toString() +
               join.toString() +
               where.toString() +
               groupBy.toString() +
               having.toString() +
               orderBy.toString();
    }

    public void select(String columnName){
        select(columnName,"",false);
    }

    public void select(String columnName, String alias){
        select(columnName,alias,false);
    }

    public void select(String columnName, boolean distinct){
        select(columnName,"",distinct);
    }

    public void select(String columnName, String alias, boolean distinct){
        if (select.length() > 0){
            select.append(Operator.COMMA.value);
        } else {
            select.append(Clause.SELECT.value);
            if (distinct) select.append(Operator.DISTINCT.value);
        }
        select.append(columnName).append(" ");
        if (!alias.isBlank()){
            select.append(Operator.AS.value);
            select.append(alias).append(" ");
        }
    }

    public void from(String tableName){
        from(tableName,"");
    }

    public void from(String tableName, String alias){
        if (from.length() > 0) from.append(Operator.COMMA);
        else from.append(Clause.FROM.value);
        from.append(tableName).append(" ");
        if (!alias.isBlank()) from.append(alias).append(" ");
    }

    public void join(String tableName, String alias, Join type){
        join(tableName,alias,"",type);
    }

    public void join(String tableName, String alias, String expression, Join type){
        join.append(type.value).append(tableName).append(" ").append(alias).append(" ");
        if (!expression.isBlank()) join.append(Operator.ON.value).append(expression).append(" ");
    }

    public void where(String condition, Operator operator){
        if (where.length() > 0) where.append(operator.value);
        else where.append(Clause.WHERE.value);
        where.append(condition).append(" ");
    }

    public void having(String condition, Operator operator){
        if (having.length() > 0) having.append(operator.value);
        else having.append(Clause.HAVING.value);
        having.append(condition).append(" ");
    }

    public void groupBy(String criterion){
        if (groupBy.length() > 0) groupBy.append(Operator.COMMA.value);
        else groupBy.append(Clause.GROUP_BY.value);
        groupBy.append(criterion).append(" ");
    }

    public void orderBy(String criterion){
        orderBy(criterion,true);
    }

    public void orderBy(String criterion, boolean ascending){
        if (orderBy.length() > 0) orderBy.append(Operator.COMMA.value);
        else orderBy.append(Clause.ORDER_BY.value);
        orderBy.append(criterion).append(" ");
        if (ascending) orderBy.append("asc ");
        else orderBy.append("desc ");
    }
}
