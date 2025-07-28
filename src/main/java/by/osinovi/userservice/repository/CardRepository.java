package by.osinovi.userservice.repository;

import by.osinovi.userservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
    Optional<Card> findById(Long id);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId")
    List<Card> findCardsByUserId(@Param("userId") Long userId);

    boolean existsByNumber(String number);
}
