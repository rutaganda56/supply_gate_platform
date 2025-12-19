package org.example.supply_gate_26514.service;

import java.util.*;

/**
 * Kigali Location Data
 * 
 * Contains all real administrative structures for Kigali:
 * - Province: Kigali
 * - Districts: Gasabo, Kicukiro, Nyarugenge
 * - Sectors, Cells, and Villages with actual names
 */
public class KigaliLocationData {

    /**
     * Get all location data as a hierarchical structure
     * Returns: Map<District, Map<Sector, Map<Cell, List<Village>>>>
     */
    public static Map<String, Map<String, Map<String, List<String>>>> getAllLocationData() {
        Map<String, Map<String, Map<String, List<String>>>> data = new LinkedHashMap<>();
        
        // Nyarugenge District Data
        Map<String, Map<String, List<String>>> nyarugengeData = getNyarugengeData();
        data.put("Nyarugenge", nyarugengeData);
        
        // Note: Add Gasabo and Kicukiro data here when available
        // For now, we'll create placeholder structures for them
        
        return data;
    }

    /**
     * Get Nyarugenge District complete data
     */
    private static Map<String, Map<String, List<String>>> getNyarugengeData() {
        Map<String, Map<String, List<String>>> sectors = new LinkedHashMap<>();
        
        // Gitega Sector
        Map<String, List<String>> gitegaCells = new LinkedHashMap<>();
        gitegaCells.put("Akabahizi", Arrays.asList(
            "Gihanga", "Iterambere", "Izuba", "Nyaburanga", "Nyenyeri", 
            "Ubukorikori", "Ubumwe", "Ubwiyunge", "Umucyo", "Umurabyo", "Umuseke", "Vugizo"
        ));
        gitegaCells.put("Akabeza", Arrays.asList(
            "Akinyambo", "Amayaga", "Gitwa", "Ituze", "Mpazi"
        ));
        gitegaCells.put("Gacyamo", Arrays.asList(
            "Amahoro", "Impuhwe", "Intsinzi", "Kivumu", "Ubumwe", "Urukundo", "Ururembo"
        ));
        gitegaCells.put("Kigarama", Arrays.asList(
            "Ingenzi", "Sangwa", "Umubano", "Umucyo", "Umuhoza", "Umurava"
        ));
        gitegaCells.put("Kinyange", Arrays.asList(
            "Akabugenewe", "Ihuriro", "Isangano", "Isano", "Karitasi", 
            "Ubumanzi", "Uburezi", "Ubwiza", "Umucyo", "Umwembe", "Urugano"
        ));
        gitegaCells.put("Kora", Arrays.asList(
            "Isangano", "Kanunga", "Kinyambo", "Kivumu", "Kora", 
            "Mpazi", "Rugano", "Rugari", "Ubumwe"
        ));
        sectors.put("Gitega", gitegaCells);
        
        // Kanyinya Sector
        Map<String, List<String>> kanyinyaCells = new LinkedHashMap<>();
        kanyinyaCells.put("Nyamweru", Arrays.asList(
            "Bwimo", "Gatare", "Mubuga", "Nyakirambi", "Nyamweru", "Ruhengeri"
        ));
        kanyinyaCells.put("Nzove", Arrays.asList(
            "Bibungo", "Bwiza", "Gateko", "Kagasa", "Nyabihu", 
            "Rutagara I", "Rutagara Ii", "Ruyenzi"
        ));
        kanyinyaCells.put("Taba", Arrays.asList(
            "Kagaramira", "Ngendo", "Nyarurama", "Nyarusange", "Rwakivumu", "Taba"
        ));
        sectors.put("Kanyinya", kanyinyaCells);
        
        // Kigali Sector
        Map<String, List<String>> kigaliCells = new LinkedHashMap<>();
        kigaliCells.put("Kigali", Arrays.asList(
            "Akirwanda", "Gisenga", "Kadobogo", "Kagarama", "Kibisogi", 
            "Muganza", "Murama", "Rubuye", "Ruhango", "Ryasharangabo"
        ));
        kigaliCells.put("Mwendo", Arrays.asList(
            "Agakomeye", "Akagugu", "Amahoro", "Amajyambere", "Birambo", 
            "Isangano", "Kanyabami", "Karambo", "Mwendo", "Ruhuha", "Ubuzima", "Umutekano"
        ));
        kigaliCells.put("Nyabugogo", Arrays.asList(
            "Gakoni", "Gatare", "Giticyinyoni", "Kadobogo", "Kamenge", 
            "Karama", "Kiruhura", "Nyabikoni", "Nyabugogo", "Ruhondo"
        ));
        kigaliCells.put("Ruriba", Arrays.asList(
            "Misibya", "Nyabitare", "Ruhango", "Ruharabuge", "Ruriba", 
            "Ruzigimbogo", "Ryamakomari", "Tubungo"
        ));
        kigaliCells.put("Rwesero", Arrays.asList(
            "Akanyamirambo", "Akinama", "Makaga", "Musimba", "Ruhogo", 
            "Rwesero", "Rweza", "Vuganyana"
        ));
        sectors.put("Kigali", kigaliCells);
        
        // Kimisagara Sector
        Map<String, List<String>> kimisagaraCells = new LinkedHashMap<>();
        kimisagaraCells.put("Kamuhoza", Arrays.asList(
            "Buhoro", "Busasamana", "Isimbi", "Ituze", "Karama", "Karwarugabo", 
            "Kigabiro", "Mataba", "Munini", "Ntaraga", "Nunga", "Rurama", "Rutunga", "Tetero"
        ));
        kimisagaraCells.put("Katabaro", Arrays.asList(
            "Akamahoro", "Akishinge", "Akishuri", "Amahumbezi", "Inganzo", 
            "Kigarama", "Mpazi", "Mugina", "Ubumwe", "Ubusabane", "Umubano", "Umurinzi", "Uruyange"
        ));
        kimisagaraCells.put("Kimisagara", Arrays.asList(
            "Akabeza", "Amahoro", "Birama", "Buhoro", "Bwiza", "Byimana", 
            "Gakaraza", "Gaseke", "Ihuriro", "Inkurunziza", "Karambi", "Kigina", 
            "Kimisagara", "Kove", "Muganza", "Nyabugogo", "Nyagakoki", "Nyakabingo", 
            "Nyamabuye", "Sangwa", "Sano"
        ));
        sectors.put("Kimisagara", kimisagaraCells);
        
        // Mageregere Sector
        Map<String, List<String>> mageregereCells = new LinkedHashMap<>();
        mageregereCells.put("Kankuba", Arrays.asList(
            "Kamatamu", "Kankuba", "Karukina", "Musave", "Nyarumanga", "Rugendabari"
        ));
        mageregereCells.put("Kavumu", Arrays.asList(
            "Ayabatanga", "Kankurimba", "Kavumu", "Mubura", "Murondo", "Nyakabingo", "Nyarubuye"
        ));
        mageregereCells.put("Mataba", Arrays.asList(
            "Burema", "Gahombo", "Kabeza", "Karambi", "Kwisanga", "Mageragere", "Mataba", "Rushubi"
        ));
        mageregereCells.put("Ntungamo", Arrays.asList(
            "Akanakamageragere", "Gatovu", "Nyabitare", "Nyarubande", "Rubungo", "Rwindonyi"
        ));
        mageregereCells.put("Nyarufunzo", Arrays.asList(
            "Akabungo", "Akamashinge", "Maya", "Nyarufunzo", "Nyarurama", "Rubete"
        ));
        mageregereCells.put("Nyarurenzi", Arrays.asList(
            "Amahoro", "Ayabaramba", "Gikuyu", "Iterambere", "Nyabirondo", "Nyarurenzi"
        ));
        mageregereCells.put("Runzenze", Arrays.asList(
            "Gisunzu", "Mpanga", "Nkomero", "Runzenze", "Uwurugenge"
        ));
        sectors.put("Mageregere", mageregereCells);
        
        // Muhima Sector
        Map<String, List<String>> muhimaCells = new LinkedHashMap<>();
        muhimaCells.put("Amahoro", Arrays.asList(
            "Amahoro", "Amizero", "Inyarurembo", "Kabirizi", "Ubuzima", "Uruhimbi"
        ));
        muhimaCells.put("Kabasengerezi", Arrays.asList(
            "Icyeza", "Ikana", "Intwari", "Kabasengerezi"
        ));
        muhimaCells.put("Kabeza", Arrays.asList(
            "Hirwa", "Ikaze", "Imanzi", "Ingenzi", "Ituze", "Sangwa", "Umwezi"
        ));
        muhimaCells.put("Nyabugogo", Arrays.asList(
            "Abeza", "Icyerekezo", "Indatwa", "Rwezangoro", "Ubucuruzi", "Umutekano"
        ));
        muhimaCells.put("Rugenge", Arrays.asList(
            "Imihigo", "Impala", "Rugenge", "Ubumanzi"
        ));
        muhimaCells.put("Tetero", Arrays.asList(
            "Indamutsa", "Ingoro", "Inkingi", "Intiganda", "Iwacu", "Tetero"
        ));
        muhimaCells.put("Ubumwe", Arrays.asList("Isangano"));
        sectors.put("Muhima", muhimaCells);
        
        // Nyakabanda Sector
        Map<String, List<String>> nyakabandaCells = new LinkedHashMap<>();
        nyakabandaCells.put("Munanira I", Arrays.asList(
            "Kabusunzu", "Munanira", "Ntaraga", "Nyagasozi", "Rurembo"
        ));
        nyakabandaCells.put("Munanira Ii", Arrays.asList(
            "Gasiza", "Kamwiza", "Kanyange", "Karudandi", "Kigabiro", 
            "Kokobe", "Mucyuranyana", "Nkundumurimbo"
        ));
        nyakabandaCells.put("Nyakabanda I", Arrays.asList(
            "Akinkware", "Gapfupfu", "Gasiza", "Kariyeri", "Kokobe", 
            "Munini", "Nyakabanda", "Rwagitanga"
        ));
        nyakabandaCells.put("Nyakabanda Ii", Arrays.asList(
            "Ibuhoro", "Kabeza", "Kanyiranganji", "Karujongi", "Kigarama", "Kirwa"
        ));
        sectors.put("Nyakabanda", nyakabandaCells);
        
        // Nyamirambo Sector
        Map<String, List<String>> nyamiramboCells = new LinkedHashMap<>();
        nyamiramboCells.put("Cyivugiza", Arrays.asList(
            "Amizero", "Gabiro", "Imanzi", "Ingenzi", "Intwari", "Karisimbi", 
            "Mahoro", "Mpano", "Muhabura", "Muhoza", "Munini", "Rugero", "Shema"
        ));
        nyamiramboCells.put("Gasharu", Arrays.asList(
            "Kagunga", "Karukoro", "Rwintare"
        ));
        nyamiramboCells.put("Mumena", Arrays.asList(
            "Akanyana", "Akanyirazaninka", "Akarekare", "Akatabaro", "Irembo", 
            "Itaba", "Kiberinka", "Mumena", "Rwampara"
        ));
        nyamiramboCells.put("Rugarama", Arrays.asList(
            "Gatare", "Kiberinka", "Munanira", "Riba", "Rubona", "Rugarama", 
            "Runyinya", "Rusisiro", "Tetero"
        ));
        sectors.put("Nyamirambo", nyamiramboCells);
        
        // Nyarugenge Sector
        Map<String, List<String>> nyarugengeSectorCells = new LinkedHashMap<>();
        nyarugengeSectorCells.put("Agatare", Arrays.asList(
            "Agatare", "Amajyambere", "Inyambo", "Meraneza", "Uburezi", "Umucyo", "Umurava"
        ));
        nyarugengeSectorCells.put("Biryogo", Arrays.asList(
            "Biryogo", "Gabiro", "Isoko", "Nyiranuma", "Umurimo"
        ));
        nyarugengeSectorCells.put("Kiyovu", Arrays.asList(
            "Amizero", "Cercle Sportif", "Ganza", "Imena", "Indangamirwa", "Ingenzi", 
            "Inyarurembo", "Ishema", "Isibo", "Muhabura", "Rugunga", "Sugira"
        ));
        nyarugengeSectorCells.put("Rwampara", Arrays.asList(
            "Amahoro", "Gacaca", "Intwari", "Rwampara", "Umucyo", "Umuganda"
        ));
        sectors.put("Nyarugenge", nyarugengeSectorCells);
        
        // Rwezamenyo Sector
        Map<String, List<String>> rwezamenyoCells = new LinkedHashMap<>();
        rwezamenyoCells.put("Kabuguru I", Arrays.asList(
            "Muhoza", "Muhuza", "Mumararungu", "Murambi"
        ));
        rwezamenyoCells.put("Kabuguru Ii", Arrays.asList(
            "Buhoro", "Gasabo", "Mutara", "Ubusabane"
        ));
        rwezamenyoCells.put("Rwezamenyo I", Arrays.asList(
            "Abatarushwa", "Indatwa", "Inkerakubanza", "Intwari"
        ));
        rwezamenyoCells.put("Rwezamenyo Ii", Arrays.asList(
            "Amahoro", "Umucyo", "Urumuri"
        ));
        sectors.put("Rwezamenyo", rwezamenyoCells);
        
        return sectors;
    }
}

