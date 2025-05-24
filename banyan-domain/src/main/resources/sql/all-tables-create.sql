CREATE TABLE crawl (
  crawl_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
--  crawl_id int NOT NULL,
  status_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  link char(254) DEFAULT NULL,
  status char(50) DEFAULT NULL,
  crawl_type char(40) DEFAULT NULL
);
CREATE INDEX idx_crawl_link on crawl (link);
CREATE INDEX idx_crawl_id on crawl (crawl_id);

-- drop table example;
CREATE TABLE example (
  example_index int DEFAULT NULL,
  example_id int DEFAULT NULL,
  group_id int DEFAULT NULL,
  simple_name char(50) default NULL,
  caption char(254) DEFAULT NULL,
  terms char(254) DEFAULT NULL,
  crunched_ids char(254) DEFAULT NULL
);


CREATE TABLE example_group (
  index int DEFAULT NULL,
  group_id int DEFAULT NULL,
  caption char(254) DEFAULT NULL
);

CREATE TABLE images (
  entry_id int NOT NULL,
  tiny_width int DEFAULT NULL,
  tiny_height int DEFAULT NULL,
  preview_width int DEFAULT NULL,
  preview_height int DEFAULT NULL,
  detail_width int DEFAULT NULL,
  detail_height int DEFAULT NULL,
  primary key (entry_id)
);
CREATE INDEX idx_images_entry_id on images (entry_id);

CREATE TABLE redirect (
  redirect_from char(254) NOT NULL,
  redirect_to char(254) DEFAULT NULL,
  PRIMARY KEY (redirect_from)
);
CREATE INDEX idx_redirect_redirect_to on redirect (redirect_to);

CREATE TABLE species (
--  id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  id int NOT NULL,
  latin_name char(254) DEFAULT NULL,
  latin_name_clean char(254) DEFAULT NULL,
  latin_name_cleanest char(254) DEFAULT NULL,
  common_name VARCHAR(1023),
  common_name_clean VARCHAR(1023),
  common_name_cleanest VARCHAR(1023),
  parent_id int DEFAULT NULL,
  interesting_parent_id int DEFAULT NULL,
  depicted_id int DEFAULT NULL,
  parent_latin_name char(254) DEFAULT NULL,
  depicted_latin_name char(254) DEFAULT NULL,
  rank int DEFAULT NULL,
  extinct boolean DEFAULT false,
  boring boolean DEFAULT false,
  boring_final boolean DEFAULT false,
  shares_sibling_name boolean DEFAULT false,
  image_link VARCHAR(1023),
  child_count int DEFAULT NULL,
  interesting_child_count int DEFAULT NULL,
  interesting_crunched_ids char(254) DEFAULT NULL,
  linked_image_id int DEFAULT NULL,
  PRIMARY KEY (id)
 );

CREATE INDEX idx_species_parent_latin_name on species (parent_latin_name);
CREATE INDEX idx_species_depicted_latin_name on species (depicted_latin_name);
CREATE INDEX idx_species_latin_name on species (latin_name);
CREATE INDEX idx_species_parent_id on species (parent_id);
CREATE INDEX idx_species_extinct on species (extinct);
CREATE INDEX idx_species_parent_id_extinct on species (parent_id,extinct);
CREATE INDEX idx_species_boring_final on species (boring_final);
CREATE INDEX idx_species_interesting_parent_id_boring_final on species (interesting_parent_id,boring_final);
