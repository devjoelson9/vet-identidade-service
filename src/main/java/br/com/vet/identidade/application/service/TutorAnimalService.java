package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AnimalRequestDTO;
import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.AnimalMapper;
import br.com.vet.identidade.domain.model.Animal;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.repository.AnimalRepository;
import br.com.vet.identidade.domain.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorAnimalService {

    private final AnimalRepository animalRepository;
    private final TutorRepository tutorRepository;
    private final AnimalMapper animalMapper;

    @Transactional
    public AnimalResponseDTO cadastrar(AnimalRequestDTO request) {
        Tutor tutor = getCurrentTutor();
        Animal animal = animalMapper.toEntity(request, tutor);
        Animal salvo = animalRepository.save(animal);
        return animalMapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponseDTO> listar(Pageable pageable) {
        Tutor tutor = getCurrentTutor();
        return animalRepository.findByTutorAndDeletadoEmIsNull(tutor, pageable)
                .map(animalMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public AnimalResponseDTO buscarPorId(Long id) {
        Tutor tutor = getCurrentTutor();
        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(id, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
        return animalMapper.toDTO(animal);
    }

    @Transactional
    public AnimalResponseDTO atualizar(Long id, AnimalRequestDTO request) {
        Tutor tutor = getCurrentTutor();
        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(id, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));

        animalMapper.updateEntity(animal, request);
        Animal salvo = animalRepository.save(animal);
        return animalMapper.toDTO(salvo);
    }

    @Transactional
    public void desativar(Long id) {
        Tutor tutor = getCurrentTutor();
        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(id, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));

        animal.setDeletadoEm(java.time.LocalDateTime.now());
        animalRepository.save(animal);
    }

    private Tutor getCurrentTutor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return tutorRepository.findByUsuario_EmailAndDeletadoEmIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado para o usuário logado"));
    }
}