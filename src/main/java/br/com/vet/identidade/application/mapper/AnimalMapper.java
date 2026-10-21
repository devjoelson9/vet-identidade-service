package br.com.vet.identidade.application.mapper;

import br.com.vet.identidade.application.dto.request.AnimalRequestDTO;
import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.domain.model.Animal;
import br.com.vet.identidade.domain.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

    public Animal toEntity(AnimalRequestDTO request, Tutor tutor) {
        Animal animal = new Animal();
        animal.setTutor(tutor);
        animal.setNome(request.nome());
        animal.setEspecie(request.especie());
        animal.setRaca(request.raca());
        animal.setSexo(request.sexo());
        animal.setDataNascimento(request.dataNascimento());
        animal.setPeso(request.peso());
        animal.setObservacoes(request.observacoes());
        return animal;
    }

    public void updateEntity(Animal animal, AnimalRequestDTO request) {
        animal.setNome(request.nome());
        animal.setEspecie(request.especie());
        animal.setRaca(request.raca());
        animal.setSexo(request.sexo());
        animal.setDataNascimento(request.dataNascimento());
        animal.setPeso(request.peso());
        animal.setObservacoes(request.observacoes());
    }

    public AnimalResponseDTO toDTO(Animal animal) {
        return new AnimalResponseDTO(
                animal.getId(),
                animal.getTutor().getId(),
                animal.getTutor().getUsuario().getNomeCompleto(),
                animal.getNome(),
                animal.getEspecie(),
                animal.getRaca(),
                animal.getSexo(),
                animal.getDataNascimento(),
                animal.getPeso(),
                animal.getObservacoes(),
                animal.getCriadoEm(),
                animal.getAtualizadoEm(),
                animal.getDeletadoEm()
        );
    }
}