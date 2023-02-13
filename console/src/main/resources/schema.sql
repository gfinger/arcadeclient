
create vertex type Person if not exists;
create property Person.name if not exists string;
create property Person.lastName  if not exists string;
create vertex type Pope if not exists;
create property Pope.name if not exists string;
create edge type HasPositionAs if not exists;
create property HasPositionAs.begin if not exists date;
create property HasPositionAs.end if not exists date;              

               