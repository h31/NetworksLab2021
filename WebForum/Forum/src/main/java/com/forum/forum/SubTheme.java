package com.forum.forum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubTheme {

    private String name;
    @JsonIgnore
    private List<MessageModel> messageModelList;

}
