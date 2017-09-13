create sequence disbursals_id_seq
;

create table disbursals
(
	id serial not null
		constraint disbursals_pkey
			primary key,
	datetime timestamp not null,
	outletref integer not null,
	userid text not null,
	transactiontype integer not null,
	cashspent numeric(2) not null,
	discountamount numeric(2) not null,
	totalamount numeric(2) not null
)
;

create unique index disbursals_id_uindex
	on disbursals (id)
;

create table users
(
	username text not null
		constraint users_pkey
			primary key,
	password text not null,
	email text not null,
	isadmin boolean default false not null,
	firstname text not null,
	lastname text not null
)
;

create unique index users_username_uindex
	on users (username)
;

create table outlets
(
	outletref integer not null
		constraint outlets_pkey
			primary key,
	outletname text not null
)
;

create unique index outlets_outletref_uindex
	on outlets (outletref)
;

create unique index outlets_outletname_uindex
	on outlets (outletname)
;

comment on table outlets is 'Joining Table'
;

alter table disbursals
	add constraint disbursals_fk_outlets
		foreign key (outletref) references outlets
;

create table transactiontypes
(
	transactionid integer not null
		constraint transactiontypes_pkey
			primary key,
	transactiontype text not null
)
;

create unique index transactiontypes_transactionid_uindex
	on transactiontypes (transactionid)
;

create unique index transactiontypes_transactiontype_uindex
	on transactiontypes (transactiontype)
;

alter table disbursals
	add constraint disbursals_fk_transactiontypes
		foreign key (transactiontype) references transactiontypes
;

