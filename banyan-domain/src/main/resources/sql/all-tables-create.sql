CREATE TABLE `crawl` (
  `status_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `link` char(255) DEFAULT NULL,
  `status` char(50) DEFAULT NULL,
  `type` char(40) DEFAULT NULL,
  KEY `idx_crawl_link` (`link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `example` (
  `index` int(11) DEFAULT NULL,
  `example_id` int(11) DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `caption` char(255) DEFAULT NULL,
  `terms` char(255) DEFAULT NULL,
  `crunched_ids` char(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `example_group` (
  `index` int(11) DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `caption` char(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `images` (
  `entry_id` int(11) NOT NULL AUTO_INCREMENT,
  `tiny_width` int(11) DEFAULT NULL,
  `tiny_height` int(11) DEFAULT NULL,
  `preview_width` int(11) DEFAULT NULL,
  `preview_height` int(11) DEFAULT NULL,
  `detail_width` int(11) DEFAULT NULL,
  `detail_height` int(11) DEFAULT NULL,
  UNIQUE KEY `idx_entry_id` (`entry_id`)
) ENGINE=InnoDB AUTO_INCREMENT=434957 DEFAULT CHARSET=utf8;
CREATE TABLE `redirect` (
  `redirect_from` char(255) NOT NULL,
  `redirect_to` char(255) DEFAULT NULL,
  PRIMARY KEY (`redirect_from`),
  KEY `idx_redirect_redirect_to` (`redirect_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `species` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latin_name` char(255) DEFAULT NULL,
  `latin_name_clean` char(255) DEFAULT NULL,
  `latin_name_cleanest` char(255) DEFAULT NULL,
  `common_name` text,
  `common_name_clean` text,
  `common_name_cleanest` text,
  `parent_id` int(11) DEFAULT NULL,
  `interesting_parent_id` int(11) DEFAULT NULL,
  `depicted_id` int(11) DEFAULT NULL,
  `parent_latin_name` char(255) DEFAULT NULL,
  `depicted_latin_name` char(255) DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  `extinct` char(1) DEFAULT '0',
  `boring` char(1) DEFAULT '0',
  `boring_final` char(1) DEFAULT '0',
  `shares_sibling_name` char(1) DEFAULT '0',
  `image_link` text,
  `child_count` int(11) DEFAULT NULL,
  `interesting_child_count` int(11) DEFAULT NULL,
  `interesting_crunched_ids` char(255) DEFAULT NULL,
  `linked_image_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_species_id` (`id`),
  KEY `idx_species_parent_latin_name` (`parent_latin_name`),
  KEY `idx_species_depicted_latin_name` (`depicted_latin_name`),
  KEY `idx_species_latin_name` (`latin_name`),
  KEY `idx_species_parent_id` (`parent_id`),
  KEY `idx_species_extinct` (`extinct`),
  KEY `idx_species_parent_id_extinct` (`parent_id`,`extinct`),
  KEY `idx_species_boring_final` (`boring_final`),
  KEY `idx_species_interesting_parent_id_boring_final` (`interesting_parent_id`,`boring_final`)
) ENGINE=InnoDB AUTO_INCREMENT=435233 DEFAULT CHARSET=utf8;
