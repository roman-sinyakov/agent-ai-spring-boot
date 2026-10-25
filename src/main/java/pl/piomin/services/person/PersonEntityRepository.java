package pl.piomin.services.person;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonEntityRepository extends JpaRepository<PersonEntity, String> {

    boolean existsByEmailIgnoreCaseAndIdNot(String email, String id);

    List<PersonEntity> findAllByOrderByLastNameAscFirstNameAsc();
}
