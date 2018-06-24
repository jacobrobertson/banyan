delete from example_group;

insert into example_group
(index, group_id, caption)
values
(0, 1, 'Explore these trees'),
(1, 3, 'Have your heard of these?'),
(2, 4, 'Can you guess which is more closely related...'),
(3, 2, 'Some families you might not have heard of...'),
(4, 5, 'Others (not on example page)')
;
delete from example;

insert into example
(example_index, example_id, group_id, simple_name, caption, terms, crunched_ids)
values
-- FAMILIES
(101, 101, 1, 'mammals', 'Mammals / elephants, tigers and pigs', 'Proboscidea,@Panthera tigris,Suidae', ''),
(102, 102, 1, 'insects', 'Insects / crickets, wasps and aphids', '@Gryllinae,Vespoidea,Brevicoryne brassicae', ''),
(103, 103, 1, 'plants', 'Flowering plants / watermelons, waterlilies and nutmeg', '@Citrullus lanatus,Nymphaeaceae,Myristica', ''),
(104, 104, 1, 'sharks', 'Cartilaginous fish / manta rays, sharks and guitarfish', '@Manta,Stegostoma fasciatum,Rhynchobatus djiddensis', ''),

-- OTHER_FAMILIES
(200, 200, 2, 'marsupials', 'Marsupials / koalas, kangeroos and possum', 'Petaurus breviceps,Phascolarctos cinereus,@Macropus,Didelphis virginiana', ''),
(201, 201, 2, 'african-mammals', 'Afrotheria / elephants, hyraxes and aardvarks', 'Tubulidentata,@Elephantidae,Hyracoidea,Sirenia,Tenrecomorpha', ''),
(202, 202, 2, 'arthropods', 'Arthropods / spiders, lobsters and centipedes', 'Theraphosidae,Panulirus longipes,@Chilopoda', ''),
(203, 203, 2, 'viruses', 'Virus Groups / rabies, ebola and parvo', '@Filoviridae,Rabies lyssavirus,Parvoviridae', ''),

-- HAVE_YOU_HEARD_OF
(300, 300, 3, 'pika-rabbits-hares', 'Pika? / They''re related to rabbits and hares', 'Ochotona,@Sylvilagus,Lepus', ''),
(301, 301, 3, 'caecilians-worms-amphibians', 'Caecilians? / They look like giant earthworms, / but are amphibians like frogs and salamanders', 'Lumbricus terrestris,Scaphiophryne,Chthonerpeton indistinctum,Typhlonectes,@Bolitoglossa mexicana', ''),
(302, 302, 3, 'pipefish-seahorses-seadragons', 'Ghost Pipefish? / They''re related to seahorses / and seadragons', '@Phyllopteryx taeniolatus,Solenostomidae,Corythoichthys schultzi,Hippocampus guttulatus', ''),
(303, 303, 3, 'okapi-giraffes', 'Okapi? / They''re the closes relative to giraffes', '@Okapia,Giraffa tippelskirchi,Giraffa reticulata,Giraffa giraffa', ''),

-- YOU_MIGHT_NOT_KNOW (i.e. which is closer ...)
(401, 401, 4, 'hippo-rhino-pig', '... to a hippo / a rhinocerous or a pig?', '@Hippopotamidae,Rhinocerotidae,Suidae', ''),
(402, 402, 4, 'racoon-cat-dog', '... to a racoon / a cat or a dog?', 'Felis,Canis lupus familiaris,@Procyonidae', ''),
(403, 403, 4, 'tomatoe-cucumber-tobacco', '... to a tomatoe / a cucumber or tobacco?', 'Nicotiana tabacum,@Cucumis,Lycopersicum esculentum', ''), 
(404, 404, 4, 'ant-wasp-termite', '... to an ant / a wasp or a termite?', 'Vespidae,@Formicidae,Isoptera', ''),

-- main page default tree
(505, 505, 5, 'welcome-to-banyan', 'Welcome to Banyan!', 'Bryophyta,Magnoliopsida,Selachimorpha,Crocodylidae,Orthoptera,Canidae,Scyllaridae', '')
;
