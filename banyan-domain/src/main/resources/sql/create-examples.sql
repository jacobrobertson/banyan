delete from example_group;

insert into example_group
(index, group_id, caption)
values
(0, 1, 'Explore these trees'),
(1, 3, 'Have your heard of these?'),
(2, 4, 'Can you guess which is more closely related...'),
(3, 2, 'Some families you might not have heard of...')
;
delete from example;

-- TODO - these all have to be redone whenever I start the DB over again.  It's probably better if I 
--		  come up with the Latin names list, and at least capture it here
insert into example
(example_index, example_id, group_id, caption, terms, crunched_ids)
values
-- FAMILIES
(100, 100, 1, 'Tree of Life Overview - animals, plants and fungus', 'Animalia,Fungus,Plantae', '.1rdoE_bU_1Wu_1Mol'),
(101, 101, 1, 'Mammals - elephants, tigers and pigs', 'elephants,tigers,pigs', '-nT4V_14E2ZI02lLtksNS02Hrko'),
(102, 102, 1, 'Insects - crickets, wasps and aphids', 'crickets,wasps,aphids', '0Rq12s.ol.80e.r.sb21U6hzt'),
(103, 103, 1, 'Flowering plants - watermelons, waterlilies and nutmeg', 'watermelons,waterlilies,nutmeg', '-ULmG_6HvKNw7oxM3O'),
(104, 104, 1, 'Cartilaginous fish - manta rays, sharks and guitarfish', 'manta rays,sharks,guitarfish', 'bwilWP-3R-31_SE_6L1.71.2By.573.k1k.41.zwq2m81GP1Ah'),

-- OTHER_FAMILIES
(200, 200, 2, 'Marsupials - koalas, kangeroos and possum', 'koalas,kangeroos,possum', '1xmOnv-1I0c1F2x_sOS-2o0c6P_cQi.21_5c02'),
(201, 201, 2, 'Afrotheria - elephants, hyraxes and aardvarks', 'elephants,hyraxes,aardvarks', '0aQ1me.fe3.306.34'),
(202, 202, 2, 'Arthropods - spiders, lobsters and centipedes', 'spiders,lobsters,centipedes', '12i-khr56Hci9w_99AFf38hQ2C3'),
(203, 203, 2, 'Virus Groups - rabies, ebola and parvo', 'rabies,ebola,parvo', '0UN9qvOOE1Ng.j.NA7.71356i73_gP'),

-- HAVE_YOU_HEARD_OF
(300, 300, 3, 'Pika?  They''re related to rabbits and hares', 'Pika,rabbits,hares', '4AZLke.2_7c_sFI.c54'),
(301, 301, 3, 'Caecilians?  They look like giant earthworms, but are amphibians like frogs and salamanders', 'Caecilians,earthworms,frogs,salamanders', '1e213H0EUVQB.l_3g_2Xo.uL_2Q_zuk2kR.11'),
(302, 302, 3, 'Ghost Pipefish?  They''re related to seahorses and seadragons', 'ghost pipefish,seahorses,seadragons', '9k5.14K.1Qs2w7.517ee16127'),
(303, 303, 3, 'Okapi?  They''re the closes relative to giraffes', 'okapi,giraffes', 'kofYv0.1113'),

-- YOU_MIGHT_NOT_KNOW (i.e. which is closer ...)
(401, 401, 4, '... to a hippo - a rhinocerous or a pig?', 'Hippopotamidae,Rhinocerotidae,Suidae', '4xY-1w01'),
(402, 402, 4, '... to a racoon - a cat or a dog?', 'racoon,cat,dog', '100OYU.3'),
(403, 403, 4, '... to a tomatoe - a cucumber or tobacco?', 'tomatoe,cucumber,tobacco', '3O1NEcp5c02u-'), 
(404, 404, 4, '... to an ant - a wasp or a termite?', 'ant,wasp,termite', '3bd.5g.6IJ.yL.Ma2cLY.u.r8v')

;
