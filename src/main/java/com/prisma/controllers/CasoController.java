package com.prisma.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prisma.data.CasoRepository;
import com.prisma.models.Caso;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CasoController {
    
    private final CasoRepository casoRepository;

    public CasoController(CasoRepository casoRepository) {
        this.casoRepository = casoRepository;
    }

    @GetMapping("/casos")
    public List<Caso> getAllCasos() {
        return casoRepository.getCasos();
    }

    @GetMapping("/images/{nombreCaso}")
    public ResponseEntity<Resource> getImage(@PathVariable String nombreCaso) {
        Optional<Caso> casoOpt = casoRepository.getCasos().stream()
                .filter(c -> c.getNombre().equals(nombreCaso))
                .findFirst();

        if (casoOpt.isPresent() && casoOpt.get().getImagenPath() != null) {
            try {
                Path path = Paths.get(casoOpt.get().getImagenPath());
                Resource resource = new UrlResource(path.toUri());
                if (resource.exists() || resource.isReadable()) {
                    String contentType = Files.probeContentType(path);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                }
            } catch (IOException e) {
                // ignore and return 404
            }
        }
        return ResponseEntity.notFound().build();
    }
}
