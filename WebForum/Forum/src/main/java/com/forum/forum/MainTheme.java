package com.forum.forum;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MainTheme {

    private String name;
    private List<SubTheme> subThemeList;

}
