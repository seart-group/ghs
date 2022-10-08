package usi.si.seart.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.repository.operation.BinaryOperation;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NestedKeyValueCriteria implements Criteria {
    String outerKey;
    String innerKey;
    Object value;
    BinaryOperation operation;
}
