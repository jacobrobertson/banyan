SELECT count(*) FROM species.images;

select * from species.images limit 10;

select * from species.species where image_link like '%Herpestes_ichneumon%';

select latin_name from species.species where image_link like '%.ogv.jpg%';
select latin_name from species.species where image_link like '%.ogg';

update species.species set image_link = null where image_link like '%.ogg%';

select * from species.species where common_name like '%Termite%' and parent_id is not null;