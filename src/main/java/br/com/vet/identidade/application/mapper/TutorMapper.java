package br.com.vet.identidade.application.mapper;

import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.domain.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper {

    public TutorResponseDTO toDTO(Tutor tutor) {
        var u = tutor.getUsuario();
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