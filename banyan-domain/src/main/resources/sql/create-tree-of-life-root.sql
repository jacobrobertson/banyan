INSERT INTO `species`.`species`
(`id`,
`latin_name`,
`latin_name_clean`,
`latin_name_cleanest`,
`common_name`,
`common_name_clean`,
`common_name_cleanest`,
`parent_id`,
`interesting_parent_id`,
`depicted_id`,
`parent_latin_name`,
`depicted_latin_name`,
`rank`,
`extinct`,
`boring`,
`boring_final`,
`shares_sibling_name`,
`image_link`,
`child_count`,
`interesting_child_count`,
`interesting_crunched_ids`,
`linked_image_id`)
VALUES
(
0,
'Tree of Life',
'Tree of Life',
'Tree of Life',
'Tree of Life',
'Tree of Life',
'Tree of Life',
null,
null,
null,
null,
null,
10, -- "Cladus" is generic
0,
0,
0,
null,
'thumb/7/7f/31-Velvet_Worm.JPG/250px-31-Velvet_Worm.JPG', -- TODO, one possibility is Ficus racemosa once that gets crawled - maybe for now just put whatever
null,
null,
null,
null
);
-- this is not needed - as it is already hard-coded
-- update species.species set parent_id = 0 where latin_name in ('Archaea', 'Bacteria', 'Eukaryota', 'Virus');
