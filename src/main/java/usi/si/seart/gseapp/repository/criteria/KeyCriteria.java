package usi.si.seart.gseapp.repository.criteria;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.gseapp.repository.operation.UnaryOperation;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyCriteria implements Criteria {
    String key;
    UnaryOperation operation;
}
