SELECT count(*) FROM species.species;

SELECT count(*) FROM species.species where parent_latin_name is not null and image_link is not null;
SELECT count(*) FROM species.species where common_name is not null and image_link is not null;

SELECT * FROM species.species where parent_id is null;
SELECT count(*) FROM species.species where parent_id is null;

select * from species.species where image_link is not null limit 1000;
select * from species.species where latin_name = 'Aedes vexans';
select * from species.species where common_name like '%Rats%';
select * from species.species where parent_latin_name = 'Phiomorpha';
select * from species.species where boring_final <> 0 limit 100;
select * from species.species where boring_final <> 0 and child_count > 3 limit 100;

select * from species.species where id < 2;
select * from species.species where extinct = 1;
select * from species.species where rank = 310;

select * from species.species where latin_name in ('Archaea', 'Bacteria', 'Eukaryota', 'Virus');

