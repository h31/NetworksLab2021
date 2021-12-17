package com.forum.repo;

import com.forum.forum.Forum;
import com.forum.forum.MainTheme;
import com.forum.forum.SubTheme;
import com.forum.forum.MessageModel;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class ForumRepository {

    private Forum forum;

    public ForumRepository() {
        initData();
    }

    public void initData() {
        forum = new Forum();
        forum.setMainThemeList(initMainThemes());
    }

    private List<MainTheme> initMainThemes() {
        List<MainTheme> result = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            result.add(MainTheme
                    .builder()
                    .subThemeList(initSubThemes())
                    .name(generateRandomString())
                    .build());
        }
        return result;
    }

    private List<SubTheme> initSubThemes() {
        List<SubTheme> result = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            result.add(SubTheme
                    .builder()
                            .messageModelList(initMessages())
                            .name(generateRandomString())
                    .build());
        }
        return result;
    }

    private List<MessageModel> initMessages() {
        List<MessageModel> result = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 10; i++) {
            result.add(MessageModel
                    .builder()
                            .message(generateRandomString())
                            .userName(generateRandomString())
                            .dateTime(LocalDateTime.now())
                    .build());
        }
        return result;
    }

    //copy-paste from: https://www.baeldung.com/java-random-string
    private String generateRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        int targetStringLength = random.nextInt(30) + 5;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString;
    }

    public List<MainTheme> getAllThemes() {
        return forum.getMainThemeList();
    }

    public MainTheme getMainThemeByName(String name) {
        return findMainByName(name);
    }

    public SubTheme getSubThemeByName(String mainThemeName, String subThemeName) {
        return findSubByName(subThemeName, findMainByName(mainThemeName));
    }

    public List<MessageModel> getAllMessages(String mainThemeName, String subThemeName) {
        return findSubByName(subThemeName, findMainByName(mainThemeName)).getMessageModelList();
    }

    public List<MessageModel> getNewestMessages(String mainThemeName, String subThemeName, LocalDateTime lastSeenTime) {
        return findSubByName(subThemeName, findMainByName(mainThemeName)).getMessageModelList()
                .stream()
                .filter(x -> x.getDateTime().isAfter(lastSeenTime))
                .collect(Collectors.toList());
    }

    private MainTheme findMainByName(String name) {
        List<MainTheme> list = forum.getMainThemeList()
                .stream()
                .filter(x -> x.getName().equals(name))
                .collect(Collectors.toList());
        if(list.size() == 0) {
            throw new IllegalArgumentException("Main theme wasn't found: " + name);
        }
        return list.get(0);
    }

    private SubTheme findSubByName(String name, MainTheme theme) {
        List<SubTheme> list = theme.getSubThemeList()
                .stream()
                .filter(x -> x.getName().equals(name))
                .collect(Collectors.toList());
        if(list.size() == 0) {
            throw new IllegalArgumentException("Main theme wasn't found: " + name);
        }
        return list.get(0);
    }

}
