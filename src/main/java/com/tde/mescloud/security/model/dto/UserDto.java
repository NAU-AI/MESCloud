package com.tde.mescloud.security.model.dto;

import com.tde.mescloud.security.role.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Role role;


}
