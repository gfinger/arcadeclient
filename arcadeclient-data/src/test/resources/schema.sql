create vertex type Person;
create property Person.name String (mandatory true, notnull true);
create vertex type Book;
create property Book.title String (mandatory true, notnull true);
insert into Person set name = 'Josh Long';
insert into Book set title = 'Reactive Spring';