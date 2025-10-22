package com.ssu.hemograma.repository;

import com.ssu.hemograma.model.Hemograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HemogramaRepository extends JpaRepository<Hemograma, Long> {

    Optional<Hemograma> findByBundleId(String bundleId);

    List<Hemograma> findByPacienteCpf(String pacienteCpf);

    List<Hemograma> findByAlertaAnemiaTrue();

    List<Hemograma> findByDataColetaBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT h FROM Hemograma h WHERE h.dataColeta >= :dataInicio AND " +
            "(h.alertaAnemia = true)")
    List<Hemograma> findHemogramasComAlertasAposData(@Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT COUNT(h) FROM Hemograma h WHERE h.dataColeta >= :dataInicio AND h.alertaAnemia = true")
    Long contarCasosAnemiaAposData(@Param("dataInicio") LocalDateTime dataInicio);

    @Query("SELECT h FROM Hemograma h WHERE h.dataColeta >= :dataInicio ORDER BY h.dataColeta DESC")
    List<Hemograma> findRecentesAposData(@Param("dataInicio") LocalDateTime dataInicio);
}