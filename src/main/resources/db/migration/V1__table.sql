CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE if EXISTS votes;
DROP TABLE if EXISTS posts;
DROP TABLE if EXISTS threads;
DROP TABLE if EXISTS users_forum;
DROP TABLE if EXISTS forums;
DROP TABLE if EXISTS users;

CREATE TABLE IF NOT EXISTS "users" (
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
  forumid INTEGER,
  created TIMESTAMP WITH TIME ZONE,
  message TEXT NOT NULL,
  title TEXT NOT NULL,
  votes BIGINT DEFAULT 0,
  FOREIGN KEY (author) REFERENCES "users" (nickname),
  FOREIGN KEY (forum) REFERENCES "forums" (slug)
);

CREATE TABLE IF NOT EXISTS "posts" (
  id       SERIAL NOT NULL PRIMARY KEY,
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

Create TABLE IF NOT EXISTS "users_forum" (
  id SERIAL NOT NULL PRIMARY KEY,
  forumid INTEGER,
  nickname CITEXT COLLATE ucs_basic NOT NULL,
  fullname citext,
  email CITEXT NOT NULL,
  about TEXT
);

CREATE INDEX IF NOT EXISTS posts_path on posts (path);
CREATE INDEX IF NOT EXISTS posts_path1 on posts ((path[1]));
CREATE INDEX IF NOT EXISTS posts_thread on posts (thread);
CREATE INDEX IF NOT EXISTS posts_id on posts (id);
CREATE INDEX IF NOT EXISTS posts_parent on posts (parent);
CREATE INDEX IF NOT EXISTS posts_thread_path_id on posts (thread, path, id);
CREATE INDEX IF NOT EXISTS posts_thread_parent_path on posts (thread, parent, path, id, (path[1]));--getPosts
CREATE INDEX IF NOT EXISTS posts_thread_parent on posts (thread, parent, (path[1]));--getPosts
CREATE INDEX IF NOT EXISTS posts_author on posts (author);
CREATE INDEX IF NOT EXISTS posts_forum on posts (forum);

CREATE INDEX IF NOT EXISTS users_nickname on users (nickname);
CREATE INDEX IF NOT EXISTS users_email on users (email);

CREATE INDEX IF NOT EXISTS threads_slug on threads (slug);
CREATE INDEX IF NOT EXISTS threads_forum on threads (forum);
CREATE INDEX IF NOT EXISTS threads_forum_created on threads (forumid, created);

CREATE INDEX IF NOT EXISTS forums_slug on forums (slug);

CREATE INDEX IF NOT EXISTS votes_nickname_threadid on votes (nickname, threadid);

CREATE UNIQUE INDEX user_forums_forumid_user on users_forum (forumid, nickname);
