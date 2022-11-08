package com.hongeee.vacation.api;

import com.hongeee.vacation.api.model.UserResponseDto;
import com.hongeee.vacation.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

  private final UserService userService;

  @GetMapping("/user/id/{userId}")
  public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.findById(userId));
  }

  @GetMapping("/user/email/{email}")
  public ResponseEntity<UserResponseDto> findUserByEmail(@PathVariable String email) {
    return ResponseEntity.ok(userService.findByEmail(email));
  }

  @GetMapping("/users")
  public ResponseEntity<List<UserResponseDto>> findAllUser() {
    return ResponseEntity.ok(userService.findAllUser());
  }

  @DeleteMapping("/user/{userId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId) {
    userService.delete(userId);

    return ResponseEntity.ok().build();
  }
}
