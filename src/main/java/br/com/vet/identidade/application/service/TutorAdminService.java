package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        usuario.setDeletadoEm(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        tutor.setDeletadoEm(java.time.LocalDateTime.now());
        tutorRepository.save(tutor);
    }

    @Transactional(readOnly = true)
    public Tutor buscarTutorAtivo(Long tutorId) {
        return tutorRepository.findByIdAndDeletadoEmIsNull(tutorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));
    }
}