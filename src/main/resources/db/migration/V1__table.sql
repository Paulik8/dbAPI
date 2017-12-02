CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE "users" (
  id SERIAL NOT NULL PRIMARY KEY,
  nickname CITEXT COLLATE ucs_basic NOT NULL UNIQUE,
  fullname citext,
  email CITEXT NOT NULL UNIQUE,
  about TEXT
);

CREATE TABLE IF NOT EXISTS "forums" (
  id SERIAL NOT NULL PRIMARY KEY,
  title CITEXT NOT NULL,
  creator citext NOT NULL,
  slug CITEXT NOT NULL UNIQUE,
  posts INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  FOREIGN KEY (creator) REFERENCES "users" (nickname)
);

CREATE TABLE IF NOT EXISTS "threads" (
  id SERIAL NOT NULL PRIMARY KEY,
  slug CITEXT UNIQUE,
  author CITEXT NOT NULL,
  forum CITEXT,
  created TIMESTAMP WITH TIME ZONE,
  message TEXT NOT NULL,
  title TEXT NOT NULL,
  votes BIGINT DEFAULT 0,
  FOREIGN KEY (author) REFERENCES "users" (nickname),
  FOREIGN KEY (forum) REFERENCES "forums" (slug)
);

CREATE TABLE IF NOT EXISTS "posts" (
  id       SERIAL PRIMARY KEY,
  parent   INTEGER DEFAULT 0,
  author   CITEXT REFERENCES users (nickname),
  message  TEXT,
  isedited BOOLEAN,
  forum    CITEXT REFERENCES forums (slug),
  created  TIMESTAMP WITH TIME ZONE,
  thread INTEGER REFERENCES threads (id),
  path INT ARRAY
);

CREATE TABLE IF NOT EXISTS  "votes" (
  id SERIAL PRIMARY KEY,
  voice INTEGER DEFAULT 0,
  nickname citext,
  threadid INTEGER REFERENCES threads(id),
  FOREIGN KEY (nickname) REFERENCES "users" (nickname)
);



