package usi.si.seart.gseapp.util.interval;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@FieldDefaults(level = AccessLevel.PROTECTED)
public class LongInterval extends Interval<Long>{
}
