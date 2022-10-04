package validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.ResourceBundle;

@FacesValidator("validators.LoginValidator")
public class LoginValidator implements Validator {

    @Override
    public void validate(FacesContext fc, UIComponent uic, Object t) throws ValidatorException {
        ResourceBundle bundle = ResourceBundle.getBundle("nls.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        String newValue = t.toString();
        
        try {
            if (newValue.length() < 5) {
                throw new IllegalArgumentException(bundle.getString("login_length_error"));
            }
            if (!Character.isLetter(newValue.charAt(0))) {
                throw new IllegalArgumentException(bundle.getString("first_letter_error"));
            }
            if (getTestArray().contains(newValue)) {
                throw new IllegalArgumentException(bundle.getString("used_name"));
            }
        } catch (IllegalArgumentException ex) {
            FacesMessage message = new FacesMessage(ex.getMessage());
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
    
    private ArrayList getTestArray() {
        ArrayList<String> list = new ArrayList<>();
        list.add("username");
        list.add("login");
        return list;
    }
}