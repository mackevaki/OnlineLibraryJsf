package controllers;

import beans.Book;
import db.Database;
import enums.SearchType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;

import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omnifaces.cdi.Eager;

@Named
@SessionScoped
@Eager
public class SearchController implements Serializable {

    private SearchType searchType;
    private String searchString;
    private ArrayList<Book> currentBookList;
    private static Map<String, SearchType> searchList = new HashMap<>();

    public SearchController() {
        fillBooksAll();
                
        ResourceBundle bundle = ResourceBundle.getBundle("nls.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchList.put(bundle.getString("author_name"), SearchType.AUTHOR);
        searchList.put(bundle.getString("book_name"), SearchType.TITLE);
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public Map<String, SearchType> getSearchList() {
        return searchList;
    }
    
    public void fillBooksByGenre() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        Integer genreId = Integer.valueOf(params.get("genre_id"));
            
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from library.book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where genre_id=" + genreId + " order by b.name ");
    }
    
    public void fillBooksByLetter() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String letter = params.get("letter");
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from library.book b "
                + "inner join author a on  b.author_id = a.id "
                + "inner join genre g on b.genre_id = g.id "
                + "inner join publisher p on b.publisher_id = p.id "
                + "where substr(b.name, 1, 1)='" + letter + "'" + " order by b.name asc");
    }
    
    public void fillBooksBySearch() {
        if (searchString.trim().length() == 0) {
            fillBooksAll();
            return;
        }
        
        StringBuilder sql = new StringBuilder("select b.descr, b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image from library.book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id ");
        
        switch (searchType) {
            case AUTHOR -> {
                sql.append("where lower(a.fio) like '%").append(searchString.toLowerCase()).append("%' order by b.name");
            }
            case TITLE -> {
                sql.append("where lower(b.name) like '%").append(searchString.toLowerCase()).append("%' order by b.name");
            }
        }
        
        fillBooksBySQL(sql.toString());
    }
    
    private void fillBooksAll() {
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, b.descr, "
                + "a.fio as author, g.name as genre, b.image from library.book b inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id inner join publisher p on b.publisher_id=p.id order by b.name");
    }
    
    private void fillBooksBySQL(String sql) {        
        try (Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);) {
            currentBookList = new ArrayList<>(); 
            
            while (res.next()) {                
                Book book = new Book();
                book.setId(res.getLong("id"));
                book.setName(res.getString("name"));
                book.setGenre(res.getString("genre"));
                book.setIsbn(res.getString("isbn"));
                book.setAuthor(res.getString("author"));
                book.setPageCount(res.getInt("page_count"));
                book.setPublishDate(res.getInt("publish_year"));
                book.setPublisher(res.getString("publisher"));
                book.setImage(res.getBytes("image"));
                book.setDescr(res.getString("descr"));
                currentBookList.add(book);
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public static void setSearchList(Map<String, SearchType> searchList) {
        SearchController.searchList = searchList;
    }

    
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public ArrayList<Book> getCurrentBookList() {
        return currentBookList;
    }

    public void setCurrentBookList(ArrayList<Book> currentBookList) {
        this.currentBookList = currentBookList;
    }
    
    public Character[] getRussianLetters() {
        Character[] letters = new Character[33];
        letters[0] = 'А';
        letters[1] = 'Б';
        letters[2] = 'В';
        letters[3] = 'Г';
        letters[4] = 'Д';
        letters[5] = 'Е';
        letters[6] = 'Ё';
        letters[7] = 'Ж';
        letters[8] = 'З';
        letters[9] = 'И';
        letters[10] = 'Й';
        letters[11] = 'К';
        letters[12] = 'Л';
        letters[13] = 'М';
        letters[14] = 'Н';
        letters[15] = 'О';
        letters[16] = 'П';
        letters[17] = 'Р';
        letters[18] = 'С';
        letters[19] = 'Т';
        letters[20] = 'У';
        letters[21] = 'Ф';
        letters[22] = 'Х';
        letters[23] = 'Ц';
        letters[24] = 'Ч';
        letters[25] = 'Ш';
        letters[26] = 'Щ';
        letters[27] = 'Ъ';
        letters[28] = 'Ы';
        letters[29] = 'Ь';
        letters[30] = 'Э';
        letters[31] = 'Ю';
        letters[32] = 'Я';

        return letters;
    }       
}
