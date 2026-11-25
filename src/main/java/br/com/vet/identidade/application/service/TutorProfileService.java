package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
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
        if (request.cpf() != null) {
            String cpf = request.cpf().trim();

            usuarioRepository.findByCpf(cpf)
                    .filter(outro -> !outro.getId().equals(usuario.getId()))
                    .ifPresent(outro -> {
                        throw new CpfAlreadyExistsException(cpf);
                    });

            usuario.setCpf(cpf);
        }
        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();

            if (email.isBlank()) {
                throw new BusinessException("E-mail não pode ser vazio");
            }

            usuarioRepository.findByEmail(email)
                    .filter(outro -> !outro.getId().equals(usuario.getId()))
                    .ifPresent(outro -> {
                        throw new EmailAlreadyExistsException(email);
                    });

            usuario.setEmail(email);
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