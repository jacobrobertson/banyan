select * from species.species where (rank = -1 or parent_latin_name is null) and (image_link is not null and common_name is not null);