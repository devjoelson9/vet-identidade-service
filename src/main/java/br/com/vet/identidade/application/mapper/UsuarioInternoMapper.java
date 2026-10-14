package br.com.vet.identidade.application.mapper;

import br.com.vet.identidade.application.dto.request.RegistrarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.request.AtualizarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.response.UsuarioInternoResponse;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.model.Veterinario;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioInternoMapper {

    public Usuario toUsuario(RegistrarUsuarioInternoRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto());
        usuario.setCpf(request.getCpfLimpo());
        usuario.setEmail(request.getEmailNormalizado());
        usuario.setTelefone(request.telefone());
        usuario.setDataNascimento(request.dataNascimento());
        usuario.getRoles().add(request.role());
        return usuario;
    }

    public Veterinario toVeterinario(Usuario usuario, RegistrarUsuarioInternoRequest request) {
        if (request.role() != Role.VETERINARIO) {
            return null;
        }
        return new Veterinario(usuario, request.getCrmvNormalizado(), request.especialidade().trim());
    }

    public UsuarioInternoResponse toResponse(Usuario usuario, Optional<Veterinario> veterinarioOpt) {
        String crmv = null;
        String especialidade = null;
        if (veterinarioOpt.isPresent()) {
            crmv = veterinarioOpt.get().getCrmv();
            especialidade = veterinarioOpt.get().getEspecialidade();
        }
        return new UsuarioInternoResponse(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getCpf(),
                usuario.getDataNascimento(),
                usuario.getEmail(),
                usuario.getTelefone(),
                usuario.getStatus().name(),
                usuario.getRoles(),
                crmv,
                especialidade
        );
    }

    public void updateUsuario(Usuario usuario, AtualizarUsuarioInternoRequest request) {
        if (request.nomeCompleto() != null && !request.nomeCompleto().isBlank()) {
            usuario.setNomeCompleto(request.nomeCompleto());
        }
        if (request.dataNascimento() != null) {
            usuario.setDataNascimento(request.dataNascimento());
        }
        if (request.telefone() != null) {
            usuario.setTelefone(request.telefone());
        }
    }

    public void updateVeterinario(Veterinario veterinario, AtualizarUsuarioInternoRequest request) {
        if (request.getCrmvNormalizado() != null && !request.getCrmvNormalizado().isBlank()) {
            veterinario.setCrmv(request.getCrmvNormalizado());
        }
        if (request.especialidade() != null && !request.especialidade().isBlank()) {
            veterinario.setEspecialidade(request.especialidade().trim());
        }
    }
}