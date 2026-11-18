package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.response.VeterinarioResumoResponse;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeterinarioConsultaService {

    private final VeterinarioRepository veterinarioRepository;

    @Transactional(readOnly = true)
    public List<VeterinarioResumoResponse> listarAtivos() {
        return veterinarioRepository.findAll().stream()
                .filter(veterinario -> veterinario.getDeletadoEm() == null)
                .filter(veterinario -> veterinario.getUsuario() != null)
                .filter(veterinario -> veterinario.getUsuario().getDeletadoEm() == null)
                .filter(veterinario -> veterinario.getUsuario().getStatus() == StatusUsuario.ATIVO)
                .map(veterinario -> new VeterinarioResumoResponse(
                        veterinario.getId(),
                        veterinario.getUsuario().getNomeCompleto(),
                        veterinario.getCrmv(),
                        veterinario.getEspecialidade(),
                        veterinario.getUsuario().getStatus().name()
                ))
                .sorted(Comparator.comparing(VeterinarioResumoResponse::nomeCompleto,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<VeterinarioResumoResponse> buscarPorUsuario(Long usuarioId) {
        return veterinarioRepository.findByUsuarioId(usuarioId)
                .filter(veterinario -> veterinario.getDeletadoEm() == null)
                .filter(veterinario -> veterinario.getUsuario() != null)
                .filter(veterinario -> veterinario.getUsuario().getDeletadoEm() == null)
                .filter(veterinario -> veterinario.getUsuario().getStatus() == StatusUsuario.ATIVO)
                .map(veterinario -> new VeterinarioResumoResponse(
                        veterinario.getId(),
                        veterinario.getUsuario().getNomeCompleto(),
                        veterinario.getCrmv(),
                        veterinario.getEspecialidade(),
                        veterinario.getUsuario().getStatus().name()
                ));
    }
}
