package com.respawncherry.foro.service;

import com.respawncherry.foro.entity.Categoria;
import com.respawncherry.foro.repository.CategoriaRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "foro.categorias.seed", havingValue = "true", matchIfMissing = true)
public class CategoriaInitializer implements ApplicationRunner {

    private static final List<String> CATEGORIAS = List.of(
            "Videojuegos", "Busca tu Squad", "Setups", "Consejos Gamer", "Cherry Lounge");

    private final CategoriaRepository categoriaRepository;

    public CategoriaInitializer(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String nombre : CATEGORIAS) {
            if (categoriaRepository.findByNombre(nombre).isEmpty()) {
                categoriaRepository.save(new Categoria(nombre));
            }
        }
    }
}
