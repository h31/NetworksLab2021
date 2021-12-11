package com.example.delservice.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "t_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // @Size(min = 2, message = "Логин должен быть больше 2-х знаков")
//    @NotBlank(message = "Поле не может быть пустым или состоять из пробельных символов")
    private String username;

   // @Size(min = 2, message = "Пароль должен быть больше 2-х знаков")
  //  @NotBlank(message = "Поле не может быть пустым или состоять из пробельных символов")
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserRole> userRoles;

    public User() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Long getId() {
        return id;
    }

    public Set<UserRole> getUserRoles() {
        return userRoles;
    }
}