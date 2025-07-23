import mysql.connector
from mysql.connector import errorcode
from fastapi import HTTPException
import os
DB_NAME="notes_db"
TABLES= {
    "users":"""CREATE TABLE IF NOT EXISTS users(
    id INT AUTO_INCREMENT PRIMARY KEY,
    username varchar(255) UNIQUE NOT NULL,
    password varchar(255))""",
    "notes":"""CREATE TABLE IF NOT EXISTS notes(
    id INT AUTO_INCREMENT PRIMARY KEY,
    title varchar(255) NOT NULL,
    note TEXT,
    user_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
    )"""
}
def database_check():
    try:
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="parthsarth9541"
        )
        cursor = conn.cursor()
        cursor.execute(f"CREATE DATABASE IF NOT EXISTS {DB_NAME}")
        conn.commit()
        cursor.close()
        conn.close()
    except mysql.connector.Error as e:
        raise HTTPException(status_code=500, detail=f"Error creating database: {str(e)}")
def tables_check(conn):
    cursor = conn.cursor()
    for table_name, ddl in TABLES.items():
        try:
            cursor.execute(ddl)
        except mysql.connector.Error as err:
            raise HTTPException(status_code=500, detail=f"Error creating table {table_name}: {str(err)}")
def get_connection():
    try:
        database_check()
        conn = mysql.connector.connect(
            host="localhost",
            user="root",
            password="parthsarth9541",
            database=DB_NAME
        )
        tables_check(conn)
        return conn
    except mysql.connector.Error as e:
        raise HTTPException(status_code=500, detail=str(e))