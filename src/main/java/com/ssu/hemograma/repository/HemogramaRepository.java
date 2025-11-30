package com.ssu.hemograma.repository;

import com.ssu.hemograma.model.Hemograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HemogramaRepository extends JpaRepository<Hemograma, Long> {
    /**
     * Busca os últimos 'limite' hemogramas, ordenados pela data de coleta mais recente.
     * Esta query utiliza a ordenação decrescente para otimizar a busca dos registros mais novos.
     */
    @Query(value = "SELECT * FROM hemogramas ORDER BY data_coleta DESC LIMIT ?1", nativeQuery = true)
    List<Hemograma> findLastHemogramas(int limite);

    Optional<Hemograma> findByBundleId(String bundleId);

    List<Hemograma> findByPacienteCpf(String pacienteCpf);

    List<Hemograma> findByDataColetaBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT h FROM Hemograma h WHERE h.dataColeta >= :dataInicio AND " +
            "(h.alertaAnemia = true)")
    List<Hemograma> findHemogramasComAlertasAposData(@Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT COUNT(h) FROM Hemograma h WHERE h.dataColeta >= :dataInicio AND h.alertaSurtoAcionado = true")
    Long contarCasosAnemiaAposData(@Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT h FROM Hemograma h WHERE h.dataColeta >= :dataInicio ORDER BY h.dataColeta DESC")
    List<Hemograma> findRecentesAposData(@Param("dataInicio") LocalDateTime dataInicio);
}
