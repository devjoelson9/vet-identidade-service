package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AtualizarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.request.RegistrarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.response.UsuarioInternoResponse;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.CrmvAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.exception.InvalidRoleException;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.UsuarioInternoMapper;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.model.Veterinario;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.domain.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioInternoService {

    private final UsuarioRepository usuarioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioInternoMapper mapper;

    @Transactional
    public UsuarioInternoResponse cadastrar(RegistrarUsuarioInternoRequest request) {
        if (!request.isRoleValida()) {
            throw new InvalidRoleException(request.role().name());
        }

        if (usuarioRepository.existsByEmail(request.getEmailNormalizado())) {
            throw new EmailAlreadyExistsException(request.getEmailNormalizado());
        }
        if (usuarioRepository.existsByCpf(request.getCpfLimpo())) {
            throw new CpfAlreadyExistsException(request.getCpfLimpo());
        }
        if (request.role() == Role.VETERINARIO && veterinarioRepository.existsByCrmv(request.getCrmvNormalizado())) {
            throw new CrmvAlreadyExistsException(request.getCrmvNormalizado());
        }

        Usuario usuario = mapper.toUsuario(request);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setStatus(StatusUsuario.ATIVO);

        Usuario salvo = usuarioRepository.save(usuario);

        Veterinario veterinario = mapper.toVeterinario(salvo, request);
        if (veterinario != null) {
            veterinarioRepository.save(veterinario);
        }

        return mapper.toResponse(salvo, Optional.ofNullable(veterinario));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioInternoResponse> listar(Role role, StatusUsuario status, Pageable pageable) {
        Page<Usuario> usuarios;
        if (role != null && status != null) {
            usuarios = usuarioRepository.findByRoleAndStatusAndDeletadoEmIsNull(role, status, Role.TUTOR, pageable);
        } else if (role != null) {
            usuarios = usuarioRepository.findByRoleAndDeletadoEmIsNull(role, Role.TUTOR, pageable);
        } else if (status != null) {
            usuarios = usuarioRepository.findByStatusAndDeletadoEmIsNull(status, Role.TUTOR, pageable);
        } else {
            usuarios = usuarioRepository.findByDeletadoEmIsNull(Role.TUTOR, pageable);
        }
        return usuarios.map(u -> mapper.toResponse(u, veterinarioRepository.findByUsuarioId(u.getId())));
    }

    @Transactional(readOnly = true)
    public UsuarioInternoResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (usuario.getRoles().contains(Role.TUTOR)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        }

        return mapper.toResponse(usuario, veterinarioRepository.findByUsuarioId(usuario.getId()));
    }

    @Transactional
    public UsuarioInternoResponse atualizar(Long id, AtualizarUsuarioInternoRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (usuario.getRoles().contains(Role.TUTOR)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        }

        mapper.updateUsuario(usuario, request);

        if (usuario.getRoles().contains(Role.VETERINARIO)) {
            Veterinario veterinario = veterinarioRepository.findByUsuarioId(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado para o usuário: " + id));
            mapper.updateVeterinario(veterinario, request);
        }

        Usuario salvo = usuarioRepository.save(usuario);
        return mapper.toResponse(salvo, veterinarioRepository.findByUsuarioId(id));
    }

    @Transactional
    public UsuarioInternoResponse alterarStatus(Long id, StatusUsuario novoStatus) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> u.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (usuario.getRoles().contains(Role.TUTOR) || usuario.getRoles().contains(Role.ADMIN)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + id);
        }

        usuario.setStatus(novoStatus);
        if (novoStatus == StatusUsuario.DESATIVADO) {
            usuario.setDeletadoEm(java.time.LocalDateTime.now());
        } else {
            usuario.setDeletadoEm(null);
        }

        Usuario salvo = usuarioRepository.save(usuario);
        return mapper.toResponse(salvo, veterinarioRepository.findByUsuarioId(id));
    }

    @Transactional
    public void desativar(Long id) {
        alterarStatus(id, StatusUsuario.DESATIVADO);
    }
}