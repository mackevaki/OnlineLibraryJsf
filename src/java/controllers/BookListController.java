package controllers;

import beans.Book;
import db.Database;
import enums.SearchType;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.omnifaces.cdi.Eager;

@Named("bookListController")
@SessionScoped
@Eager
public class BookListController implements Serializable {

    private boolean requestFromPager;
    private int booksOnPage = 2;
    private int selectedGenreId; // выбранный жанр
    private char selectedLetter; // выбранная буква алфавита
    private long selectedPageNumber = 1; // выбранный номер страницы в постраничной навигации
    private long totalBooksCount; // общее кол-во книг (не на текущей странице, а всего), для постраничности
    private ArrayList<Integer> pageNumbers = new ArrayList<Integer>();
    private SearchType selectedSearchType = SearchType.TITLE;// хранит выбранный тип поиска
    private String searchString; // хранит поисковую строку
    private ArrayList<Book> currentBookList; // текущий список книг для отображения
    private String currentSqlNoLimit;// последний выполнный sql без добавления limit
    private boolean editModeView; // отображение режима редактирования
    
    public BookListController() {
        fillBooksAll();
    }
    
    private void submitValues(Character selectedLetter, long selectedPageNumber, int selectedGenreId, boolean requestFromPager) {
        this.selectedLetter = selectedLetter;
        this.selectedPageNumber = selectedPageNumber;
        this.selectedGenreId = selectedGenreId;
        this.requestFromPager = requestFromPager;
    }
    
//<editor-fold defaultstate="collapsed" desc="queries to BD">
    private void fillBooksBySQL(String sql) {
        StringBuilder sqlBuilder = new StringBuilder(sql);
        
        currentSqlNoLimit = sql;
        
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            
            System.out.println(requestFromPager);
            if (!requestFromPager) {
                rs = stmt.executeQuery(sqlBuilder.toString());
                rs.last();
                totalBooksCount = rs.getRow();
                
                fillPageNumbers(totalBooksCount, booksOnPage);
            }
            
            if (totalBooksCount > booksOnPage) {
                sqlBuilder.append(" limit ").append(selectedPageNumber * booksOnPage - booksOnPage).append(",").append(booksOnPage);
            }
            
            rs = stmt.executeQuery(sqlBuilder.toString());
            
            currentBookList = new ArrayList<>();
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setName(rs.getString("name"));
                book.setGenre(rs.getString("genre"));
                book.setIsbn(rs.getString("isbn"));
                book.setAuthor(rs.getString("author"));
                book.setPageCount(rs.getInt("page_count"));
                book.setPublishDate(rs.getInt("publish_year"));
                book.setPublisher(rs.getString("publisher"));
//              book.setImage(rs.getBytes("image"));
//              book.setContent(rs.getBytes("content"));
                book.setDescr(rs.getString("descr"));
                currentBookList.add(book);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void fillBooksAll() {
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, b.descr, "
                + "a.fio as author, g.name as genre, b.image from book b inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id inner join publisher p on b.publisher_id=p.id order by b.name");
    }

    public String fillBooksByGenre() {
        immitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        
        submitValues(' ', 1, Integer.valueOf(params.get("genre_id")), false);
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where genre_id=" + selectedGenreId + " order by b.name ");
        
        return "books";
    }
    
    public String fillBooksByLetter() {
        immitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);
        
