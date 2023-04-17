delete from Customer;
delete from Person;
create vertex Customer content {"name": "Happy Garden", "address": {"street": "Flower Road", "@type": "Address"}};
create vertex Person content {"name": "Clint"};
create edge IsContactOf from (Select from Person where name = 'Clint') to (Select from Customer where name = 'Happy Garden')


