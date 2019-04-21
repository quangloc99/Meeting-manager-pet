DO $$
BEGIN
  IF NOT EXISTS (select 1 from pg_type where typname = 'meeting_collection_sort_order') THEN
    CREATE TYPE meeting_collection_sort_order AS ENUM (
      'asc-name', 'des-name', 'asc-time', 'des-time'
    );
  END IF;
END
$$;

CREATE TABLE IF NOT EXISTS collections (
  id serial PRIMARY KEY,
  sort_order meeting_collection_sort_order default 'asc-time'
);

CREATE TABLE IF NOT EXISTS meetings (
  id serial PRIMARY KEY,
  name text,
  time timestamp with time zone,
  duration integer CHECK (duration >= 0),
  location_building integer,
  location_floor integer,
  collection_id serial REFERENCES collections (id)
);
