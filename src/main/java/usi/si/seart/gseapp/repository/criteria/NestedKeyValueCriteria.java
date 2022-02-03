package usi.si.seart.gseapp.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import usi.si.seart.gseapp.repository.operation.BinaryOperation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NestedKeyValueCriteria implements Criteria {
    String outerKey;
    String innerKey;
    Object value;
    BinaryOperation operation;
}
