delete from example_group;

insert into example_group
(index, group_id, caption)
values
(0, 1, 'Explore these trees'),
(1, 3, 'Have your heard of these?'),
(2, 4, 'Can you guess which is more closely related...'),
(3, 2, 'Some families you might not have heard of...'),
(4, 5, 'Search page links')
;
delete from example;

insert into example
(example_index, example_id, group_id, simple_name, caption, terms, crunched_ids)
values
-- FAMILIES
(100, 100, 1, 'tree-of-life', 'Tree of Life Overview - animals, plants and fungus', 'Animalia,Fungi,Plantae', ''),
(101, 101, 1, 'mammals', 'Mammals - elephants, tigers and pigs', 'Proboscidea,Panthera tigris,Suidae', ''),
(102, 102, 1, 'insects', 'Insects - crickets, wasps and aphids', 'Gryllinae,Vespoidea,Brevicoryne brassicae', ''),
(103, 103, 1, 'plants', 'Flowering plants - watermelons, waterlilies and nutmeg', 'Citrullus lanatus,Nymphaeaceae,Myristica', ''),
(104, 104, 1, 'sharks', 'Cartilaginous fish - manta rays, sharks and guitarfish', 'Manta,Stegostoma fasciatum,Rhynchobatus djiddensis', ''),

-- OTHER_FAMILIES
(200, 200, 2, 'marsupials', 'Marsupials - koalas, kangeroos and possum', 'Petaurus breviceps,Phascolarctos cinereus,Macropus,Didelphis virginiana', ''),
(201, 201, 2, 'african-mammals', 'Afrotheria - elephants, hyraxes and aardvarks', 'Tubulidentata,Elephantidae,Gomphotheriidae,Mammutidae,Moeritheriidae,Hyracoidea', ''),
(202, 202, 2, 'arthropods', 'Arthropods - spiders, lobsters and centipedes', 'Theraphosidae,Panulirus longipes,Chilopoda', ''),
(203, 203, 2, 'viruses', 'Virus Groups - rabies, ebola and parvo', 'Filoviridae,Rhabdoviridae,Parvoviridae', ''),

-- HAVE_YOU_HEARD_OF
(300, 300, 3, 'pika-rabbits-hares', 'Pika?  They''re related to rabbits and hares', 'Ochotona,Sylvilagus,Lepus', ''),
(301, 301, 3, 'caecilians-worms-amphibians', 'Caecilians?  They look like giant earthworms, but are amphibians like frogs and salamanders', 'Lumbricus terrestris,Scaphiophryne,Chthonerpeton indistinctum,Typhlonectes,Bolitoglossa', ''),
(302, 302, 3, 'pipefish-seahorses-seadragons', 'Ghost Pipefish?  They''re related to seahorses and seadragons', 'Phyllopteryx taeniolatus,Solenostomidae,Corythoichthys schultzi,Hippocampus guttulatus', ''),
(303, 303, 3, 'okapi-giraffes', 'Okapi?  They''re the closes relative to giraffes', 'Okapia,Giraffa tippelskirchi,Giraffa reticulata,Giraffa giraffa', ''),

-- YOU_MIGHT_NOT_KNOW (i.e. which is closer ...)
(401, 401, 4, 'hippo-rhino-pig', '... to a hippo - a rhinocerous or a pig?', 'Hippopotamidae,Rhinocerotidae,Suidae', ''),
(402, 402, 4, 'racoon-cat-dog', '... to a racoon - a cat or a dog?', 'Felis,Canis lupus familiaris,Procyonidae', ''),
(403, 403, 4, 'tomatoe-cucumber-tobacco', '... to a tomatoe - a cucumber or tobacco?', 'Nicotiana tabacum,Cucumis,Lycopersicum esculentum', ''), 
(404, 404, 4, 'ant-wasp-termite', '... to an ant - a wasp or a termite?', 'Vespidae,Formicidae,Isoptera', ''),

-- SEARCH PAGE LINKS
-- note that some of these are the same as the example groups, but I kept them separate so I could change them independently if I wanted to
(501, 501, 5, 'tree-of-life-search', 'Tree of Life Overview - animals, plants and fungus', 'Animalia,Fungi,Plantae', ''),
(502, 502, 5, 'insects-search', 'Insects - crickets, wasps and aphids', 'Gryllinae,Vespoidea,Brevicoryne brassicae', ''),
(503, 503, 5, 'marsupials-searc', 'Marsupials - koalas, kangeroos and possum', 'Petaurus breviceps,Phascolarctos cinereus,Macropus,Didelphis virginiana', ''), 
(504, 504, 5, 'example-searches', 'Example searches - "bears", "viola arvensis", "dwarf marsh violet", "vulpes"', 'Ursidae,Viola arvensis,Viola epipsila,Vulpes', ''),
--- ALL ABOVE this line no longer being used...

(505, 505, 5, 'welcome-to-banyan', 'Welcome to Banyan!', 'Bryophyta,Magnoliopsida,Selachimorpha,Crocodylidae,Orthoptera,Canidae,Scyllaridae', '')
;
