package controllers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class LoginController {

    public LoginController() {
    }

    public String login() {
        return "books";
    }

}
