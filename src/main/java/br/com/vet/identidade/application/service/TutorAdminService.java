package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TutorAdminService {

    private final TutorRepository tutorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void desativarTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        Usuario usuario = tutor.getUsuario();
        usuario.setStatus(StatusUsuario.DESATIVADO);
        usuario.setDeletadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        tutor.setDeletadoEm(LocalDateTime.now());
        tutorRepository.save(tutor);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarTutorAtivo(Long tutorId) {
        Tutor tutor = tutorRepository.findByIdAndDeletadoEmIsNull(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));
        return toDTO(tutor);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarTutorPorId(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));
        return toDTO(tutor);
    }

    @Transactional
    public TutorResponseDTO reativarTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor desativado não encontrado: " + tutorId));

        Usuario usuario = tutor.getUsuario();
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.setDeletadoEm(null);
        usuarioRepository.save(usuario);

        tutor.setDeletadoEm(null);
        tutorRepository.save(tutor);

        return toDTO(tutor);
    }

    private TutorResponseDTO toDTO(Tutor tutor) {
        Usuario u = tutor.getUsuario();
        return new TutorResponseDTO(
                tutor.getId(),
                u.getId(),
                u.getNomeCompleto(),
                u.getEmail(),
                u.getCpf(),
                u.getTelefone(),
                tutor.getEndereco(),
                u.getStatus().name(),
                tutor.getDeletadoEm()
        );
    }
}