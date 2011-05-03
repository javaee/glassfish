drop table TEAMPLAYER
drop table PLAYER 
drop table TEAM
drop TABLE LEAGUE
go

create table PLAYER 
(
	PLAYER_ID VARCHAR(255) primary key not null, 
	NAME VARCHAR(255),
	POSITION VARCHAR(255), 
	SALARY double precision not null
)

create table LEAGUE 
(
	LEAGUE_ID VARCHAR(255) primary key not null, 
	NAME VARCHAR(255),
	SPORT VARCHAR(255)
)

create table TEAM 
(
	TEAM_ID VARCHAR(255) primary key not null, 
	CITY VARCHAR(255),
	NAME VARCHAR(255),
	LEAGUE_ID VARCHAR(255),
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
