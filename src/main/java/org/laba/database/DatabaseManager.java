package org.laba.database;

import org.laba.database.errors.WrongFieldException;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class DatabaseManager {
    private Connection conn;
    public DatabaseManager() throws SQLException {
        this("guest","guest");
    }
    public DatabaseManager(String username, String password) throws SQLException {
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library",username,password);
    }
    public void create_db() throws SQLException {
        PreparedStatement cs = conn.prepareStatement("call create_db()");
        cs.executeUpdate();
    }
    public void drop_db() throws SQLException {
        PreparedStatement cs = conn.prepareStatement("call drop_db()");
        cs.executeUpdate();
    }
    public void clear_db() throws SQLException {
        PreparedStatement cs = conn.prepareStatement("call clear_table()");
        cs.executeUpdate();
    }
    public void insert_book(String ISBN, String book_name, String description,String publication_date, String author_name) throws WrongFieldException, SQLException {
        validateCortege(ISBN,book_name,description,publication_date,author_name);
        PreparedStatement cs = conn.prepareStatement("CALL insert_book(?,?,?,?::Date,?)");
        cs.setString(1,ISBN);
        cs.setString(2,book_name);
        cs.setString(3,description);
        cs.setDate(4, java.sql.Date.valueOf(publication_date));
        cs.setString(5,author_name);
        cs.execute();
    }
    public String search_books_by_author_name(String author_name) throws SQLException, WrongFieldException {
        if(author_name.length()>255) throw new WrongFieldException("author_name should have no more than 255 symbols");
        CallableStatement cs = conn.prepareCall("{call search_books_by_author_name(?)}");
        cs.setString(1,author_name);

        ResultSet rs = cs.executeQuery();
        StringBuilder res=new StringBuilder();
        while (rs.next()){
            res.append(rs.getInt("id")).append(" ").append(rs.getString("isbn")).append(" ")
                    .append(rs.getString("name")).append(" ").append(rs.getString("description"))
                    .append(" ").append(rs.getDate("publication_date")).append("\n");
        }
        return res.toString();
    }
    public void update_book_by_id(int book_id,String ISBN, String book_name, String description,String publication_date, String author_name) throws WrongFieldException, SQLException {
        validateCortege(ISBN,book_name,description,publication_date,author_name);
        if(book_id<0) throw new WrongFieldException("id must be non negative");
        PreparedStatement cs = conn.prepareStatement("CALL update_book(?,?,?,?,?,?)");
        cs.setInt(1,book_id);
        cs.setString(2,ISBN);
        cs.setString(3,book_name);
        cs.setString(4,description);
        try {
            cs.setDate(5,new java.sql.Date( new SimpleDateFormat("yyyy-MM-dd").parse(publication_date).getTime()));
        }catch (ParseException _){}
        cs.setString(6,author_name);
        cs.executeUpdate();
    }
    public void delete_book_by_name(String book_name) throws WrongFieldException, SQLException {
        if(book_name.length()>255) throw new WrongFieldException("book_name should be consist of not more than 255 symbols");
        PreparedStatement cs = conn.prepareStatement("call delete_book_by_name(?)");
        cs.setString(1,book_name);
        cs.executeUpdate();
    }
    public void create_user(String login, String password, HashMap<String,Boolean> rights) throws SQLException {
        PreparedStatement cs = conn.prepareStatement("call create_user(?,?)");
        cs.setString(1,login);
        cs.setString(2, password);
        cs.executeUpdate();
        cs.clearParameters();
        for (String right: rights.keySet()) {
            if(rights.get(right)){
                cs = Objects.equals(right, "search_books_by_author_name") ?  conn.prepareStatement("call grant_user_on_function(?,?)") : conn.prepareStatement("call grant_user_on_procedure(?,?)");
                cs.setString(1,right);
                cs.setString(2,login);
                cs.executeUpdate();
                cs.clearParameters();
            }
        }
    }
    private void validateCortege(String ISBN, String book_name, String description, String publication_date, String author_name) throws WrongFieldException {
        if(ISBN.length() != 13) throw new WrongFieldException("ISBN must have 13 symbols");
        if(book_name.length()>255) throw new WrongFieldException("book_name should have no more than 255 symbols");
        if(!validateDateFormat(publication_date)) throw new WrongFieldException("publication_date format should be yyyy-MM-dd");
        if(author_name.length()>255) throw new WrongFieldException("author_name should have no more than 255 symbols");
    }
    private boolean validateDateFormat(String input) {
        if(input == null) {
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.parse(input);
            return true;
        } catch(ParseException e) {
            return false;
        }
    }
}
