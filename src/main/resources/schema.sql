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
    'MOVE',
    'WRONG_MOVE',
    'CHECK',
    'CHECK_MATE',
    'SHORT_CASTLING',
    'LONG_CASTLING',
    'WRONG_SHORT_CASTLING',
    'WRONG_LONG_CASTLING'
);



DROP TYPE IF EXISTS piece_color;

CREATE TYPE piece_color AS ENUM(
    'BLACK', 'WHITE'
);

DROP TYPE IF EXISTS game_state;

CREATE TYPE game_state AS ENUM(
    'CREATED', 'STARTED', 'PAUSED', 'ENDED'
);


DROP TYPE IF EXISTS actor_type;

CREATE TYPE actor_type AS ENUM (
    'WHITE_PLAYER','BLACK_PLAYER','SYSTEM'
);

DROP TYPE IF EXISTS log_level;

CREATE TYPE log_level AS ENUM (
  'INFO','WARN','ERROR'
);


create table if not exists chessgameinfolog
(
    gameId varchar(54) not null,
    logTime timestamp not null,
    logMessage text not null,
    causedBy actor_type not null,
    logMessageType log_message_type NOT NULL,
    logLevel log_level NOT NULL
);

delete from chessgameinfolog;

create table if not exists chess_game_state
(
    gameId varchar(54) not null primary key,
    gameState game_state not null,
    currentTurn piece_color,
    turnNumber int not null,
    castlingShortStillPossibleByWhite boolean not null,
    castlingLongStillPossibleByWhite boolean not null,
    castlingShortStillPossibleByBlack boolean not null,
    castlingLongStillPossibleByBlack boolean not null,
    settling text not null
);


delete from chess_game_state;


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

