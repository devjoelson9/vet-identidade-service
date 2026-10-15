package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.RegistrarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.mapper.TutorMapper;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorCadastroService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TutorMapper tutorMapper;

    @Transactional
    public TutorResponseDTO cadastrar(RegistrarTutorRequestDTO request) {
        String email = request.getEmailNormalizado();
        String cpf = request.getCpfLimpo();

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new CpfAlreadyExistsException(cpf);
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setTelefone(request.telefone());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.getRoles().add(Role.TUTOR);

        Usuario salvo = usuarioRepository.save(usuario);

        Tutor tutor = new Tutor(salvo, request.endereco());
        tutorRepository.save(tutor);

        return tutorMapper.toDTO(tutor);
    }
}