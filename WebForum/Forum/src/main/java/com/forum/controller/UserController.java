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

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/forum/getAllMessages/{main}/{sub}")
    public ResponseEntity<List<MessageModel>> getAllMessages(@PathVariable(name = "main") String main, @PathVariable(name = "sub") String sub) {
        return ResponseEntity.ok(userService.getAllMessages(GetMessageModel
                .builder()
                        .mainTheme(main)
                        .subTheme(sub)
                .build()));
    }

    @GetMapping("/forum/getAllThemes")
    public ResponseEntity<List<MainTheme>> getAllThemes() {
        return ResponseEntity.ok(userService.getAllThemes());
    }

    @GetMapping("/forum/getNewMessages/{main}/{sub}")
    public ResponseEntity<List<MessageModel>> getNewMessages(@PathVariable(name = "main") String main, @PathVariable(name = "sub") String sub, @RequestParam(name = "time") String time) {
        return ResponseEntity.ok(userService.getNewMessages(GetMessageModel.builder()
                        .mainTheme(main)
                        .subTheme(sub)
                        .lastSeenTime(LocalDateTime.parse(time))
                .build()));
    }

    @PostMapping("/user/sendMessage")
    public ResponseEntity<MessageModel> saveMessage(@RequestBody MessageForSave model) {
        return ResponseEntity.ok(userService.saveMessage(model));
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
