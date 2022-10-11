package controllers;

import beans.Book;
import db.Database;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named(value = "imageController")
@SessionScoped
public class ImageController implements Serializable {

    public ImageController() {
    }    
    
    public byte[] getImage(int id) {
        byte[] image = null;
        
        try(Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet res = stmt.executeQuery("select image from library.book b where b.id=" + id)) {
            while(res.next()) {
                image = res.getBytes("image");
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return image;
    }
    
    
    public byte[] getContent(int id) {
        byte[] content = null;
        try(Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select content from book where id=" + id)) {
            while (rs.next()) {
                content = rs.getBytes("content");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }
}
