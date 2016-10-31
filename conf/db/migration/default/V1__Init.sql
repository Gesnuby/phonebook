create table records (
  id serial not null,
  username varchar(255) not null,
  phone varchar(255) not null,
  constraint records_pk primary key (id)
);