-- V1__init.sql
create extension if not exists "uuid-ossp";
create table if not exists food_products (
  id uuid primary key default uuid_generate_v4(),
  name text not null,
  unit text not null, -- g/ml
  protein_100g decimal(8,3) not null,
  phe_100g decimal(8,3) not null,
  kcal_100g int not null,
  category text,
  is_active boolean not null default true
);
create index if not exists idx_food_products_name on food_products (name);
