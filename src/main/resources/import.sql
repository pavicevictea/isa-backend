INSERT INTO role (name) VALUES ('ROLE_USER');

INSERT INTO users (username, password, first_name, last_name, email, enabled, address, activation_code) VALUES ('pera', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', 'Petar', 'Petrovic', 'petar@example.com', true, 'Adresa 123', NULL);

INSERT INTO user_role (user_id, role_id) VALUES ((SELECT id FROM users WHERE username = 'pera'), (SELECT id FROM role WHERE name = 'ROLE_USER'));

SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval(pg_get_serial_sequence('role', 'id'), COALESCE((SELECT MAX(id) FROM role), 1));