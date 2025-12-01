package com.ssu.hemograma.repository;

import com.ssu.hemograma.model.Hemograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HemogramaDashboardRepository extends JpaRepository<Hemograma, Long> {

    @Query(value = "SELECT COUNT(*) FROM hemogramas", nativeQuery = true)
    Long totalHemogramas();

    @Query(value = "SELECT SUM(is_anemia = TRUE) FROM hemogramas", nativeQuery = true)
    Long totalAnemia();

    @Query(value = "SELECT SUM(alerta_anemia = TRUE) FROM hemogramas", nativeQuery = true)
    Long totalAlerta();

    @Query(value = "SELECT SUM(alerta_surto_acionado = TRUE) FROM hemogramas", nativeQuery = true)
    Long totalSurto();

    @Query(value = """
        SELECT classificacao_anemia, COUNT(*) AS total
        FROM hemogramas
        WHERE is_anemia = TRUE
        GROUP BY classificacao_anemia
        """, nativeQuery = true)
    List<Object[]> porClassificacao();

    @Query(value = """
        SELECT
            CASE
                WHEN idade_em_meses < 12 THEN '0-11 meses'
                WHEN idade_em_meses BETWEEN 12 AND 59 THEN '1-4 anos'
                WHEN idade_em_meses BETWEEN 60 AND 143 THEN '5-11 anos'
                WHEN idade_em_meses BETWEEN 144 AND 215 THEN '12-17 anos'
                ELSE '18+ anos'
            END AS faixa,
            COUNT(*) AS total
        FROM hemogramas
        WHERE is_anemia = TRUE
        GROUP BY faixa
        """, nativeQuery = true)
    List<Object[]> porFaixaEtaria();

    @Query(value = """
        SELECT
            DATE_FORMAT(data_coleta, '%Y-%m') AS periodo,
            COUNT(*) AS total
        FROM hemogramas
        WHERE is_anemia = TRUE
        GROUP BY periodo
        ORDER BY periodo
        """, nativeQuery = true)
    List<Object[]> timeline();
}