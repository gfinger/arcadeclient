-- noinspection SqlNoDataSourceInspectionForFile

create vertex type Customer if not exists;
create property Customer.name if not exists string;
create property Customer.address if not exists embedded;
create vertex type Person if not exists;
create property Person.name if not exists string;
create edge type IsContactOf if not exists;

