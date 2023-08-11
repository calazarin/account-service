package account.exception.payment;

public class InvalidPaymentException extends RuntimeException{

    public InvalidPaymentException(){
        super("Not possible to add or update payment; please double check if user exists and/or input data is correct!");
    }

    public InvalidPaymentException(String msg){
        super(msg);
    }
}
