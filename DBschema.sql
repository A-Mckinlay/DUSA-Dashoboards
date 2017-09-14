CREATE SEQUENCE disbursals_id_seq;

CREATE TABLE disbursals
(
  id              SERIAL        NOT NULL
    CONSTRAINT disbursals_pkey
    PRIMARY KEY,
  datetime        TIMESTAMP     NOT NULL,
  outletref       INTEGER       NOT NULL,
  userid          TEXT          NOT NULL,
  transactiontype INTEGER       NOT NULL,
  cashspent       NUMERIC(8, 2) NOT NULL,
  discountamount  NUMERIC(8, 2) NOT NULL,
  totalamount     NUMERIC(8, 2) NOT NULL
);

CREATE UNIQUE INDEX disbursals_id_uindex
  ON disbursals (id);

CREATE TABLE users
(
  username  TEXT                  NOT NULL
    CONSTRAINT users_pkey
    PRIMARY KEY,
  password  TEXT                  NOT NULL,
  email     TEXT                  NOT NULL,
  isadmin   BOOLEAN DEFAULT FALSE NOT NULL,
  firstname TEXT                  NOT NULL,
  lastname  TEXT                  NOT NULL
);

CREATE UNIQUE INDEX users_username_uindex
  ON users (username);

CREATE TABLE outlets
(
  outletref  INTEGER NOT NULL
    CONSTRAINT outlets_pkey
    PRIMARY KEY,
  outletname TEXT    NOT NULL
);

CREATE UNIQUE INDEX outlets_outletref_uindex
  ON outlets (outletref);

CREATE UNIQUE INDEX outlets_outletname_uindex
  ON outlets (outletname);

COMMENT ON TABLE outlets IS 'Joining Table';

ALTER TABLE disbursals
  ADD CONSTRAINT disbursals_fk_outlets
FOREIGN KEY (outletref) REFERENCES outlets;

CREATE TABLE transactiontypes
(
  transactionid   INTEGER NOT NULL
    CONSTRAINT transactiontypes_pkey
    PRIMARY KEY,
  transactiontype TEXT    NOT NULL
);

CREATE UNIQUE INDEX transactiontypes_transactionid_uindex
  ON transactiontypes (transactionid);

CREATE UNIQUE INDEX transactiontypes_transactiontype_uindex
  ON transactiontypes (transactiontype);

ALTER TABLE disbursals
  ADD CONSTRAINT disbursals_fk_transactiontypes
FOREIGN KEY (transactiontype) REFERENCES transactiontypes;


INSERT INTO transactiontypes (transactionid, transactiontype) VALUES (0, 'Payment');
INSERT INTO transactiontypes (transactionid, transactiontype) VALUES (1, 'Redemption');
INSERT INTO transactiontypes (transactionid, transactiontype) VALUES (2, 'Reversal');