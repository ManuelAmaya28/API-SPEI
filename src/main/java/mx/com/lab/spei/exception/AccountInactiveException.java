package mx.com.lab.spei.exception;

public class AccountInactiveException extends SpeiBusinessException {

    private final String clabe;
    private final String accountStatus;

    public AccountInactiveException(String clabe, String status) {
        super("Savings account for CLABE " + clabe + " is not active (status: " + status + ")");
        this.clabe = clabe;
        this.accountStatus = status;
    }

    public String getClabe() {
        return clabe;
    }

    public String getAccountStatus() {
        return accountStatus;
    }
}
