package com.forum.controller;

import com.forum.forum.MainTheme;
import com.forum.forum.MessageModel;
import com.forum.model.ActiveUserModel;
import com.forum.model.GetMessageModel;
import com.forum.model.MessageForSave;
import com.forum.model.UserModel;
import com.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/forum/getAllMessages")
    public ResponseEntity<List<MessageModel>> getAllMessages(@RequestBody GetMessageModel request) {
        System.out.println(request);
        return ResponseEntity.ok(userService.getAllMessages(request));
    }

    @GetMapping("/forum/getAllThemes")
    public ResponseEntity<List<MainTheme>> getAllThemes() {
        return ResponseEntity.ok(userService.getAllThemes());
    }

    @PostMapping("/forum/getNewMessages")
    public ResponseEntity<List<MessageModel>> getNewMessages(@RequestBody GetMessageModel request) {
        return ResponseEntity.ok(userService.getNewMessages(request));
    }

    @PostMapping("/user/sendMessage")
    public ResponseEntity<MessageModel> saveMessage(@RequestBody MessageForSave model) {
        return ResponseEntity.ok(userService.saveMessage(model));
    }

    @PostMapping("/user/register")
    public ResponseEntity<UserModel> register(@RequestBody UserModel userModel) {
        return ResponseEntity.ok(userService.registerNewUser(userModel));
    }

    @GetMapping("/forum/allUsers")
    public ResponseEntity<List<UserModel>> allUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/forum/activeUsers")
    public ResponseEntity<List<ActiveUserModel>> allActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

}
