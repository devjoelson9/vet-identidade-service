package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.RecepcionistaTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.TutorMapper;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecepcionistaTutorService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TutorMapper tutorMapper;

    @Transactional
    public TutorResponseDTO cadastrar(RecepcionistaTutorRequestDTO request) {
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

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> listar(Pageable pageable) {
        return tutorRepository.findByDeletadoEmIsNull(pageable)
                .map(tutorMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> buscarPorTermo(String termo, Pageable pageable) {
        if (termo == null || termo.trim().isEmpty()) {
            return listar(pageable);
        }
        return tutorRepository.buscarPorTermo(termo.trim(), pageable)
                .map(tutorMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + id));
        return tutorMapper.toDTO(tutor);
    }

    @Transactional
    public TutorResponseDTO atualizar(Long id, RecepcionistaTutorRequestDTO request) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + id));

        Usuario usuario = tutor.getUsuario();

        String novoEmail = request.getEmailNormalizado();
        String novoCpf = request.getCpfLimpo();

        if (!usuario.getEmail().equals(novoEmail) && usuarioRepository.existsByEmail(novoEmail)) {
            throw new EmailAlreadyExistsException(novoEmail);
        }
        if (!usuario.getCpf().equals(novoCpf) && usuarioRepository.existsByCpf(novoCpf)) {
            throw new CpfAlreadyExistsException(novoCpf);
        }

        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setCpf(novoCpf);
        usuario.setEmail(novoEmail);
        usuario.setTelefone(request.telefone());
        tutor.setEndereco(request.endereco());

        usuarioRepository.save(usuario);
        tutorRepository.save(tutor);

        return tutorMapper.toDTO(tutor);
    }

    @Transactional
    public void desativar(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + id));

        Usuario usuario = tutor.getUsuario();
        usuario.setStatus(StatusUsuario.DESATIVADO);
        usuario.setDeletadoEm(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        tutor.setDeletadoEm(java.time.LocalDateTime.now());
        tutorRepository.save(tutor);
    }
}