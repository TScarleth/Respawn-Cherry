ACCEPT seed_user_1_hash CHAR PROMPT 'BCrypt hash for Jugador Uno: ' HIDE
ACCEPT seed_user_2_hash CHAR PROMPT 'BCrypt hash for Jugador Dos: ' HIDE
ACCEPT seed_user_3_hash CHAR PROMPT 'BCrypt hash for Jugador Tres: ' HIDE

MERGE INTO USUARIOS target
USING (SELECT 'Jugador Uno' nombre, 'jugador1@respawncherry.local' correo FROM DUAL) source
ON (target.CORREO = source.correo)
WHEN NOT MATCHED THEN
    INSERT (ID, NOMBRE, CORREO, CONTRASENA, ROL)
    VALUES (USUARIO_SEQ.NEXTVAL, source.nombre, source.correo, '&seed_user_1_hash', 'USUARIO');

MERGE INTO USUARIOS target
USING (SELECT 'Jugador Dos' nombre, 'jugador2@respawncherry.local' correo FROM DUAL) source
ON (target.CORREO = source.correo)
WHEN NOT MATCHED THEN
    INSERT (ID, NOMBRE, CORREO, CONTRASENA, ROL)
    VALUES (USUARIO_SEQ.NEXTVAL, source.nombre, source.correo, '&seed_user_2_hash', 'USUARIO');

MERGE INTO USUARIOS target
USING (SELECT 'Jugador Tres' nombre, 'jugador3@respawncherry.local' correo FROM DUAL) source
ON (target.CORREO = source.correo)
WHEN NOT MATCHED THEN
    INSERT (ID, NOMBRE, CORREO, CONTRASENA, ROL)
    VALUES (USUARIO_SEQ.NEXTVAL, source.nombre, source.correo, '&seed_user_3_hash', 'USUARIO');

UNDEFINE seed_user_1_hash
UNDEFINE seed_user_2_hash
UNDEFINE seed_user_3_hash

COMMIT;
