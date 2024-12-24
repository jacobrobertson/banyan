select
	(SELECT count(*) FROM crawl where (status <> 'DONE' or status is null)) as `NOT-DONE`,
	(SELECT count(*) FROM crawl where status = 'DONE') as `DONE`,
    (SELECT count(*) FROM crawl) as `ALL`
;

select * from species where (parent_id = 0 or parent_id is NULL) AND PARENT_LATIN_NAME IS NOT null;

SELECT * FROM REDIRECT WHERE REDIRECT_FROM = 'Manta';

select count(*) from species.crawl;
select count(*) from species.crawl where status = 'DONE' and (type <> 'AUTH' or type is null);

SELECT count(*) FROM species.crawl where (status <> 'DONE' or status is null);
SELECT count(*) FROM species.crawl where status = 'DONE';
SELECT count(*) FROM species.crawl where status = 'AUTH';
SELECT * FROM species.crawl where link like '%efpa%';
delete  FROM species.crawl where link = 'Fibuloides cyanopsis' and status_date = '2016-02-23 06:31:29';

-- delete FROM species.crawl where link like '%Pirocydonia%' and link <> 'Pirocydonia';

-- delete FROM species.crawl where link like ' %';

SELECT link, count(link) as cnt FROM species.crawl GROUP BY link HAVING cnt > 1;

select * from species.crawl where link in ('Boscia (Capparacea)');

-- find species that we think were crawled, but never got an entry
select
id, latin_name, common_name, latin_name
FROM 
species.species SP
WHERE NOT EXISTS (SELECT 1 FROM species.crawl CR
                  WHERE CR.link = SP.latin_name and CR.status = 'DONE' and CR.type <> 'AUTH')
limit 10;
-- above is wrong? below is right?
select
link
FROM 
species.crawl CR
WHERE 
	CR.status = 'DONE' and CR.type <> 'AUTH'
and
NOT EXISTS (SELECT 1 FROM species.species SP
                  WHERE CR.link = SP.latin_name)
limit 10;


delete from species.crawl where link in (
'Ancylis acromochla', 
'Ancylis artifica', 
'Ancylis atricapilla', 
'Ancylis catharaspis', 
'Ancylis falsicoma', 
'Fibuloides aestuosa', 
'Fibuloides corinthia', 
'Fibuloides elongata', 
'Fibuloides japonica', 
'Fibuloides levatana', 
'Fibuloides wuyiensis', 
'Niphadophylax albonigra', 
'Niphadophylax sophrona', 
'Stictea macropetana', 
'Toonavora aellaea'
) order by status_date, link;

delete from species.crawl where status_date in 
(a
);

