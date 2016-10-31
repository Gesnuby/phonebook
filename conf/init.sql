create role phonebook login password 'password' nosuperuser;

-- in linux for lc_collate and lc_ctype use 'ru_RU.UTF-8'
create database phonebook
  with owner phonebook
  encoding = 'utf-8'
  lc_collate = 'Russian_Russia.1251'
  lc_ctype = 'Russian_Russia.1251';
