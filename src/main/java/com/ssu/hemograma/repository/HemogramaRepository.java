package com.ssu.hemograma.repository;

import com.ssu.hemograma.model.Hemograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HemogramaRepository extends JpaRepository<Hemograma, Long> {
    /**
     * Busca os últimos 'limite' hemogramas, ordenados pela data de coleta mais recente.
     * Esta query utiliza a ordenação decrescente para otimizar a busca dos registros mais novos.
     */
    @Query(value = "SELECT * FROM hemogramas ORDER BY data_coleta DESC LIMIT ?1", nativeQuery = true)
    List<Hemograma> findLastHemogramas(int limite);
}
