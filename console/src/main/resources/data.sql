create vertex Person set name = 'Silvio Enea', surname = 'Piccolomini';
create vertex Pope set name = 'Pius II.';
create edge HasPositionAs from (select from Person where name like 'Silvio Enea') to (select from Pope where name like 'Pius II.') set begin = '1458-01-01', end = '1464-01-01';

