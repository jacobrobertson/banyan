-- this is what I want to delete, no need to actually run them

select count(*) from species where boring_final = true;
select count(*) from crawl;
select count(*) from redirect;

--
delete from species where boring_final = true and id >= 0      and id <  50000;
delete from species where boring_final = true and id >= 50000  and id < 100000;
delete from species where boring_final = true and id >= 100000 and id < 150000;
delete from species where boring_final = true and id >= 150000 and id < 200000;
delete from species where boring_final = true and id >= 200000 and id < 250000;
delete from species where boring_final = true and id >= 250000 and id < 300000;
delete from species where boring_final = true and id >= 300000 and id < 350000;
delete from species where boring_final = true and id >= 350000 and id < 400000;
delete from species where boring_final = true and id >= 400000 and id < 450000;
delete from species where boring_final = true and id >= 450000 and id < 500000;
delete from species where boring_final = true and id >= 500000 and id < 550000;
delete from species where boring_final = true and id >= 550000 and id < 600000;
delete from species where boring_final = true and id >= 600000 and id < 650000;

drop table crawl;
drop table redirect;

call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('APP', 'SPECIES', 1);