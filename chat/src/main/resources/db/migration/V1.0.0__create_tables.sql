CREATE TABLE users (
	id SERIAL PRIMARY KEY,
	username VARCHAR ( 50 ) UNIQUE
);

CREATE TABLE conversations (
	id SERIAL PRIMARY KEY,
	users JSON,
	conversation JSON
);