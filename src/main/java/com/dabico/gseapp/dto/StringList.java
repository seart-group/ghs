package com.dabico.gseapp.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class StringList {
    @Builder.Default
    List<String> items = new ArrayList<>();
}
