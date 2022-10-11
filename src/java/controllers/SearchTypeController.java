package controllers;

import enums.SearchType;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Named(value = "searchTypeController")
@RequestScoped
public class SearchTypeController {
    private Map<String, SearchType> searchList = new HashMap<>(); // хранит все виды поисков (по автору, по названию)

    public SearchTypeController() {
        ResourceBundle bundle = ResourceBundle.getBundle("nls.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchList.put(bundle.getString("author_name"), SearchType.AUTHOR);
        searchList.put(bundle.getString("book_name"), SearchType.TITLE);        
    }
    
    public Map<String, SearchType> getSearchList() {
        return searchList;
    }    
}
