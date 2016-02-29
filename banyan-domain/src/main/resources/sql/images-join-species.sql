select IMG.entry_id, SPEC.id, SPEC.latin_name, SPEC.image_link from 

species.species SPEC
left join species.images IMG

on IMG.entry_id = SPEC.id

where IMG.entry_id is null and SPEC.image_link is not null

-- and tiny_width = null

limit 1000
;