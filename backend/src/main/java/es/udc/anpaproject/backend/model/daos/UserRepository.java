package es.udc.anpaproject.backend.model.daos;

import java.util.Optional;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.udc.anpaproject.backend.model.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUserName(String userName);

	Slice<User> findByUserNameContainingIgnoreCaseOrderByUserNameAsc(String userName, Pageable pageable);

	Slice<User> findByOrderByUserNameAsc(Pageable pageable);
	
}
