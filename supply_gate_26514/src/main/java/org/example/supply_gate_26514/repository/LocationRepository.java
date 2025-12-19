package org.example.supply_gate_26514.repository;

import org.example.supply_gate_26514.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    Boolean existsLocationByStructureCode(String structureCode);
    Optional<Location> findByStructureCode(String structureCode);
}
