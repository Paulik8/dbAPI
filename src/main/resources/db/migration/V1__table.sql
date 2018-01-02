CREATE EXTENSION IF NOT EXISTS citext;

-- DROP TABLE votes;
-- DROP TABLE posts;
-- DROP TABLE threads;
-- DROP TABLE forums;
-- DROP TABLE users;


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

CREATE INDEX IF NOT EXISTS posts_path on posts (path);
CREATE INDEX IF NOT EXISTS posts_thread_path on posts (thread, path);
CREATE INDEX IF NOT EXISTS posts_thread_parent_path on posts (thread, path, parent);
CREATE INDEX IF NOT EXISTS posts_author on posts (lower(author));
CREATE INDEX IF NOT EXISTS posts_forum on posts (lower(forum));
CREATE INDEX IF NOT EXISTS posts_forum_author on posts (lower(forum), lower(author));
CREATE INDEX IF NOT EXISTS posts_thread on posts (thread);

CREATE INDEX IF NOT EXISTS users_nickname on users (lower(nickname));

CREATE INDEX IF NOT EXISTS threads_slug on threads (lower(slug));
-- CREATE INDEX IF NOT EXISTS threads_forum on threads (lower(forum));
-- CREATE INDEX IF NOT EXISTS threads_forum_created on threads (lower(forum), created);
-- CREATE INDEX IF NOT EXISTS threads_forum_author on threads (lower(forum), lower(author));
CREATE INDEX IF NOT EXISTS threads_votes on threads (votes);

CREATE INDEX IF NOT EXISTS forums_slug on forums (lower(slug), posts);
CREATE INDEX IF NOT EXISTS forums_threads on forums (lower(slug));

CREATE INDEX IF NOT EXISTS votes_nickname_threadid on votes (lower(nickname), threadid);
CREATE INDEX IF NOT EXISTS votes_nickname_threadid_voice on votes (nickname, voice, threadid);


-- select * from posts where thread = 81 and path[1] =  ANY(select id from posts where parent = 0 and thread = 81 order by id);


