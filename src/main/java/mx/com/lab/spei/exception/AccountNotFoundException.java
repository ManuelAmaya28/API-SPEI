package mx.com.lab.spei.exception;

public class AccountNotFoundException extends SpeiBusinessException {

    private final String clabe;

    public AccountNotFoundException(String clabe) {
        super("No active savings account found for CLABE: " + clabe);
        this.clabe = clabe;
    }

    public String getClabe() {
        return clabe;
    }
}
