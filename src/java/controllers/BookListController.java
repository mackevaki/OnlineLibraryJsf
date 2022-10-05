package controllers;

import beans.Book;
import db.Database;
import enums.SearchType;
import java.io.Serializable;
import java.util.Map;
import java.sql.*;

import jakarta.faces.context.FacesContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.ValueChangeEvent;

import jakarta.inject.Named;
import java.util.ArrayList;
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
    private SearchType searchType;// хранит выбранный тип поиска
    private String searchString; // хранит поисковую строку
    private ArrayList<Book> currentBookList; // текущий список книг для отображения
    private String currentSql;// последний выполнный sql без добавления limit

    public BookListController() {
        fillBooksAll();
    }

    private void fillBooksBySQL(String sql) {
        StringBuilder sqlBuilder = new StringBuilder(sql);

        currentSql = sql;

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

            currentBookList = new ArrayList<Book>();

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

    private void submitValues(Character selectedLetter, long selectedPageNumber, int selectedGenreId, boolean requestFromPager) {
        this.selectedLetter = selectedLetter;
        this.selectedPageNumber = selectedPageNumber;
        this.selectedGenreId = selectedGenreId;
        this.requestFromPager = requestFromPager;
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

        if (searchType == SearchType.AUTHOR) {
            sql.append("where lower(a.fio) like '%").append(searchString.toLowerCase()).append("%' order by b.name ");

        } else if (searchType == SearchType.TITLE) {
            sql.append("where lower(b.name) like '%").append(searchString.toLowerCase()).append("%' order by b.name ");
        }

        fillBooksBySQL(sql.toString());

        return "books";
    }

    public void selectPage() {
        immitateLoading();
        
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        requestFromPager = true;
        fillBooksBySQL(currentSql);
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
        
        switchEditMode();
        return "books";
    }
    
    private boolean editMode;
    
    public boolean isEditMode() {
        return editMode;
    }
    
    public void switchEditMode() {
        editMode = !editMode;
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

   private void fillPageNumbers(long totalBooksCount, int booksCountOnPage) {
        pageNumbers.clear();

        if (totalBooksCount <= 0 ){
            return;
        }
        
        int pageCount = (int)totalBooksCount/booksCountOnPage;
        
        int ord = (int)totalBooksCount % booksCountOnPage;
        
        if (ord>0){
            pageCount += 1 ;
        }
        
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }

    }

    public void searchTypeChanged(ValueChangeEvent e) {
        searchType = (SearchType) e.getNewValue();
    }
    
    public void searchStringChanged(ValueChangeEvent e) {
        searchString = e.getNewValue().toString();
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

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
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
    
    private void immitateLoading() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
