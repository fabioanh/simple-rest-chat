CREATE TABLE users (
	id SERIAL PRIMARY KEY,
	nickname VARCHAR ( 50 ) UNIQUE
);

CREATE TABLE conversations (
	id SERIAL PRIMARY KEY,
	users JSON,
	messages JSON
);