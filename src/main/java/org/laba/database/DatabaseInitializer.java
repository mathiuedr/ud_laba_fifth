package org.laba.database;
import java.sql.*;

public class DatabaseInitializer {
    public static void init(){
        // Подключение к PostgreSQL (без указания базы данных)
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", "postgres", "123");
             Statement stmt = conn.createStatement()) {

            // Создание базы данных
            if(!databaseExists(conn,"library")){
                stmt.executeUpdate("CREATE DATABASE library");
                System.out.println("Database 'library' created successfully.");
            }



        } catch (SQLException e) {
            System.err.println("Error creating database: " + e);
        }

        // Подключение к созданной базе данных
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "postgres", "123");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE create_db ()\n" +
                    "LANGUAGE plpgsql AS $$\n" +
                    "BEGIN\n" +
                    "CREATE TABLE Books (\n" +
                    "                            id SERIAL PRIMARY KEY,\n" +
                    "                            ISBN VARCHAR(13) UNIQUE NOT NULL,\n" +
                    "                            name VARCHAR(255) NOT NULL,\n" +
                    "                            description TEXT,\n" +
                    "                            publication_date DATE,\n" +
                    "                            author_name VARCHAR(255));\n" +
                    "END;\n" +
                    "$$ "+"SECURITY DEFINER\n");
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE drop_db()\n" +
                    "LANGUAGE plpgsql AS $$\n" +
                    "BEGIN\n" +
                    "DROP TABLE books;\n" +
                    "END;\n" +
                    "$$ "+ "SECURITY DEFINER\n");
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE clear_table()\n" +
                    "LANGUAGE plpgsql\n" +
                    "SECURITY DEFINER\n" +
                    "AS $$\n" +
                    "BEGIN\n" +
                    "TRUNCATE TABLE books;\n" +
                    "END;\n" +
                    "$$;");
            // Создание таблицы Books
            if(!tableExists(conn,"books")){
                try {
                    PreparedStatement cs = conn.prepareStatement("call create_db()");
                    cs.executeUpdate();

                    System.out.println("Table 'Books' created successfully.");
                }catch (SQLException e ){
                    System.out.println("GOIDA "+ e);
                }

            }


            // Создание хранимой процедуры для добавления книги
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE insert_book(" +
                    "new_isbn varchar(13), new_name varchar(255), new_description TEXT, new_publication_date DATE, new_author_name varchar(255)) " +
                    "LANGUAGE plpgsql " +
                    "SECURITY DEFINER\n" +
                    "AS $$ " +
                    "BEGIN " +
                    "INSERT INTO \"books\" (ISBN, name, description, publication_date, author_name) " +
                    "VALUES (new_isbn, new_name, new_description, new_publication_date, new_author_name); " +
                    "END; $$");

            //System.out.println("Stored procedure 'insert_book' created successfully.");

            // Создание функции для поиска книги по названию
            stmt.executeUpdate("CREATE OR REPLACE FUNCTION search_books_by_author_name(search_name TEXT)\n" +
                    "RETURNS TABLE(id INT, isbn varchar(13), name varchar(255), description TEXT, publication_date DATE, authosr_name varchar(255))\n" +
                    "LANGUAGE plpgsql\n" +
                    "SECURITY DEFINER\n" +
                    "AS $$\n" +
                    "BEGIN\n" +
                    "    RETURN QUERY\n" +
                    "    SELECT * FROM \"books\" WHERE author_name LIKE search_name;\n" +
                    "END;\n" +
                    "$$;");

            //System.out.println("Function 'search_books_by_name' created successfully.");

            // Создание процедуры для обновления книги
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE update_book(book_id INT, new_ISBN VARCHAR(13), new_name VARCHAR(255), new_description TEXT, new_publication_date DATE, new_author_name VARCHAR(255) )" +
                    "LANGUAGE plpgsql " +
                    "SECURITY DEFINER\n" +
                    "AS $$ " +
                    "BEGIN " +
                    "UPDATE \"books\" SET isbn = new_ISBN, name = new_name, description = new_description, publication_date = new_publication_date, author_name = new_author_name WHERE id = book_id; " +
                    "END; $$");

            //System.out.println("Stored procedure 'update_book' created successfully.");

            // Создание процедуры для удаления книги по названию
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE delete_book_by_name(book_name VARCHAR(255) ) " +
                    "LANGUAGE plpgsql " +
                    "SECURITY DEFINER\n" +
                    "AS $$ " +
                    "BEGIN " +
                    "DELETE FROM \"books\" WHERE name = book_name; " +
                    "END; $$");
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE grant_user_on_procedure(procedure_name TEXT, user_name TEXT ) " +
                    "LANGUAGE plpgsql " +
                    "SECURITY DEFINER\n" +
                    "AS $$ " +
                    "BEGIN " +
                    "EXECUTE format('GRANT EXECUTE ON PROCEDURE %I TO %I', procedure_name, user_name);" +
                    "END; $$");
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE grant_user_on_function(procedure_name TEXT, user_name TEXT ) " +
                    "LANGUAGE plpgsql " +
                    "SECURITY DEFINER\n" +
                    "AS $$ " +
                    "BEGIN " +
                    "EXECUTE format('GRANT EXECUTE ON FUNCTION %I TO %I', procedure_name, user_name);" +
                    "END; $$");
            stmt.executeUpdate("CREATE OR REPLACE PROCEDURE create_user(user_name TEXT, new_password TEXT )\n" +
                    "                    LANGUAGE plpgsql\n" +
                    "                    SECURITY DEFINER\n" +
                    "                    AS $$\n" +
                    "                    BEGIN\n" +
                    "                    EXECUTE format('CREATE USER %I WITH PASSWORD %L', user_name, new_password);\n" +
                    "                    EXECUTE format('GRANT CONNECT ON DATABASE library TO %I', user_name);\n" +
                    "EXECUTE format('GRANT USAGE ON SCHEMA public TO %I', user_name);  \n" +
                    "                    END; $$");

            //System.out.println("Stored procedure 'delete_book_by_name' created successfully.");
            if(!userExists(conn,"admin")){
                stmt.executeUpdate( "CREATE USER admin WITH PASSWORD '123'");
            }
            if(!userExists(conn,"guest")){
                stmt.executeUpdate( "CREATE USER guest WITH PASSWORD 'guest'");
            }
            /*REVOKE ALL PRIVILEGES ON TABLE books FROM myuser;
            REVOKE ALL PRIVILEGES ON SCHEMA public FROM myuser;
            REVOKE CREATE ON SCHEMA public FROM myuser;

            -- Выдача права на использование схемы
            GRANT USAGE ON SCHEMA public TO myuser;*/
            stmt.executeUpdate("REVOKE ALL PRIVILEGES ON TABLE books FROM guest;");
            stmt.executeUpdate("REVOKE ALL PRIVILEGES ON SCHEMA public FROM guest;");
            stmt.executeUpdate("REVOKE CREATE ON SCHEMA public FROM guest;");
            stmt.executeUpdate("REVOKE ALL PRIVILEGES ON TABLE books FROM admin;");
            stmt.executeUpdate("REVOKE ALL PRIVILEGES ON SCHEMA public FROM admin;");
            stmt.executeUpdate("REVOKE CREATE ON SCHEMA public FROM admin;");
            //stmt.executeUpdate("GRANT USAGE ON SCHEMA public TO guest;");

            stmt.executeUpdate("REVOKE ALL ON PROCEDURE create_db FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON PROCEDURE clear_table FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON PROCEDURE drop_db FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON PROCEDURE insert_book FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON PROCEDURE update_book FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON PROCEDURE delete_book_by_name FROM PUBLIC;");
            stmt.executeUpdate("REVOKE ALL ON FUNCTION search_books_by_author_name FROM PUBLIC;");
            stmt.executeUpdate("GRANT CONNECT ON DATABASE library TO admin");
            stmt.executeUpdate("GRANT CONNECT ON DATABASE library TO guest");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE create_db TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE clear_table TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE drop_db TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE insert_book TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE update_book TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON PROCEDURE delete_book_by_name TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON FUNCTION search_books_by_author_name TO admin");
            stmt.executeUpdate("GRANT EXECUTE ON FUNCTION search_books_by_author_name TO guest");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e);
        }
    }
    private static boolean userExists(Connection conn, String username) throws SQLException {
        String sql = "SELECT 1 FROM pg_roles WHERE rolname = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Если есть результат, пользователь существует
            }
        }
    }
    private static boolean databaseExists(Connection conn, String dbName) throws SQLException {
        String sql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dbName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Если есть результат, база данных существует
            }
        }
    }
    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Если есть результат, таблица существует
            }
        }
    }
}
