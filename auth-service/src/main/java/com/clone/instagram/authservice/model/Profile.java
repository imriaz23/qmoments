package com.clone.instagram.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {
    private String displayName;
    private String profilePictureUrl;
    private Date birthDay;
    private Set<Address> addresses;
}
