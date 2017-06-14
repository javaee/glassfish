DROP TRIGGER T_LEAGUE
DROP TRIGGER T_PLAYER 
DROP TRIGGER T_TEAM
go

DROP TABLE TEAMPLAYER
DROP TABLE PLAYER 
DROP TABLE TEAM
DROP TABLE LEAGUE
go

CREATE TABLE PLAYER 
(
	PLAYER_ID VARCHAR(255) primary key not null, 
	NAME VARCHAR(255),
	POSITION VARCHAR(255), 
	SALARY double precision not null,
	VERSION NUMERIC(19) not null
)

create table LEAGUE 
(
	LEAGUE_ID VARCHAR(255) primary key not null, 
	NAME VARCHAR(255),
	SPORT VARCHAR(255),
	VERSION NUMERIC(19) not null
)

create table TEAM 
(
	TEAM_ID VARCHAR(255) primary key not null, 
	CITY VARCHAR(255),
	NAME VARCHAR(255),
	LEAGUE_ID VARCHAR(255),
	VERSION NUMERIC(19) not null,
	foreign key (LEAGUE_ID) references LEAGUE (LEAGUE_ID)
)

create table TEAMPLAYER 
(
	PLAYER_ID VARCHAR(255) not null,
    	TEAM_ID VARCHAR(255) not null,
    	constraint PK_TEAMPLAYER primary key (PLAYER_ID, TEAM_ID),
	foreign key (TEAM_ID) references TEAM (TEAM_ID),
	foreign key (PLAYER_ID) references PLAYER (PLAYER_ID)
)

go

CREATE TRIGGER T_LEAGUE
   ON LEAGUE
   FOR UPDATE as
   begin
       UPDATE LEAGUE
           SET LEAGUE.VERSION = LEAGUE.VERSION + 1
           FROM inserted
       WHERE LEAGUE.LEAGUE_ID = inserted.LEAGUE_ID
           AND LEAGUE.VERSION = inserted.VERSION
   end
go

CREATE TRIGGER T_PLAYER
   ON PLAYER
   FOR UPDATE as
   begin
       UPDATE PLAYER
           SET PLAYER.VERSION = PLAYER.VERSION + 1
           FROM inserted
       WHERE PLAYER.PLAYER_ID = inserted.PLAYER_ID
           AND PLAYER.VERSION = inserted.VERSION
   end
go

CREATE TRIGGER T_TEAM
   ON TEAM
   FOR UPDATE as
   begin
       UPDATE TEAM
           SET TEAM.VERSION = TEAM.VERSION + 1
           FROM inserted
       WHERE TEAM.TEAM_ID = inserted.TEAM_ID
           AND TEAM.VERSION = inserted.VERSION
   end
go
