package com.hongeee.vacation.repository;

import com.hongeee.vacation.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  @Modifying
  @Query(value = "UPDATE User u SET u.annualDays = :annualDays")
  void updateAnnualDays(double annualDays);
}
