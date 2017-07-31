DROP TABLE CompositeIntStringBeanTable;

CREATE TABLE CompositeIntStringBeanTable (
	id INTEGER , 
	name VARCHAR(255) , 
	salary DOUBLE PRECISION NOT NULL, 
	CONSTRAINT pk_CompositeIntStringBeanTabl PRIMARY KEY (id , name)
);

commit;
