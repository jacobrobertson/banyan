SELECT count(*) FROM species;
SELECT count(*) FROM images;

select * from species where latin_name in ('Arbor vitae');


select * from species where (rank = -1 or parent_latin_name is null) and (image_link is not null and common_name is not null);

select count(*) from species where parent_latin_name is null; -- self-links! only 4

select common_name, latin_name, image_link from species where common_name is not null and image_link is null;
select count(*) from species where common_name is not null and image_link is null;

select * from species where boring_final = 0 and image_link is null and interesting_child_count = 0;
SELECT count(*) FROM species where parent_latin_name is not null and image_link is not null;

SELECT count(*) FROM species where parent_id is null or parent_id = 0;
SELECT count(*) FROM species where parent_id is null;

SELECT * FROM species where (rank = 0 or rank = -1);

select * from species where id = parent_id limit 1000;
select * from species where image_link is not null limit 1000;
select * from species where latin_name like '%Crithagra buchanani%';
select * from species where latin_name like 'Rattus%';
-- delete from species where latin_name ='Group IV: ssRNA( )';

select * from species where (latin_name like '%virus%' or latin_name like '%Virus%') and image_link is not null;
select * from species where (latin_name like '%virus%' or latin_name like '%Virus%') and common_name is not null;
select count(*) from species where (latin_name like '%virus%' or latin_name like '%Virus%');
select * from species where common_name_clean like '%DRYMOTOX%';
select * from species where parent_latin_name like 'Drymotoxeres pucheranii';
select * from species where boring_final <> 0 limit 100;

-- tree scores
select count(*) from species where boring_final = 0;
select count(*) from species where boring_final = 0 and (parent_id is not null or parent_id <> 0);
SELECT count(*) FROM species where common_name is not null or image_link is not null;
SELECT count(*) FROM species where (common_name is not null or image_link is not null) and parent_id is not null;


select * from species where boring_final <> 0 and child_count > 3 limit 100;

select * from species where id < 2;
select * from species where extinct is null or boring is null or boring_final is null or shares_sibling_name is null;
select count(*) from species where rank = 680;

select * from species where latin_name in ('Archaea', 'Bacteria', 'Eukaryota', 'Virus');

select latin_name from species where image_link like '%Eristalis_tenax_auf_Tragopogon_pratensis%' limit 10000;


select * from species where latin_name = 'Archaea';
select * from species where parent_id = 1;

select * from species where latin_name = 'Animalia' or latin_name = 'Plantae' or id = 6;

select * from species where latin_name in ('Rattus', 'Murinae', 'Chordata', 'Animalia');

select id, common_name, latin_name from species where boring_final = false;
