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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecepcionistaAnimalService {

    private final AnimalRepository animalRepository;
    private final TutorRepository tutorRepository;
    private final AnimalMapper animalMapper;

    @Transactional
    public AnimalResponseDTO cadastrar(Long tutorId, AnimalRequestDTO request) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        Animal animal = animalMapper.toEntity(request, tutor);
        Animal salvo = animalRepository.save(animal);
        return animalMapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponseDTO> listarPorTutor(Long tutorId, Pageable pageable) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        return animalRepository.findByTutorAndDeletadoEmIsNull(tutor, pageable)
                .map(animalMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public AnimalResponseDTO buscarPorId(Long tutorId, Long animalId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(animalId, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));
        return animalMapper.toDTO(animal);
    }

    @Transactional
    public AnimalResponseDTO atualizar(Long tutorId, Long animalId, AnimalRequestDTO request) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(animalId, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        animalMapper.updateEntity(animal, request);
        Animal salvo = animalRepository.save(animal);
        return animalMapper.toDTO(salvo);
    }

    @Transactional
    public void desativar(Long tutorId, Long animalId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .filter(t -> t.getDeletadoEm() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado: " + tutorId));

        Animal animal = animalRepository.findByIdAndTutorAndDeletadoEmIsNull(animalId, tutor)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + animalId));

        animal.setDeletadoEm(java.time.LocalDateTime.now());
        animalRepository.save(animal);
    }
}