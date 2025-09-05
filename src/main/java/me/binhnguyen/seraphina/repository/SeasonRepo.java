package me.binhnguyen.seraphina.repository;

import jakarta.persistence.Persistence;
import me.binhnguyen.seraphina.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonRepo extends JpaRepository<Season, Persistence> {

  @Query("SELECT s FROM Season s WHERE s.year = :year")
  Season getByYear(int year);
}
