package controllers;

import beans.Genre;
import db.Database;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omnifaces.cdi.Eager;

@Named
@ApplicationScoped
@Eager
public class GenreController implements Serializable {
    private ArrayList<Genre> genreList; //= new ArrayList<>();
    
    public GenreController() {
        fillAllGenres();
    }
    
    private ArrayList<Genre> fillAllGenres() {
        genreList = new ArrayList<>();
        try (Connection conn = Database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery("select * from library.genre order by name");) {
            while (res.next()) {
                Genre genre = new Genre();
                genre.setName(res.getString("name"));
                genre.setId(res.getLong("id"));
                genreList.add(genre);
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return genreList;
    }
    
    public ArrayList<Genre> getGenreList() {
//        if (!genreList.isEmpty()) {
//            return genreList;
//        } else {
//            return fillAllGenres();
//        }
        return genreList;
    }
}
