SELECT 
s1.latin_name, s1.common_name,
s2.latin_name, s2.common_name
FROM 

species.species S1,
species.species S2

where  S1.parent_id is not null
and S1.parent_id = S2.id;
