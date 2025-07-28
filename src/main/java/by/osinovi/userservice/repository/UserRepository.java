package by.osinovi.userservice.repository;

import by.osinovi.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findById(Long id);

    Optional<User> findUserByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE id in :ids", nativeQuery = true)
    List<User> findUserByIdIn(@Param("ids") List<Long> ids);
}
