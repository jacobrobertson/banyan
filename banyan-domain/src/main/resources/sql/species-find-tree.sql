select id, latin_name, parent_id, parent_latin_name, boring_final from species.species 
where latin_name in ('Eutheria','Placentalia','Trechnotheria') order by id;

select link, status, type, status_date from species.crawl where link in ('Phiomorpha');
