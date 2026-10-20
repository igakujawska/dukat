-- Serwis sprawdza nazwy, a indeksy chronią również przed wyścigiem dwóch rejestracji.
CREATE UNIQUE INDEX users_username_ci ON users(lower(username));
CREATE UNIQUE INDEX users_email_ci ON users(lower(email));
