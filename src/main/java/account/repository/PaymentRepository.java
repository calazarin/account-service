package account.repository;

import account.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Override
    Optional<Payment> findById(Long aLong);

    Optional<Payment> findByUserUsernameIgnoreCaseAndPeriod(String username, String period);

    List<Payment> findByUserUsernameIgnoreCase(String username);
}
