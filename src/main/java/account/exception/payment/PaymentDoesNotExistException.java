package account.exception.payment;

public class PaymentDoesNotExistException extends RuntimeException{

    public PaymentDoesNotExistException(){
        super("Payment for given period does not exist");
    }

    public PaymentDoesNotExistException(String msg){
        super(msg);
    }
}
