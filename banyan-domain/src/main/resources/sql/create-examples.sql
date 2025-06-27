
/*
 * ! means not pinned
 * $ means the image shown in the example page (only one can be shown)
 */

delete from example_group WHERE group_id < 100;

insert into example_group
(index, group_id, caption)
values
(0, 1, 'Explore these trees'),
(1, 3, 'Have your heard of these?'),
(2, 4, 'Can you guess which is more closely related...'),
(3, 2, 'Some families you might not have heard of...'),
(4, 5, 'Others (not on example page)')
;

delete from example WHERE example_id < 1000;
insert into example
(example_index, example_id, group_id, simple_name, caption, terms, crunched_ids)
VALUES
/* FAMILIES */
(101, 101, 1, 'mammals', 'Mammals / elephants, tigers and pigs', 'Proboscidea,$Panthera tigris,Chiroptera,Neoceti,Placentalia,Scrotifera,Suidae,Ferungulata', ''),
(102, 102, 1, 'insects', 'Insects / grasshoppers, wasps and aphids', '!Mantodea,!Blattodea,!Coleoptera,Vespoidea,Anisoptera,Tuberolachnus salignus,Polyneoptera,Neoptera,Holometabola,Eumetabola,$Orthoptera', ''),
(103, 103, 1, 'plants', 'Flowering plants / apples, waterlilies and cactus', 'Magnoliopsida,Malus domestica,$Nymphaeaceae,Cactaceae,Cucumis sativus', ''),
(104, 104, 1, 'sharks', 'Cartilaginous fish / manta rays, sharks and guitarfish', 'Elasmobranchii,Rajomorphii,Selachimorpha,$Mobula,Stegostoma fasciatum,Rhynchobatus djiddensis,!Squatiniformes', ''),
/* OTHER_FAMILIES */
(200, 200, 2, 'marsupials', 'Marsupials / koalas, kangeroos and possum', 'Marsupialia,Petaurus breviceps,Phascolarctos cinereus,$Macropus,Didelphis virginiana', ''),
(201, 201, 2, 'african-mammals', 'Afrotheria / elephants, hyraxes and aardvarks', 'Proboscidea,$Paenungulata,Afroinsectiphilia,Sirenia,Hyracoidea,Afrotheria,Rhynchocyon petersi,Orycteropodidae', ''),
(202, 202, 2, 'arthropods', 'Arthropods / spiders, lobsters and centipedes', 'Theraphosidae,Panulirus longipes,$Chilopoda,!Scorpiones,!Acariformes,!Orthoptera', ''),
(203, 203, 2, 'viruses', 'Virus Groups / rabies, ebola and parvo', '$Filoviridae,Rabies lyssavirus,Parvoviridae', ''),
/* HAVE_YOU_HEARD_OF */
(300, 300, 3, 'pika-rabbits-hares', 'Pika? / They''re related to rabbits and hares', 'Lagomorpha,Ochotona,$Sylvilagus,Lepus,!Brachylagus', ''),
(301, 301, 3, 'caecilians-worms-amphibians', 'Caecilians? / They look like giant earthworms, / but are amphibians like frogs and salamanders', '$Microcaecilia,Batrachia,Lissamphibia,Lumbricus terrestris,Scaphiophryne,Bolitoglossa mexicana', ''),
(302, 302, 3, 'pipefish-seahorses-seadragons', 'Ghost Pipefish? / They''re related to seahorses / and seadragons', 'Syngnathiformes,!Aulostomidae,!Centriscidae,$Solenostomidae,Corythoichthys schultzi,Hippocampus guttulatus,Phycodurus eques,Syngnathidae', ''),
(303, 303, 3, 'okapi-giraffes', 'Okapi? / They''re the closest relative to giraffes', '$Okapia,Giraffa camelopardalis tippelskirchi,Giraffa camelopardalis reticulata,Giraffa camelopardalis giraffa,Giraffa,Giraffidae', ''),
/* YOU_MIGHT_NOT_KNOW (i.e. which is closer ...) */
(401, 401, 4, 'hippo-rhino-pig', '... to a hippo / a rhinocerous or a pig?', '$Hippopotamidae,Rhinocerotidae,Suidae,Euungulata,Artiodactyla,Perissodactyla', ''),
(402, 402, 4, 'raccoon-cat-dog', '... to a raccoon / a cat or a dog?', 'Carnivora,Caniformia,Feliformia,Felis,Canis familiaris,$Procyonidae', ''),
(403, 403, 4, 'tomato-cucumber-tobacco', '... to a tomato / a cucumber or tobacco?', 'Nicotiana,Solanaceae,Core eudicots,Cucumis,$Lycopersicum esculentum', ''), 
(404, 404, 4, 'ant-wasp-termite', '... to an ant / a wasp or a termite?', 'Vespidae,$Formicidae,Termitoidae,!Periplaneta,Apocrita,Neoptera', ''),
/* main page default tree */
(505, 505, 5, 'welcome-to-banyan', 'Welcome to Banyan!', '!Chondrichthyes,Canidae,!Amphibia,!Chelicerata,Crustacea,!Chlorophyta,Ananas comosus', '')
;
