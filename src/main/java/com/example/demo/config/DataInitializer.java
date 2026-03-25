package com.example.demo.config;

import com.example.demo.entidades.Active;
import com.example.demo.repositorios.ActiveRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ActiveRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<Active> actives = List.of(
                    new Active("Bancolombia - ADR", "CIB"),
                    new Active("Ecopetrol - ADR", "EC"),
                    new Active("Grupo Aval - ADR", "AVAL"),
                    new Active("Tecnoglass Inc.", "TGLS"),
                    new Active("GeoPark Ltd.", "GPRK"),
                    new Active("Gran Tierra Energy", "GTE"),
                    new Active("Parex Resources", "PXT.TO"),
                    new Active("CEMEX - ADR", "CX"),
                    new Active("Petrobras - ADR", "PBR"),
                    new Active("Itau Unibanco - ADR", "ITUB"),
                    new Active("Banco Bradesco - ADR", "BBD"),
                    new Active("Copa Holdings", "CPA"),
                    new Active("Credicorp Ltd.", "BAP"),
                    new Active("MercadoLibre", "MELI"),
                    new Active("Banco Santander Chile", "BSAC"),
                    new Active("Grupo Aeroportuario del Sureste", "ASR"),
                    new Active("Grupo Aeroportuario del Pacifico", "PAC"),
                    new Active("Fomento Economico Mexicano", "FMX"),
                    new Active("Coca-Cola FEMSA", "KOF"),
                    new Active("Gruma", "GRUMAB.MX")
                );
                repository.saveAll(actives);
            }
        };
    }
}
