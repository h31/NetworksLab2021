package com.forum.service;

import com.forum.forum.MainTheme;
import com.forum.forum.MessageModel;
import com.forum.model.ActiveUserModel;
import com.forum.model.GetMessageModel;
import com.forum.model.MessageForSave;
import com.forum.model.UserModel;
import com.forum.repo.ForumRepository;
import com.forum.repo.UserRepository;
import com.forum.security.CustomWebSecurityConfigurerAdapter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class UserService {

    @Autowired
    ForumRepository forumRepository;

    @Autowired
    UserRepository userRepository;

    public List<MessageModel> getAllMessages(GetMessageModel getMessageModel) {
        return forumRepository.getAllMessages(getMessageModel.getMainTheme(), getMessageModel.getSubTheme());
    }

    public List<MessageModel> getNewMessages(GetMessageModel getMessageModel) {
        return forumRepository.getNewestMessages(getMessageModel.getMainTheme(), getMessageModel.getSubTheme(), getMessageModel.getLastSeenTime());
    }

    public List<MainTheme> getAllThemes() {
        return forumRepository.getAllThemes();
    }

    public MessageModel  saveMessage(MessageForSave messageModel) {
        userRepository.updateActivity(messageModel.getUserName());
        MessageModel model = MessageModel
                .builder()
                .message(messageModel.getMessage())
                .userName(messageModel.getUserName())
                .dateTime(LocalDateTime.now())
                .build();
        forumRepository
                .getSubThemeByName(messageModel.getMainThemeName(), messageModel.getSubThemeName())
                .getMessageModelList().add(model);
        return model;
    }

    public List<UserModel> getAllUsers() {
        return userRepository.getAllRegistered();
    }

    public List<ActiveUserModel> getActiveUsers() {
        return userRepository.getAllActive();
    }
}
