SELECT count(*) FROM species.species;

SELECT count(*) FROM species.species where parent_latin_name is not null and image_link is not null;
SELECT count(*) FROM species.species where common_name is not null and image_link is not null;

SELECT * FROM species.species where parent_id is null;
SELECT count(*) FROM species.species where parent_id is null;

select * from species.species where image_link is not null limit 1000;
select * from species.species where latin_name = 'Avialae';
select * from species.species where common_name_clean like '%OWLS%';
select * from species.species where parent_latin_name = 'Tree of Life';
select * from species.species where boring_final <> 0 limit 100;
select count(*) from species.species where boring_final = 0;
select * from species.species where boring_final <> 0 and child_count > 3 limit 100;

select * from species.species where id < 2;
select * from species.species where extinct is null or boring is null or boring_final is null or shares_sibling_name is null;
select * from species.species where rank = 310;

select * from species.species where latin_name in ('Archaea', 'Bacteria', 'Eukaryota', 'Virus');

