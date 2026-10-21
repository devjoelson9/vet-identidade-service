package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.AnimalMapper;
import br.com.vet.identidade.domain.model.Animal;
import br.com.vet.identidade.domain.repository.AnimalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeterinarioAnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;

    @Transactional(readOnly = true)
    public Page<AnimalResponseDTO> listar(Pageable pageable) {
        return animalRepository.findByDeletadoEmIsNull(pageable)
                .map(animalMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponseDTO> buscarPorTermo(String termo, Pageable pageable) {
        if (termo == null || termo.trim().isEmpty()) {
            return listar(pageable);
        }
        return animalRepository.buscarPorTermo(termo.trim(), pageable)
                .map(animalMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public AnimalResponseDTO buscarPorId(Long id) {
        Animal animal = animalRepository.findByIdAndDeletadoEmIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + id));
        return animalMapper.toDTO(animal);
    }
}