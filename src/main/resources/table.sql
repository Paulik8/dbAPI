
CREATE TABLE user (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname citext NOT NULL UNIQUE,
  fullname CITEXT,
  email CITEXT NOT NULL UNIQUE,
  about TEXT
);