        submitValues(selectedLetter, 1, -1, false);
        
        
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id "
                + "where substr(b.name,1,1)='" + selectedLetter + "' order by b.name ");
        
        return "books";
    }
    
    public String fillBooksBySearch() {
        immitateLoading();
        
        submitValues(' ', 1, -1, false);
        
        if (searchString.trim().length() == 0) {
            fillBooksAll();
            return "books";
        }
        
        StringBuilder sql = new StringBuilder("select b.descr, b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image from book b "
                + "inner join author a on b.author_id=a.id "
                + "inner join genre g on b.genre_id=g.id "
                + "inner join publisher p on b.publisher_id=p.id ");
        
        if (selectedSearchType == SearchType.AUTHOR) {
            sql.append("where lower(a.fio) like '%").append(searchString.toLowerCase()).append("%' order by b.name ");
            
        } else if (selectedSearchType == SearchType.TITLE) {
            sql.append("where lower(b.name) like '%").append(searchString.toLowerCase()).append("%' order by b.name ");
        }
        
        fillBooksBySQL(sql.toString());
        
        return "books";
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
    
    public String updateBooks() {
        immitateLoading();
        
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement("update book set name=? isbn=? page_count=? publish_year=? descr=? where id=?");) {
            for (Book book : currentBookList) {
                if (!book.isEdit()) { continue; }
                
                stmt.setString(1, book.getName());
                stmt.setString(2, book.getIsbn());
                stmt.setInt(3, book.getPageCount());
                stmt.setInt(4, book.getPublishDate());
                stmt.setString(5, book.getDescr());
                stmt.setLong(6, book.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        cancelEdit();
        return "books";
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="edition mode">
    

    public void showEdit() {
        editModeView = true;
    }
        
    public void cancelEdit() {
        editModeView = false;
        for (Book b : currentBookList) {
            b.setEdit(false);
        }
    }
//</editor-fold>
    
    public Character[] getRussianLetters() {
        Character[] letters = new Character[]{'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я'};
        return letters;
    }
//<editor-fold defaultstate="collapsed" desc="pager">
    
    public void selectPage() {
        cancelEdit();
        immitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        requestFromPager = true;
        fillBooksBySQL(currentSqlNoLimit);
    }
    
    private void fillPageNumbers(long totalBooksCount, int booksCountOnPage) {
        if (totalBooksCount <= 0 || booksCountOnPage == 0){
            return;
        }
        
        int pageCount = (int)totalBooksCount/booksCountOnPage;
        int ord = (int)totalBooksCount % booksCountOnPage;
        
        if (ord > 0){
            pageCount += 1 ;
        }
        
        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }
    }
    
    public void booksOnPageChanged(ValueChangeEvent e) {
        immitateLoading();
        cancelEdit();
        booksOnPage = Integer.parseInt(e.getNewValue().toString());
        selectedPageNumber = 1;
        requestFromPager = false;
        fillBooksBySQL(currentSqlNoLimit);
    }
//</editor-fold>
   
    public void searchTypeChanged(ValueChangeEvent e) {
        selectedSearchType = (SearchType) e.getNewValue();
    }
    
    public void searchStringChanged(ValueChangeEvent e) {
        searchString = e.getNewValue().toString();
    }
    
//<editor-fold defaultstate="collapsed" desc="getters and setters">
    public boolean isEditModeView() {
        return editModeView;
    }
    
    public ArrayList<Integer> getPageNumbers() {
        return pageNumbers;
    }
    
    public void setPageNumbers(ArrayList<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }
    
    public String getSearchString() {
        return searchString;
    }
    
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }
    
    public SearchType getSelectedSearchType() {
        return selectedSearchType;
    }
    
    public void setSelectedSearchType(SearchType selectedSearchType) {
        this.selectedSearchType = selectedSearchType;
    }
    
    public ArrayList<Book> getCurrentBookList() {
        return currentBookList;
    }
    
    public void setTotalBooksCount(long booksCount) {
        this.totalBooksCount = booksCount;
    }
    
    public long getTotalBooksCount() {
        return totalBooksCount;
    }
    
    public int getSelectedGenreId() {
        return selectedGenreId;
    }
    
    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }
    
    public char getSelectedLetter() {
        return selectedLetter;
    }
    
    public void setSelectedLetter(char selectedLetter) {
        this.selectedLetter = selectedLetter;
    }
    
    public int getBooksOnPage() {
        return booksOnPage;
    }
    
    public void setBooksOnPage(int booksOnPage) {
        this.booksOnPage = booksOnPage;
    }
    
    public void setSelectedPageNumber(long selectedPageNumber) {
        this.selectedPageNumber = selectedPageNumber;
    }
    
    public long getSelectedPageNumber() {
        return selectedPageNumber;
    }
//</editor-fold>
    
    private void immitateLoading() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
