DROP TABLE CompositeIntStringBeanTable;

CREATE TABLE CompositeIntStringBeanTable (
	id INTEGER, 
	name VARCHAR(127), 
	salary DOUBLE PRECISION NOT NULL , 
	PRIMARY KEY (id, name)
);

commit;
