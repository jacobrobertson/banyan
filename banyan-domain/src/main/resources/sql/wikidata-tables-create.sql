DROP TABLE wd_taxons;
CREATE TABLE wd_taxons (
  qid char(11) DEFAULT NULL,
  latin_name char(254) DEFAULT NULL,
  parent_qid char(11) DEFAULT NULL,
  rank int DEFAULT NULL,
  extinct boolean DEFAULT false,
  common_name VARCHAR(1023)
);
CREATE UNIQUE INDEX wd_taxons_qid on wd_taxons (qid);
CREATE INDEX wd_taxons_parent_qid on wd_taxons (parent_qid);
CREATE INDEX wd_taxons_latin_name on wd_taxons (latin_name);

DROP TABLE wd_images;
CREATE TABLE wd_images (
  qid char(11) DEFAULT NULL,
  image_link VARCHAR(1023),
  depicts_qid char(11)
);
CREATE INDEX wd_images_qid on wd_images (qid);
CREATE INDEX wd_images_depicts_qid on wd_images (depicts_qid);

CREATE TABLE wd_non_taxons (
  qid char(11) DEFAULT NULL
);
CREATE UNIQUE INDEX wd_non_taxons_qid on wd_non_taxons (qid);
