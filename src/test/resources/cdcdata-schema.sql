CREATE DATABASE cdcdata WITH LOG;

CREATE TABLE watched_tables (
    table_name varchar(255),
    sequence_value bigint
);

CREATE DATABASE test_database WITH LOG;

CREATE TABLE test_table (
    some_name varchar(50),
    some_value int
);