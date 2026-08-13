package com.prisma.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import com.prisma.models.Caso;

@Service
public class CasoRepository {
    private final List<Caso> casos = new ArrayList<>();

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp"
    );

    @PostConstruct
    public void init() {
        loadFromCasosFolder();
        if (casos.isEmpty()) {
            seed();
        }
    }

    public List<Caso> getCasos() {
        return casos;
    }

    public void addCaso(Caso caso) {
        casos.add(caso);
    }

    private void loadFromCasosFolder() {
        Path casosDir = resolveCasosFolder();
        if (casosDir == null || !Files.isDirectory(casosDir)) {
            return;
        }

        List<Caso> found = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(casosDir)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && isImage(entry)) {
                    String fileName = entry.getFileName().toString();
                    String caseName = stripExtension(fileName);
                    Caso caso = new Caso(caseName, entry.toAbsolutePath().toString());
                    found.add(caso);
                }
            }
        } catch (IOException e) {
            System.err.println("NEXUS: No se pudo leer la carpeta casos/: " + e.getMessage());
        }

        found.sort(Comparator.comparing(Caso::getNombre, String.CASE_INSENSITIVE_ORDER));
        casos.addAll(found);
    }

    private Path resolveCasosFolder() {
        Path userDir = Paths.get(System.getProperty("user.dir"), "casos");
        if (Files.isDirectory(userDir)) {
            return userDir;
        }
        return null;
    }

    private boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    private void seed() {
        if (!casos.isEmpty()) {
            return;
        }

        casos.addAll(List.of(
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
                        "Operación NEXUS",
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
        ));
    }
}