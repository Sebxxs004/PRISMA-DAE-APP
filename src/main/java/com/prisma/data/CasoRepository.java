package com.prisma.data;

import java.time.LocalDate;
import java.util.List;

import com.prisma.models.Caso;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class CasoRepository {
    private static final ObservableList<Caso> CASOS = FXCollections.observableArrayList();

    static {
        seed();
    }

    private CasoRepository() {
    }

    public static ObservableList<Caso> getCasos() {
        return CASOS;
    }

    public static void addCaso(Caso caso) {
        CASOS.add(caso);
    }

    private static void seed() {
        if (!CASOS.isEmpty()) {
            return;
        }

        CASOS.addAll(
                new Caso(
                        "Caso Aurora",
                        "Investigación inicial por alteración de evidencia en zona urbana.",
                        "Quito",
                        LocalDate.of(2026, 1, 12),
                        List.of("María López"),
                        List.of("Sujeto A"),
                        List.of("Fraude procesal"),
                        List.of("Fiscalía", "Policía Judicial")
                ),
                new Caso(
                        "Operación Prisma",
                        "Posible red de coacción y encubrimiento con múltiples testigos.",
                        "Guayaquil",
                        LocalDate.of(2026, 2, 4),
                        List.of("Carlos Mena", "Andrea Ruiz"),
                        List.of("Grupo desconocido"),
                        List.of("Coacción", "Encubrimiento"),
                        List.of("Fiscal", "Peritos", "Testigos")
                ),
                new Caso(
                        "Noche Cero",
                        "Hechos violentos vinculados a ingreso forzado y robo agravado.",
                        "Cuenca",
                        LocalDate.of(2026, 3, 18),
                        List.of("Luis Andrade"),
                        List.of("Dos implicados"),
                        List.of("Robo agravado", "Lesiones"),
                        List.of("Fiscalía", "Víctima", "Patrullaje")
                ),
                new Caso(
                        "Caso Vértice",
                        "Conjunto de movimientos financieros incompatibles con la actividad declarada.",
                        "Manta",
                        LocalDate.of(2026, 4, 2),
                        List.of("Entidad afectada"),
                        List.of("Administrador interno"),
                        List.of("Lavado de activos"),
                        List.of("Unidad de análisis", "Auditoría")
                ),
                new Caso(
                        "Caso Horizonte",
                        "Conflicto territorial con versiones cruzadas y cadenas de mando mixtas.",
                        "Loja",
                        LocalDate.of(2026, 4, 21),
                        List.of("Juana Torres"),
                        List.of("Sospechoso principal"),
                        List.of("Amenazas", "Asociación ilícita"),
                        List.of("Fiscal", "Investigadores", "Vecinos")
                )
        );
    }
}