package com.szelag.pia.service.mock;

import java.util.List;
import java.util.Map;

/**
 * Raw product data repository for mock catalog.
 * In production, this would be replaced by a database or external API.
 */
public class ProductData {

    // Fix #3: Map.ofEntries instead of Map.of (no 10-entry limit)
    public static final Map<String, Map<String, String>> NAMES = Map.ofEntries(
            Map.entry("BOLT-M8-50", Map.of(
                    "nl-NL", "Bout M8x50 verzinkt",
                    "de-DE", "Schraube M8x50 verzinkt",
                    "pl-PL", "Śruba M8x50 ocynkowana",
                    "en-GB", "Bolt M8x50 galvanised")),
            Map.entry("FILTER-HYD-001", Map.of(
                    "nl-NL", "Hydraulisch filter 10 micron",
                    "de-DE", "Hydraulikfilter 10 Mikron",
                    "pl-PL", "Filtr hydrauliczny 10 mikronów",
                    "en-GB", "Hydraulic filter 10 micron")),
            Map.entry("BEARING-6205-2RS", Map.of(
                    "nl-NL", "Kogellager 6205-2RS",
                    "de-DE", "Kugellager 6205-2RS",
                    "pl-PL", "Łożysko kulkowe 6205-2RS",
                    "en-GB", "Ball bearing 6205-2RS")));

    public static final Map<String, Map<String, String>> DESCRIPTIONS = Map.ofEntries(
            Map.entry("BOLT-M8-50", Map.of(
                    "nl-NL", "Hoogwaardige verzinkt stalen bout, metrisch M8, lengte 50mm.",
                    "de-DE", "Hochwertige verzinkte Stahlschraube, metrisch M8, Länge 50mm.",
                    "pl-PL", "Wysokiej jakości śruba ze stali ocynkowanej, metryczna M8, długość 50mm.",
                    "en-GB", "High-quality galvanised steel bolt, metric M8, length 50mm.")),
            Map.entry("FILTER-HYD-001", Map.of(
                    "nl-NL", "Hydraulisch oliefilter met 10 micron filtratie.",
                    "de-DE", "Hydraulikölfilter mit 10 Mikron Filtration.",
                    "pl-PL", "Filtr oleju hydraulicznego z filtracją 10 mikronów.",
                    "en-GB", "Hydraulic oil filter with 10 micron filtration.")),
            Map.entry("BEARING-6205-2RS", Map.of(
                    "nl-NL", "Groefkogellager, dubbelzijdig afgedicht.",
                    "de-DE", "Rillenkugellager, beidseitig abgedichtet.",
                    "pl-PL", "Łożysko kulkowe zwykłe, dwustronnie uszczelnione.",
                    "en-GB", "Deep groove ball bearing, double sealed.")));

    public static final Map<String, List<String>> IMAGES = Map.ofEntries(
            Map.entry("BOLT-M8-50", List.of(
                    "https://cdn-icons-png.flaticon.com/512/2343/2343368.png")),
            Map.entry("FILTER-HYD-001", List.of(
                    "https://cdn-icons-png.flaticon.com/512/3063/3063821.png")),
            Map.entry("BEARING-6205-2RS", List.of(
                    "https://cdn-icons-png.flaticon.com/512/2103/2103633.png")));

    public static final Map<String, Map<String, String>> SPECS = Map.ofEntries(
            Map.entry("BOLT-M8-50", Map.of("thread", "M8", "length_mm", "50", "material", "Steel")),
            Map.entry("FILTER-HYD-001", Map.of("filtration_micron", "10", "pressure_bar", "25")),
            Map.entry("BEARING-6205-2RS", Map.of("inner_diameter_mm", "25", "outer_diameter_mm", "52")));

    private ProductData() {
    } // utility class, no instantiation
}