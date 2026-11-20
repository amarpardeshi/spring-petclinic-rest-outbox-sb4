INSERT INTO owners (id, first_name, last_name, address, city, telephone, created_at, updated_at)
VALUES (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO owners (id, first_name, last_name, address, city, telephone, created_at, updated_at)
VALUES (2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO pets (id, name, birth_date, pet_type, owner_id, created_at, updated_at)
VALUES (1, 'Leo', '2022-09-07', 'CAT', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO pets (id, name, birth_date, pet_type, owner_id, created_at, updated_at)
VALUES (2, 'Basil', '2023-08-06', 'DOG', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO visits (id, visit_date, description, vet_name, pet_id, created_at, updated_at)
VALUES (1, '2026-10-10', 'Annual rabies vaccination', 'Dr. Helen Leary', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
