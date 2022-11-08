package com.hongeee.vacation.service;

import com.hongeee.vacation.api.model.UserResponseDto;
import com.hongeee.vacation.domain.User;
import com.hongeee.vacation.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserResponseDto findById(Long id) {
    User user = userRepository.findById(id).orElseThrow(EntityNotFoundException::new);

    return new UserResponseDto(user);
  }

  @Transactional(readOnly = true)
  public UserResponseDto findByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);

    return new UserResponseDto(user);
  }

  @Transactional(readOnly = true)
  public List<UserResponseDto> findAllUser() {
    return userRepository.findAll().stream().map(UserResponseDto::new).collect(Collectors.toList());
  }

  @Transactional
  public void delete(Long id) {
    userRepository.deleteById(id);
  }
}
