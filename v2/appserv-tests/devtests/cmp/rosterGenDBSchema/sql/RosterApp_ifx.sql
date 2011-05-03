DROP TABLE TeamPlayer ;
DROP TABLE Player ;
DROP TABLE Team ;
DROP TABLE League ;

CREATE TABLE Player 
(
	player_Id VARCHAR(127) PRIMARY KEY, 
	name VARCHAR(127), 
	position VARCHAR(127), 
	salary DOUBLE PRECISION NOT NULL 
);

CREATE TABLE League 
(
	league_Id VARCHAR(127) PRIMARY KEY, 
	name VARCHAR(127), 
	sport VARCHAR(127)
);

CREATE TABLE Team 
(
	team_Id VARCHAR(127) PRIMARY KEY, 
	city VARCHAR(127), 
	name VARCHAR(127),
	league_Id VARCHAR(127),
	FOREIGN KEY (league_Id)   REFERENCES League (league_Id)
);

CREATE TABLE TeamPlayer 
(
	player_Id VARCHAR(127), 
        team_Id VARCHAR(127), 
        PRIMARY KEY (player_Id, team_Id),
	FOREIGN KEY (team_Id)   REFERENCES Team (team_Id),
	FOREIGN KEY (player_Id)   REFERENCES Player (player_Id)
);

commit;

quit;
