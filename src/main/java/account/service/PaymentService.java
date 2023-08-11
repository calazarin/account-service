package account.service;

import account.dto.PaymentDetailsDto;
import account.dto.PaymentDto;
import account.entity.AppUser;
import account.entity.Payment;
import account.exception.payment.InvalidPaymentException;
import account.exception.payment.PaymentDoesNotExistException;
import account.repository.PaymentRepository;
import account.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository){
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addNewPayments(List<PaymentDto> paymentDtoList){

        Map<String, List<PaymentDto>> paymentsByUser = new HashMap<>();

        for(PaymentDto paymentDto : paymentDtoList) {
            List<PaymentDto> currentPayments = paymentsByUser.getOrDefault(paymentDto.getEmployee(), new ArrayList<>());
            currentPayments.add(paymentDto);
            paymentsByUser.put(paymentDto.getEmployee(), currentPayments);
        }

        for(Map.Entry<String, List<PaymentDto>> entry : paymentsByUser.entrySet()){

            Optional<AppUser> userOpt = userRepository.findByUsernameIgnoreCase(entry.getKey());
            if(!userOpt.isPresent()) {
                log.error("Not possible to add payments for user {} as it does not exist", entry.getKey());
                throw new InvalidPaymentException("Not possible to add payment as employee does not exist");
            }

            long distinctPeriods = entry.getValue().stream().map(p -> p.getPeriod()).distinct().count();
            if(distinctPeriods != entry.getValue().size()){
                throw new InvalidPaymentException("Impossible to add duplicated payment!");
            }

            AppUser userToUpdate = userOpt.get();
            Set<Payment> newUserPayments = new HashSet<>();
            Set<Payment> currentPaymentSet = userToUpdate.getPayments();

            List<String> allExistentPaymentPeriods = currentPaymentSet
                    .stream().map(p -> p.getPeriod())
                    .collect(Collectors.toList());

            for(PaymentDto paymentDto : entry.getValue()){

                if(allExistentPaymentPeriods.contains(paymentDto.getPeriod())){
                    log.error("Skipping payment for user {} as period '{}' already exists!", entry.getKey(),
                            paymentDto.getPeriod());
                    throw new InvalidPaymentException("Impossible to add duplicated payment!");
                }

                Payment payment = new Payment(paymentDto.getPeriod(), paymentDto.getSalary(), userToUpdate);
                Payment newPayment = paymentRepository.save(payment);
                newUserPayments.add(newPayment);
                log.info("Adding new payment with period '{}' and value {} for user {}",
                        paymentDto.getPeriod(), paymentDto.getSalary(), userToUpdate.getUsername());
            }

            currentPaymentSet.addAll(newUserPayments);
            userToUpdate.setPayments(currentPaymentSet);
            userRepository.save(userToUpdate);
        }

    }

    public List<PaymentDto> updateUserPayment(String userEmail, String period, Long newSalary){
        Optional<Payment> paymentOpt = paymentRepository.findByUserUsernameIgnoreCaseAndPeriod(userEmail, period);
        if(!paymentOpt.isPresent()){
            log.error("Not possible update payment for user {} and period {}!", userEmail, period);
            throw new InvalidPaymentException();
        }

        Payment paymentToBeUpdated = paymentOpt.get();
        paymentToBeUpdated.setSalary(newSalary);
        log.info("Updating payment with period '{}' and salary {} for user {}", period, newSalary, userEmail);
        paymentRepository.save(paymentToBeUpdated);

        return Arrays.asList(PaymentDto.toDto(paymentToBeUpdated));
    }

    private List<PaymentDetailsDto> findPaymentsByUserAndPeriod(String period, String username){
        Optional<Payment> paymentOpt = paymentRepository.findByUserUsernameIgnoreCaseAndPeriod(username, period);
        if(!paymentOpt.isPresent()){
            log.error("Payment given period '{}' and user {} does not exist", period, username);
            throw new PaymentDoesNotExistException();
        }
        Payment retrievedPayment = paymentOpt.get();
        return Arrays.asList(new PaymentDetailsDto(retrievedPayment.getUser().getName(),
                retrievedPayment.getUser().getLastName(),
                formatPeriod(retrievedPayment.getPeriod()),
                formatSalary(retrievedPayment.getSalary())));
    }

    public List<PaymentDetailsDto> findUserPayments(Optional<String> periodOpt, String username){

        if(periodOpt.isPresent()){
          return findPaymentsByUserAndPeriod(periodOpt.get(), username);
        } else {

            List<Payment> paymentList =  paymentRepository.findByUserUsernameIgnoreCase(username);

            if(paymentList.isEmpty()){
                return Arrays.asList(new PaymentDetailsDto());
            }

            return paymentList.stream()
                    .sorted(Comparator.comparing(Payment::getPeriod).reversed())
                    .map(p -> new PaymentDetailsDto(p.getUser().getName(), p.getUser().getLastName(),
                            formatPeriod(p.getPeriod()), formatSalary(p.getSalary())))
                    .collect(Collectors.toList());
        }
    }

    private String formatPeriod(String period){

        String[] periodArr = period.split("-");

        Map<String, String> months = new HashMap<>();
        months.put("01", "January");
        months.put("02", "February");
        months.put("03", "March");
        months.put("04", "April");
        months.put("05", "May");
        months.put("06", "June");
        months.put("07", "July");
        months.put("08", "August");
        months.put("09", "September");
        months.put("10", "October");
        months.put("11", "November");
        months.put("12", "December");

        return months.get(periodArr[0]) + "-" + periodArr[1];
    }


    private String formatSalary(Long salary){
        String dollars = "";
        char[] arr = salary.toString().toCharArray();
        String cents = "";

        for(int i = arr.length - 1; i >= arr.length - 2; i--){
            cents = arr[i] + cents;
        }

        for(int j = 0; j <= arr.length - 3; j++){
            dollars = dollars + arr[j];
        }
        if(dollars == ""){
            dollars = "0";
        }
        String formattedSalary = String.format("%s dollar(s) %s cent(s)", dollars, cents);
        return formattedSalary;
    }

}
