MERGE INTO CATEGORIAS target
USING (
    SELECT 'Videojuegos' nombre FROM DUAL
    UNION ALL SELECT 'Busca tu Squad' FROM DUAL
    UNION ALL SELECT 'Setups' FROM DUAL
    UNION ALL SELECT 'Consejos Gamer' FROM DUAL
    UNION ALL SELECT 'Cherry Lounge' FROM DUAL
) source
ON (target.NOMBRE = source.nombre)
WHEN NOT MATCHED THEN
    INSERT (ID, NOMBRE)
    VALUES (CATEGORIA_SEQ.NEXTVAL, source.nombre);

MERGE INTO PUBLICACIONES target
USING (
    SELECT 'Recomendaciones para jugar en cooperativo' titulo,
           'Comparte tus juegos cooperativos favoritos.' contenido,
           (SELECT ID FROM CATEGORIAS WHERE NOMBRE = 'Videojuegos') categoria_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador1@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.TITULO = source.titulo AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, TITULO, CONTENIDO, CATEGORIA_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (PUBLICACION_SEQ.NEXTVAL, source.titulo, source.contenido, source.categoria_id, source.autor_id, SYSTIMESTAMP);

MERGE INTO PUBLICACIONES target
USING (
    SELECT 'Busco squad para partidas casuales' titulo,
           'Busco equipo para jugar algunas tardes.' contenido,
           (SELECT ID FROM CATEGORIAS WHERE NOMBRE = 'Busca tu Squad') categoria_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador2@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.TITULO = source.titulo AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, TITULO, CONTENIDO, CATEGORIA_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (PUBLICACION_SEQ.NEXTVAL, source.titulo, source.contenido, source.categoria_id, source.autor_id, SYSTIMESTAMP);

MERGE INTO PUBLICACIONES target
USING (
    SELECT 'Consejos para mejorar el setup' titulo,
           '¿Qué mejoras recomiendan para un espacio gamer?' contenido,
           (SELECT ID FROM CATEGORIAS WHERE NOMBRE = 'Setups') categoria_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador3@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.TITULO = source.titulo AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, TITULO, CONTENIDO, CATEGORIA_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (PUBLICACION_SEQ.NEXTVAL, source.titulo, source.contenido, source.categoria_id, source.autor_id, SYSTIMESTAMP);

MERGE INTO COMENTARIOS target
USING (
    SELECT 'También recomiendo juegos cooperativos locales.' contenido,
           (SELECT ID FROM PUBLICACIONES WHERE TITULO = 'Recomendaciones para jugar en cooperativo'
             AND AUTOR_ID = (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador1@respawncherry.local')) publicacion_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador2@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.CONTENIDO = source.contenido AND target.PUBLICACION_ID = source.publicacion_id AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, CONTENIDO, PUBLICACION_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (COMENTARIO_SEQ.NEXTVAL, source.contenido, source.publicacion_id, source.autor_id, SYSTIMESTAMP);

MERGE INTO COMENTARIOS target
USING (
    SELECT '¿Qué horarios tienen para jugar?' contenido,
           (SELECT ID FROM PUBLICACIONES WHERE TITULO = 'Busco squad para partidas casuales'
             AND AUTOR_ID = (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador2@respawncherry.local')) publicacion_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador3@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.CONTENIDO = source.contenido AND target.PUBLICACION_ID = source.publicacion_id AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, CONTENIDO, PUBLICACION_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (COMENTARIO_SEQ.NEXTVAL, source.contenido, source.publicacion_id, source.autor_id, SYSTIMESTAMP);

MERGE INTO COMENTARIOS target
USING (
    SELECT 'Una buena silla y una iluminación cómoda ayudan bastante.' contenido,
           (SELECT ID FROM PUBLICACIONES WHERE TITULO = 'Consejos para mejorar el setup'
             AND AUTOR_ID = (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador3@respawncherry.local')) publicacion_id,
           (SELECT ID FROM USUARIOS WHERE CORREO = 'jugador1@respawncherry.local') autor_id
    FROM DUAL
) source
ON (target.CONTENIDO = source.contenido AND target.PUBLICACION_ID = source.publicacion_id AND target.AUTOR_ID = source.autor_id)
WHEN NOT MATCHED THEN
    INSERT (ID, CONTENIDO, PUBLICACION_ID, AUTOR_ID, FECHA_CREACION)
    VALUES (COMENTARIO_SEQ.NEXTVAL, source.contenido, source.publicacion_id, source.autor_id, SYSTIMESTAMP);

COMMIT;
