-- try to figure out why most species are showing up as boring

select 
*
-- id, latin_name, common_name, parent_latin_name, parent_id 
from species.species where 
parent_latin_name in ('Vertebrata');

select * from species.species where parent_id in (417371);

select id, latin_name, common_name, parent_latin_name, parent_id from species.species where 
latin_name in 
('Echimyidae', 'Octodontoidea', 'Caviomorpha', 'Hystricognathi', 'Hystricomorpha', 
'Rodentia', 'Euarchontoglires', 'Boreoeutheria', 'Placentalia', 'Eutheria', 'Theria', 'Zatheria', 'Trechnotheria', 'Mammalia',
'Eukaryota',
'Animalia',
'Chordata',
'Chordata Craniata',
'Vertebrata',
'Gnathostomata (Vertebrata)',
'Gnathostomata',
'Tetrapoda',
'Reptiliomorpha',
'Amniota',
'Synapsida',
'Eupelycosauria',
'Sphenacodontia',
'Sphenacodontoidea',
'Therapsida',
'Theriodontia',
'Cynodontia',
'Eucynodontia',
'Probainognathia',
'Prozostrodontia',
'Mammaliaformes'
) 
order by latin_name;

-- researching specific pages that look like something's wrong

select * from species.species where 
-- latin_name in ('Ciliophrys'); --  or parent_latin_name in ('Pedinellaceae', 'Heterokonta');
-- latin_name in ('Dicyphus seleucus'); --  or parent_latin_name in ('Rhagium');
latin_name like 'Centropogon%';


select * from species.species where 
latin_name in ('Katablepharidophyceae'); 


-- this should in theory never return any results
SELECT * FROM species.species where (parent_id is null or parent_id = 0);


SELECT * FROM species.species where id = 0;

select * from species.crawl where link like 'Centropogon%';