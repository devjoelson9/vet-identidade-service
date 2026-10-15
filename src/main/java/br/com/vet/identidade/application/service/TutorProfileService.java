package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.TutorMapper;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorProfileService {

    private final TutorRepository tutorRepository;
    private final UsuarioRepository usuarioRepository;
    private final TutorMapper tutorMapper;

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarMeuPerfil() {
        Long usuarioId = getCurrentUsuarioId();
        Tutor tutor = tutorRepository.findByUsuarioIdAndDeletadoEmIsNull(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de tutor não encontrado"));
        return tutorMapper.toDTO(tutor);
    }

    @Transactional
    public TutorResponseDTO atualizarMeuPerfil(AtualizarTutorRequestDTO request) {
        Long usuarioId = getCurrentUsuarioId();
        Tutor tutor = tutorRepository.findByUsuarioIdAndDeletadoEmIsNull(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de tutor não encontrado"));

        Usuario usuario = tutor.getUsuario();

        if (request.nomeCompleto() != null && !request.nomeCompleto().isBlank()) {
            usuario.setNomeCompleto(request.nomeCompleto());
        }
        if (request.telefone() != null) {
            usuario.setTelefone(request.telefone());
        }
        if (request.endereco() != null) {
            tutor.setEndereco(request.endereco());
        }

        usuarioRepository.save(usuario);
        tutorRepository.save(tutor);

        return tutorMapper.toDTO(tutor);
    }

    private Long getCurrentUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
        return usuario.getId();
    }
}