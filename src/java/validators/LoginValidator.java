package validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.util.ResourceBundle;

@FacesValidator("validators.LoginValidator")
public class LoginValidator implements Validator {

    @Override
    public void validate(FacesContext fc, UIComponent uic, Object t) throws ValidatorException {
        if (t.toString().length() < 5) {
            ResourceBundle bundle = ResourceBundle.getBundle("nls.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
            FacesMessage message = new FacesMessage(bundle.getString("login_length_error"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
}