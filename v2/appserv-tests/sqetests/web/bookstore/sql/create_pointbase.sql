DROP TABLE books;

CREATE TABLE books
(id VARCHAR(8),
surname VARCHAR(24),
first_name VARCHAR(24),
title VARCHAR(96),
price FLOAT,
onSale BOOLEAN,
year INT,
description VARCHAR(30),
inventory INT);


INSERT INTO books VALUES('201', 'Duke', '',
 'My Early Years: Growing up on *7',
 30.75, true, 1995, 'What a cool book.', 20);

INSERT INTO books VALUES('202', 'Jeeves', '',
 'Web Servers for Fun and Profit', 40.75, false,
 2000, 'What a cool book.', 20);

INSERT INTO books VALUES('203', 'Masterson', 'Webster',
 'Web Components for Web Developers',
 27.75, true, 2000, 'What a cool book.', 20);

INSERT INTO books VALUES('205', 'Novation', 'Kevin',
 'From Oak to Java: The Revolution of a Language',
 10.75, false, 1998, 'What a cool book.', 20);

INSERT INTO books VALUES('206', 'Gosling', 'James',
 'Java Intermediate Bytecodes', 30.95, false,
 2000, 'What a cool book.', 20);

INSERT INTO books VALUES('207', 'Thrilled', 'Ben',
 'The Green Project: Programming for Consumer Devices',
 30.00, false, 1998, 'What a cool book', 20);

INSERT INTO books VALUES('208', 'Tru', 'Itzal',
 'Duke: A Biography of the Java Evangelist',
 45.00, true, 2001, 'What a cool book.', 20);

SELECT * FROM BOOKS;

