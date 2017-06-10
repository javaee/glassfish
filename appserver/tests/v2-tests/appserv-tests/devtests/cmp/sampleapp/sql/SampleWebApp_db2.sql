DROP TABLE CompositeIntStringBeanTable;

CREATE TABLE CompositeIntStringBeanTable (
	id INTEGER NOT NULL, 
	name VARCHAR(255) NOT NULL, 
	salary DOUBLE PRECISION NOT NULL, 
	CONSTRAINT pk_CompIntStrTab PRIMARY KEY (id, name)
);

commit;
