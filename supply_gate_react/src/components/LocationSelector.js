import React, { useState, useEffect } from "react";
import { MapPin, ChevronDown } from "lucide-react";
import { locationApi } from "../lib/api";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./ui/select";

export function LocationSelector({
  onLocationChange,
  selectedLocationId,
  error,
  required = false,
}) {
  const [allLocations, setAllLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  // Selected values for each level
  const [selectedProvince, setSelectedProvince] = useState(null);
  const [selectedDistrict, setSelectedDistrict] = useState(null);
  const [selectedSector, setSelectedSector] = useState(null);
  const [selectedCell, setSelectedCell] = useState(null);
  const [selectedVillage, setSelectedVillage] = useState(null);

  // Load all locations on component mount
  useEffect(() => {
    const loadLocations = async () => {
      setLoading(true);
      setErrorMessage(null);
      try {
        const locations = await locationApi.getAllLocations();
        setAllLocations(locations);
      } catch (err) {
        const errorMsg = err.message || "Failed to load locations";
        setErrorMessage(errorMsg);
        if (process.env.NODE_ENV === 'development') {
          console.error("Error loading locations:", err);
        }
      } finally {
        setLoading(false);
      }
    };

    loadLocations();
  }, []);

  /**
   * Filter locations by type and parent
   * 
   * For Provinces: No parent (parent is null/undefined)
   * For Districts: Parent is the selected Province
   * For Sectors: Parent is the selected District
   * For Cells: Parent is the selected Sector
   * For Villages: Parent is the selected Cell
   */
  const getLocationsByType = (type, parentId) => {
    return allLocations.filter((loc) => {
      // First check if the location type matches
      if (loc.structureType !== type) return false;
      
      // If parentId is provided, check if this location's parent matches
      if (parentId) {
        // Check if parent exists and matches
        return loc.parent?.structureId === parentId;
      }
      
      // For top-level (Province), parent should be null or undefined
      return !loc.parent;
    });
  };

  // Get selected location object
  const getLocationById = (id) => {
    if (!id) return null;
    return allLocations.find((loc) => loc.structureId === id) || null;
  };

  // Handle province selection
  const handleProvinceChange = (provinceId) => {
    setSelectedProvince(provinceId);
    setSelectedDistrict(null);
    setSelectedSector(null);
    setSelectedCell(null);
    setSelectedVillage(null);
    onLocationChange(null); // Reset until village is selected
  };

  // Handle district selection
  const handleDistrictChange = (districtId) => {
    setSelectedDistrict(districtId);
    setSelectedSector(null);
    setSelectedCell(null);
    setSelectedVillage(null);
    onLocationChange(null); // Reset until village is selected
  };

  // Handle sector selection
  const handleSectorChange = (sectorId) => {
    setSelectedSector(sectorId);
    setSelectedCell(null);
    setSelectedVillage(null);
    onLocationChange(null); // Reset until village is selected
  };

  // Handle cell selection
  const handleCellChange = (cellId) => {
    setSelectedCell(cellId);
    setSelectedVillage(null);
    onLocationChange(null); // Reset until village is selected
  };

  // Handle village selection (final selection)
  const handleVillageChange = (villageId) => {
    setSelectedVillage(villageId);
    onLocationChange(villageId); // This is the final locationId we need
  };

  // Get filtered locations for each level
  const provinces = getLocationsByType('PROVINCE');
  const districts = selectedProvince ? getLocationsByType('DISTRICT', selectedProvince) : [];
  const sectors = selectedDistrict ? getLocationsByType('SECTOR', selectedDistrict) : [];
  const cells = selectedSector ? getLocationsByType('CELL', selectedSector) : [];
  const villages = selectedCell ? getLocationsByType('VILLAGE', selectedCell) : [];

  return (
    <div className="space-y-4">
      {/* Error Message */}
      {(error || errorMessage) && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {error || errorMessage}
        </div>
      )}

      {/* Location Selectors - Always visible, even while loading */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
          {/* Province Selector */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Province {required && <span className="text-red-500">*</span>}
              {loading && <span className="ml-2 text-xs text-gray-400">(Loading...)</span>}
            </label>
            <Select
              value={selectedProvince || ""}
              onValueChange={handleProvinceChange}
              disabled={loading}
            >
              <SelectTrigger
                className={`w-full ${error ? "border-red-500" : ""} ${
                  selectedProvince ? "border-blue-500" : ""
                } ${loading ? "opacity-60" : ""}`}
              >
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <SelectValue placeholder={loading ? "Loading..." : "Select Province"} />
                </div>
                <ChevronDown className="w-4 h-4 text-gray-500" />
              </SelectTrigger>
              <SelectContent>
                {loading ? (
                  <SelectItem value="loading" disabled>
                    Loading provinces...
                  </SelectItem>
                ) : provinces.length === 0 ? (
                  <SelectItem value="no-provinces" disabled>
                    No provinces available
                  </SelectItem>
                ) : (
                  provinces.map((province) => (
                    <SelectItem key={province.structureId} value={province.structureId}>
                      {province.structureName}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>

          {/* District Selector */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              District {required && <span className="text-red-500">*</span>}
            </label>
            <Select
              value={selectedDistrict || ""}
              onValueChange={handleDistrictChange}
              disabled={!selectedProvince || loading}
            >
              <SelectTrigger
                className={`w-full ${!selectedProvince ? "opacity-50 cursor-not-allowed" : ""} ${
                  selectedDistrict ? "border-blue-500" : ""
                }`}
              >
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <SelectValue placeholder="Select District" />
                </div>
                <ChevronDown className="w-4 h-4 text-gray-500" />
              </SelectTrigger>
              <SelectContent>
                {districts.length === 0 ? (
                  <SelectItem value="no-districts" disabled>
                    {selectedProvince ? "No districts available" : "Select province first"}
                  </SelectItem>
                ) : (
                  districts.map((district) => (
                    <SelectItem key={district.structureId} value={district.structureId}>
                      {district.structureName}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>

          {/* Sector Selector */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Sector {required && <span className="text-red-500">*</span>}
            </label>
            <Select
              value={selectedSector || ""}
              onValueChange={handleSectorChange}
              disabled={!selectedDistrict || loading}
            >
              <SelectTrigger
                className={`w-full ${!selectedDistrict ? "opacity-50 cursor-not-allowed" : ""} ${
                  selectedSector ? "border-blue-500" : ""
                }`}
              >
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <SelectValue placeholder="Select Sector" />
                </div>
                <ChevronDown className="w-4 h-4 text-gray-500" />
              </SelectTrigger>
              <SelectContent>
                {sectors.length === 0 ? (
                  <SelectItem value="no-sectors" disabled>
                    {selectedDistrict ? "No sectors available" : "Select district first"}
                  </SelectItem>
                ) : (
                  sectors.map((sector) => (
                    <SelectItem key={sector.structureId} value={sector.structureId}>
                      {sector.structureName}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>

          {/* Cell Selector */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Cell {required && <span className="text-red-500">*</span>}
            </label>
            <Select
              value={selectedCell || ""}
              onValueChange={handleCellChange}
              disabled={!selectedSector || loading}
            >
              <SelectTrigger
                className={`w-full ${!selectedSector ? "opacity-50 cursor-not-allowed" : ""} ${
                  selectedCell ? "border-blue-500" : ""
                }`}
              >
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <SelectValue placeholder="Select Cell" />
                </div>
                <ChevronDown className="w-4 h-4 text-gray-500" />
              </SelectTrigger>
              <SelectContent>
                {cells.length === 0 ? (
                  <SelectItem value="no-cells" disabled>
                    {selectedSector ? "No cells available" : "Select sector first"}
                  </SelectItem>
                ) : (
                  cells.map((cell) => (
                    <SelectItem key={cell.structureId} value={cell.structureId}>
                      {cell.structureName}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>

          {/* Village Selector */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Village {required && <span className="text-red-500">*</span>}
            </label>
            <Select
              value={selectedVillage || ""}
              onValueChange={handleVillageChange}
              disabled={!selectedCell || loading}
            >
              <SelectTrigger
                className={`w-full ${!selectedCell ? "opacity-50 cursor-not-allowed" : ""} ${
                  selectedVillage ? "border-blue-500" : ""
                }`}
              >
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-gray-500" />
                  <SelectValue placeholder="Select Village" />
                </div>
                <ChevronDown className="w-4 h-4 text-gray-500" />
              </SelectTrigger>
              <SelectContent>
                {villages.length === 0 ? (
                  <SelectItem value="no-villages" disabled>
                    {selectedCell ? "No villages available" : "Select cell first"}
                  </SelectItem>
                ) : (
                  villages.map((village) => (
                    <SelectItem key={village.structureId} value={village.structureId}>
                      {village.structureName}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>
        </div>

      {/* Selected Location Display */}
      {selectedVillage && (
        <div className="mt-2 p-2 bg-blue-50 border border-blue-200 rounded text-sm text-blue-700">
          Selected: {getLocationById(selectedVillage)?.structureName || "Village"}
        </div>
      )}
    </div>
  );
}
