package account.repository;

import account.entity.AppUser;
import account.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role>  findByNameIgnoreCase(String name);

}
