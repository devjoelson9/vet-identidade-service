package br.com.vet.identidade.infrastructure.security;

import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (usuario.getDeletadoEm() != null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + email);
        }

        if (usuario.getStatus() != br.com.vet.identidade.domain.enums.StatusUsuario.ATIVO) {
            throw new UsernameNotFoundException("Conta desativada: " + email);
        }

        Collection<SimpleGrantedAuthority> authorities = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getSenhaHash(),
                authorities
        );
    }
}