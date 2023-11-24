create table if not exists chessgame
(
    id varchar(54) not null primary key,
    whitePlayer text not null,
    blackPlayer text not null,
    startTime timestamp not null,
    endTime timestamp
    );
delete from chessgame;


create table if not exists chessgamemove
(
    id varchar(54) not null,
    executionTime timestamp not null,
    fromPosition varchar(2) not null,
    toPosition varchar(2) not null,
    settling text not null
);

delete from chessgamemove;


DROP TYPE IF EXISTS log_message_type;

CREATE TYPE log_message_type AS ENUM (
    'GAME_STARTED',
    'MOVE_BY_WHITE',
    'MOVE_BY_BLACK',
    'WRONG_MOVE_BY_BLACK',
    'WRONG_MOVE_BY_WHITE',
    'CHECK_BY_WHITE',
    'CHECK_BY_BLACK',
    'CHECK_MATE_BY_WHITE',
    'CHECK_MATE_BY_BLACK'
    );



create table if not exists chessgameinfolog
(
    id varchar(54) not null,
    logTime timestamp not null,
    logMessage text not null,
    logMessageType log_message_type NOT NULL
);

delete from chessgameinfolog;


create table if not exists tokenentry
(
    segment       integer not null,
    processorName varchar(255) not null,
    token         bytea,
    tokenType     varchar(255),
    timestamp     varchar(1000),
    owner         varchar(1000),
    primary key   (processorName, segment)
);
delete from tokenentry;

