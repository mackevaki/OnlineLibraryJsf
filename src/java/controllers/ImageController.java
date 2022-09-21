package controllers;

import db.Database;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
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
            Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return image;
    }
}
