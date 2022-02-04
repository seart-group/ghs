package usi.si.seart.gseapp.jackson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import usi.si.seart.gseapp.dto.GitRepoDto;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JsonWrapper {
    Map<String, Object> parameters;
    Integer count;
    List<GitRepoDto> items;
}
