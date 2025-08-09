CREATE TABLE example (
  example_index INT DEFAULT NULL,
  example_id INT DEFAULT NULL,
  group_id INT DEFAULT NULL,
  simple_name char(50) default NULL,
  caption char(254) DEFAULT NULL,
  terms char(254) DEFAULT NULL,
  crunched_ids char(254) DEFAULT NULL
);

CREATE TABLE example_group (
  index INT DEFAULT NULL,
  group_id INT DEFAULT NULL,
  caption char(254) DEFAULT NULL
);

CREATE TABLE common_taxons (
  taxon_id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  
  latin_name char(254) DEFAULT NULL,
  common_name CHAR(254),
  rank char(25) DEFAULT NULL,
  extinct boolean DEFAULT false,
  image_file_path VARCHAR(254),
  parent_taxon_id INT DEFAULT NULL,
  
  latin_name_clean char(254) DEFAULT NULL,
  latin_name_cleanest char(254) DEFAULT NULL,
  common_name_clean CHAR(254),
  common_name_cleanest CHAR(254),  
  
  interesting_parent_taxon_id INT DEFAULT NULL,
  boring boolean DEFAULT false,
  boring_final boolean DEFAULT false,
  shares_sibling_name boolean DEFAULT false,
  child_count INT DEFAULT NULL,
  interesting_child_count INT DEFAULT NULL,
  interesting_downstream_crunched_ids char(254) DEFAULT NULL,
  interesting_upstream_crunched_ids char(254) DEFAULT NULL,
  
  tiny_width INT DEFAULT NULL,
  tiny_height INT DEFAULT NULL,
  preview_width INT DEFAULT NULL,
  preview_height INT DEFAULT NULL,
  detail_width INT DEFAULT NULL,
  detail_height INT DEFAULT NULL,

  PRIMARY KEY (taxon_id)
 );

CREATE INDEX idx_common_taxons_latin_name on common_taxons (latin_name);
CREATE INDEX idx_common_taxons_parent_id on common_taxons (parent_taxon_id);
CREATE INDEX idx_common_taxons_extinct on common_taxons (extinct);
CREATE INDEX idx_common_taxons_parent_id_extinct on common_taxons (parent_taxon_id, extinct);
CREATE INDEX idx_common_taxons_boring_final on common_taxons (boring_final);
CREATE INDEX idx_common_taxons_interesting_parent_id_boring_final on common_taxons (interesting_parent_taxon_id, boring_final);
