DROP TABLE TeamPlayer ;
DROP TABLE Player ;
DROP TABLE Team ;
DROP TABLE League ;

commit;

CREATE TABLE Player 
(
	player_Id VARCHAR(255) PRIMARY KEY, 
	name VARCHAR(255), 
	position VARCHAR(255), 
	salary DOUBLE PRECISION NOT NULL
);

commit;

CREATE TABLE League 
(
	league_Id VARCHAR(255) PRIMARY KEY, 
	name VARCHAR(255), 
	sport VARCHAR(255)
);

commit;

CREATE TABLE Team 
(
	team_Id VARCHAR(255) PRIMARY KEY, 
	city VARCHAR(255), 
	name VARCHAR(255),
	league_Id VARCHAR(255),
    CONSTRAINT FK_1 FOREIGN KEY (league_Id)   REFERENCES League (league_Id)
);

commit;

CREATE TABLE TeamPlayer 
(
	player_Id VARCHAR(255), 
	team_Id VARCHAR(255), 
	CONSTRAINT FK_2 FOREIGN KEY (team_Id)   REFERENCES Team (team_Id),
	CONSTRAINT FK_3 FOREIGN KEY (player_Id)   REFERENCES Player (player_Id)
);

commit;

quit;
