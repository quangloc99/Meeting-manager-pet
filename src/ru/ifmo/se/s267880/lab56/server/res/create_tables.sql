DO $$
BEGIN
  IF NOT EXISTS (select 1 from pg_type where typname = 'meeting_collection_sort_order') THEN
    CREATE TYPE meeting_collection_sort_order AS ENUM (
      'asc-name', 'des-name', 'asc-time', 'des-time'
    );
  END IF;
END
$$;

CREATE TABLE IF NOT EXISTS users (
  id serial PRIMARY KEY,
  email text UNIQUE,
  password_hash text,
  zone_id text
);

CREATE TABLE IF NOT EXISTS collections (
  id serial PRIMARY KEY,
  name VARCHAR(255) UNIQUE,    -- just like linux file system
  sort_order meeting_collection_sort_order default 'asc-time',
  owner_id serial REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS meetings (
  id serial PRIMARY KEY,
  name text,
  time timestamp,
  duration integer CHECK (duration >= 0),
  location_building integer,
  location_floor integer,
  collection_id serial REFERENCES collections (id)
);
