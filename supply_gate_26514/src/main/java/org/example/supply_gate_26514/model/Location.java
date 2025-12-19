package org.example.supply_gate_26514.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "administrative_structure")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID structureId;
    private String structureCode;
    private String structureName;
    @ManyToOne
    @JoinColumn(name="parent_id")
    private  Location parent;

    public LocationEnum getStructureType() {
        return structureType;
    }

    public void setStructureType(LocationEnum structureType) {
        this.structureType = structureType;
    }

    @Enumerated(EnumType.STRING)
    private LocationEnum structureType;
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("user-location")
    private List<User> user;

    public List<User> getUser() {
        return user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    public UUID getStructureId() {
        return structureId;
    }

    public String getStructureCode() {
        return structureCode;
    }

    public String getStructureName() {
        return structureName;
    }

    public Location getParent() {
        return parent;
    }

    public LocationEnum getLocationEnum() {
        return structureType;
    }

    public void setStructureId(UUID structureId) {
        this.structureId = structureId;
    }

    public void setStructureCode(String structureCode) {
        this.structureCode = structureCode;
    }

    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }

    public void setParent(Location parent) {
        this.parent = parent;
    }

    public void setLocationEnum(LocationEnum structureType) {
        this.structureType = structureType;
    }

    public Location() {
    }

    public Location(UUID structureId, String structureCode, String structureName, Location parent, LocationEnum structureType) {
        this.structureId = structureId;
        this.structureCode = structureCode;
        this.structureName = structureName;
        this.parent = parent;
        this.structureType = structureType;
    }
}

