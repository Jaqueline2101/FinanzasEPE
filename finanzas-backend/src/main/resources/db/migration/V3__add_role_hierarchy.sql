ALTER TABLE roles ADD COLUMN parent_id BIGINT;
ALTER TABLE roles ADD CONSTRAINT fk_roles_parent FOREIGN KEY (parent_id) REFERENCES roles(id);
