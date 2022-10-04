package com.clone.instagram.authservice.payload;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {

  @NotBlank
  @Size(min = 3, max = 40)
  private String name;

  @NotBlank
  @Size(min = 3, max = 16)
  private String username;

  @NotBlank
  @Email
  @Size(max = 60)
  private String email;

  @NotBlank
  @Size(min = 6, max = 20)
  private String password;

}
