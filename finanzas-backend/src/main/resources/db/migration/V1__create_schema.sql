CREATE TABLE acceso_main (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    escala VARCHAR(50),
    tema VARCHAR(50)
);

CREATE TABLE sucursal (
    id SERIAL PRIMARY KEY,
    nombre_sucursal VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    estado CHAR(1) DEFAULT 'A'
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    active BOOLEAN DEFAULT TRUE,
    alcance VARCHAR(50),
    acceso_id BIGINT REFERENCES acceso_main(id)
);

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(200),
    active BOOLEAN DEFAULT TRUE,
    acceso_id BIGINT REFERENCES acceso_main(id)
);

CREATE TABLE role_permissions (
    role_id BIGINT REFERENCES roles(id),
    permission_id BIGINT REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE datos_personales (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    passwd VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(100),
    apepat VARCHAR(100),
    apemat VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    sucursal_actual_id INTEGER REFERENCES sucursal(id),
    last_login TIMESTAMP,
    foto_perfil VARCHAR(255)
);

CREATE TABLE user_branch_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES datos_personales(id),
    branch_id INTEGER REFERENCES sucursal(id),
    role_id BIGINT REFERENCES roles(id),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tablas heredadas (Legacy compatibility)
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES datos_personales(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE usuario_sucursales_asignadas (
    usuario_id BIGINT REFERENCES datos_personales(id),
    sucursal_id INTEGER REFERENCES sucursal(id),
    PRIMARY KEY (usuario_id, sucursal_id)
);
